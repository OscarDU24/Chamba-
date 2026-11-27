package org.example.projectofinal.service;

import jakarta.persistence.EntityManager;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.pdf.JRPdfExporter;
import org.example.projectofinal.dao.EntityManagerAdmin;
import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.model.PeriodoEvaluacion;
import org.hibernate.Session;

import java.io.File;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Genera los reportes con las plantillas de JasperReports (carpeta resources/.../reportes)
 * y los exporta a PDF o a Excel (.xlsx).
 * Cada plantilla trae su propia consulta SQL; aquí solo se le pasan los parámetros
 * y la conexión a la base, así que los datos siempre son los del momento de exportar.
 */
public class ReporteJasperService {

    /** Formatos de salida. */
    public enum Formato {
        PDF("pdf", "PDF (*.pdf)"),
        EXCEL("xlsx", "Excel (*.xlsx)");

        public final String extension;
        public final String descripcion;

        Formato(String extension, String descripcion) {
            this.extension = extension;
            this.descripcion = descripcion;
        }
    }

    /** Plantillas disponibles (archivos .jrxml). */
    public enum Plantilla {
        GENERAL("ReporteGeneral", "Reporte general"),
        DEPARTAMENTAL("ReporteDepartamental", "Reporte departamental"),
        INDIVIDUAL("ReporteIndividual", "Reporte individual");

        final String archivo;
        final String hoja;

        Plantilla(String archivo, String hoja) {
            this.archivo = archivo;
            this.hoja = hoja;
        }
    }

    private static final String CARPETA = "/org/example/projectofinal/reportes/";

    /** Las plantillas se compilan una sola vez mientras la app está abierta. */
    private static final Map<Plantilla, JasperReport> COMPILADAS = new EnumMap<>(Plantilla.class);

    // =========================================================
    // REPORTES
    // =========================================================

    public void exportarGeneral(File archivo, Formato formato, PeriodoEvaluacion periodo) throws JRException {
        Map<String, Object> p = new HashMap<>();
        p.put("P_PERIODO", periodo.getNombre());
        exportar(Plantilla.GENERAL, p, archivo, formato);
    }

    public void exportarDepartamental(File archivo, Formato formato, PeriodoEvaluacion periodo,
                                      Departamento departamento) throws JRException {
        Map<String, Object> p = new HashMap<>();
        p.put("P_PERIODO", periodo.getNombre());
        p.put("P_DEPARTAMENTO", departamento.getNombre());
        exportar(Plantilla.DEPARTAMENTAL, p, archivo, formato);
    }

    public void exportarIndividual(File archivo, Formato formato, PeriodoEvaluacion periodo,
                                   Empleado empleado) throws JRException {
        Map<String, Object> p = new HashMap<>();
        p.put("P_PERIODO", periodo.getNombre());
        p.put("P_CIF", empleado.getCif());
        exportar(Plantilla.INDIVIDUAL, p, archivo, formato);
    }

    // =========================================================
    // COMPILAR, LLENAR Y EXPORTAR
    // =========================================================

    private void exportar(Plantilla plantilla, Map<String, Object> parametros, File archivo, Formato formato)
            throws JRException {

        // En Excel todo va en una sola hoja continua: sin saltos de página
        // (si no, el pie de página quedaría en medio de las tablas).
        Map<String, Object> todos = new HashMap<>(parametros);
        todos.put(JRParameter.IS_IGNORE_PAGINATION, formato == Formato.EXCEL);

        JasperPrint impreso = llenar(compilar(plantilla), todos);

        if (formato == Formato.PDF) {
            exportarPdf(impreso, archivo);
        } else {
            exportarExcel(impreso, archivo, plantilla.hoja);
        }
    }

    private static synchronized JasperReport compilar(Plantilla plantilla) throws JRException {
        JasperReport compilada = COMPILADAS.get(plantilla);
        if (compilada != null) {
            return compilada;
        }

        String ruta = CARPETA + plantilla.archivo + ".jrxml";
        try (InputStream jrxml = ReporteJasperService.class.getResourceAsStream(ruta)) {
            if (jrxml == null) {
                throw new JRException("No se encontró la plantilla " + ruta);
            }
            compilada = JasperCompileManager.compileReport(jrxml);
        } catch (java.io.IOException e) {
            throw new JRException("No se pudo leer la plantilla " + ruta, e);
        }

        COMPILADAS.put(plantilla, compilada);
        return compilada;
    }

    /** Llena el reporte usando la misma conexión a PostgreSQL que usa Hibernate. */
    private JasperPrint llenar(JasperReport reporte, Map<String, Object> parametros) throws JRException {
        EntityManager em = EntityManagerAdmin.getEntityManager();

        try {
            return em.unwrap(Session.class).doReturningWork(conexion -> {
                try {
                    return JasperFillManager.fillReport(reporte, parametros, conexion);
                } catch (JRException e) {
                    throw new IllegalStateException(e);
                }
            });

        } catch (IllegalStateException e) {
            if (e.getCause() instanceof JRException jr) {
                throw jr;
            }
            throw e;

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    private void exportarPdf(JasperPrint impreso, File archivo) throws JRException {
        JRPdfExporter exportador = new JRPdfExporter();
        exportador.setExporterInput(new SimpleExporterInput(impreso));
        exportador.setExporterOutput(new SimpleOutputStreamExporterOutput(archivo));
        exportador.exportReport();
    }

    private void exportarExcel(JasperPrint impreso, File archivo, String nombreHoja) throws JRException {
        SimpleXlsxReportConfiguration configuracion = new SimpleXlsxReportConfiguration();
        configuracion.setOnePagePerSheet(false);              // todo en una sola hoja
        configuracion.setSheetNames(new String[]{nombreHoja});
        configuracion.setDetectCellType(true);                // números como números
        configuracion.setRemoveEmptySpaceBetweenRows(true);
        configuracion.setRemoveEmptySpaceBetweenColumns(true);
        configuracion.setWhitePageBackground(false);
        configuracion.setIgnorePageMargins(true);
        configuracion.setCollapseRowSpan(false);

        JRXlsxExporter exportador = new JRXlsxExporter();
        exportador.setExporterInput(new SimpleExporterInput(impreso));
        exportador.setExporterOutput(new SimpleOutputStreamExporterOutput(archivo));
        exportador.setConfiguration(configuracion);
        exportador.exportReport();
    }
}
