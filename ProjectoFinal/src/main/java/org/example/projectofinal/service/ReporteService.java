
package org.example.projectofinal.service;

import jakarta.persistence.EntityManager;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.example.projectofinal.config.IExportar;
import org.example.projectofinal.config.ExportarPDF;
import org.example.projectofinal.config.ExportarExcel;

import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.model.Evaluacion;
import org.example.projectofinal.util.JpaUtil;

import org.hibernate.Session;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ReporteService {

    public record FilaDepartamento(
            String departamento,
            long empleados,
            long objetivos,
            BigDecimal avance,
            BigDecimal evaluacionFinal,
            String estadoEvaluacion
    ) {
    }

    public record FilaEmpleado(
            String empleado,
            String departamento,
            long objetivos,
            BigDecimal avance,
            BigDecimal evaluacionFinal,
            String estadoEvaluacion
    ) {
    }

    public record Opcion(UUID id, String nombre) {

        @Override
        public String toString() {
            return nombre;
        }
    }

    public enum Formato {
        PDF,
        XLSX
    }

    public enum TipoReporte {

        INDIVIDUAL,
        DEPARTAMENTAL,
        INSTITUCIONAL;

        @Override
        public String toString() {
            return switch (this) {
                case INDIVIDUAL -> "Reporte individual";
                case DEPARTAMENTAL -> "Reporte departamental";
                case INSTITUCIONAL -> "Reporte institucional";
            };
        }
    }

    private final EvaluacionService evaluacionService =
            new EvaluacionService();

    private final DepartamentoService departamentoService =
            new DepartamentoService();

    private final AdminPanelService empleadoService =
            new AdminPanelService();

    private final ObjetivoDepartamentoService asignacionService =
            new ObjetivoDepartamentoService();

    private final ResponsableObjetivoService responsableService =
            new ResponsableObjetivoService();

    public List<FilaDepartamento> porDepartamento(UUID idPeriodo) {

        Objects.requireNonNull(idPeriodo);

        List<Empleado> empleados = empleadoService.listarEmpleados();

        Map<UUID, Evaluacion> evaluaciones = new HashMap<>();

        for (Evaluacion e : evaluacionService.listarPorPeriodo(idPeriodo)) {

            if (EvaluacionService.DEPARTAMENTAL.equals(e.getTipo())
                    && e.getIdDepartamento() != null) {

                evaluaciones.put(
                        e.getIdDepartamento().getId(),
                        e
                );
            }
        }

        Map<UUID, Long> objetivosPorDep = new HashMap<>();

        asignacionService.listarPorPeriodo(idPeriodo)
                .forEach(a -> objetivosPorDep.merge(
                        a.getIdDepartamento().getId(),
                        1L,
                        Long::sum
                ));

        List<FilaDepartamento> filas = new ArrayList<>();

        for (Departamento d : departamentoService.listarActivos()) {

            long totalEmpleados = empleados.stream()
                    .filter(e -> Boolean.TRUE.equals(e.getActivo()))
                    .filter(e -> e.getIdDepartamento() != null)
                    .filter(e -> e.getIdDepartamento().getId().equals(d.getId()))
                    .count();

            Evaluacion ev = evaluaciones.get(d.getId());

            filas.add(
                    new FilaDepartamento(
                            d.getNombre(),
                            totalEmpleados,
                            objetivosPorDep.getOrDefault(d.getId(), 0L),
                            evaluacionService.calcularDepartamental(
                                    idPeriodo,
                                    d.getId()
                            ),
                            ev == null
                                    ? null
                                    : ev.getPuntuacionFinal(),
                            ev == null
                                    ? "Sin evaluar"
                                    : ev.getEstado()
                    )
            );
        }

        return filas;
    }

    public List<FilaEmpleado> porEmpleado(UUID idPeriodo) {

        Objects.requireNonNull(idPeriodo);

        Map<UUID, Evaluacion> evaluaciones = new HashMap<>();

        for (Evaluacion e : evaluacionService.listarPorPeriodo(idPeriodo)) {

            if (EvaluacionService.INDIVIDUAL.equals(e.getTipo())
                    && e.getIdEmpleado() != null) {

                evaluaciones.put(
                        e.getIdEmpleado().getId(),
                        e
                );
            }
        }

        Map<UUID, Long> objetivosPorEmpleado = new HashMap<>();

        asignacionService.listarPorPeriodo(idPeriodo).forEach(a ->
                responsableService.listarPorAsignacion(a.getId())
                        .forEach(r -> objetivosPorEmpleado.merge(
                                r.getIdEmpleado().getId(),
                                1L,
                                Long::sum
                        ))
        );

        List<FilaEmpleado> filas = new ArrayList<>();

        for (Empleado e : empleadoService.listarEmpleados()) {

            if (!Boolean.TRUE.equals(e.getActivo())) {
                continue;
            }

            Evaluacion ev = evaluaciones.get(e.getId());

            String departamento = e.getIdDepartamento() == null
                    ? "Sin departamento"
                    : e.getIdDepartamento().getNombre();

            filas.add(
                    new FilaEmpleado(
                            e.getNombreCompleto(),
                            departamento,
                            objetivosPorEmpleado.getOrDefault(e.getId(), 0L),
                            evaluacionService.calcularIndividual(
                                    idPeriodo,
                                    e.getId()
                            ),
                            ev == null
                                    ? null
                                    : ev.getPuntuacionFinal(),
                            ev == null
                                    ? "Sin evaluar"
                                    : ev.getEstado()
                    )
            );
        }

        return filas;
    }

    public static BigDecimal promedio(List<BigDecimal> valores) {

        List<BigDecimal> presentes = valores.stream()
                .filter(Objects::nonNull)
                .toList();

        if (presentes.isEmpty()) {
            return null;
        }

        return presentes.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(
                        BigDecimal.valueOf(presentes.size()),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    public List<Opcion> listarPeriodos() {

        return consultarOpciones(
                """
                SELECT id_periodo, nombre
                FROM periodos_evaluacion
                ORDER BY fecha_inicio DESC
                """
        );
    }

    public List<Opcion> listarDepartamentos() {

        return consultarOpciones(
                """
                SELECT id_departamento, nombre
                FROM departamentos
                WHERE activo = TRUE
                ORDER BY nombre
                """
        );
    }

    public List<Opcion> listarEmpleados() {

        return consultarOpciones(
                """
                SELECT id_empleado, nombre_completo
                FROM empleados
                WHERE activo = TRUE
                ORDER BY nombre_completo
                """
        );
    }

    private List<Opcion> consultarOpciones(String sql) {

        EntityManager em = JpaUtil.getEntityManager();

        try {

            @SuppressWarnings("unchecked")
            List<Object[]> resultados =
                    em.createNativeQuery(sql).getResultList();

            List<Opcion> opciones = new ArrayList<>();

            for (Object[] fila : resultados) {

                opciones.add(
                        new Opcion(
                                (UUID) fila[0],
                                (String) fila[1]
                        )
                );
            }

            return opciones;

        } finally {
            em.close();
        }
    }

    public void generar(
            TipoReporte tipo,
            Formato formato,
            UUID idPeriodo,
            UUID idSeleccionado,
            File destino
    ) throws JRException, IOException {

        Objects.requireNonNull(tipo, "Tipo de reporte requerido");
        Objects.requireNonNull(formato, "Formato requerido");
        Objects.requireNonNull(idPeriodo, "Período requerido");
        Objects.requireNonNull(destino, "Archivo de destino requerido");

        if (tipo != TipoReporte.INSTITUCIONAL
                && idSeleccionado == null) {

            throw new IllegalArgumentException(
                    "Debes seleccionar un empleado o departamento."
            );
        }

        String rutaPlantilla = switch (tipo) {

            case INDIVIDUAL ->
                    "/report/ReporteIndividual.jrxml";

            case DEPARTAMENTAL ->
                    "/report/ReporteDepartamental.jrxml";

            case INSTITUCIONAL ->
                    "/report/ReporteInstitucional.jrxml";
        };

        Map<String, Object> parametros = new HashMap<>();

        parametros.put("ID_PERIODO", idPeriodo);

        switch (tipo) {

            case INDIVIDUAL ->
                    parametros.put("ID_EMPLEADO", idSeleccionado);

            case DEPARTAMENTAL ->
                    parametros.put("ID_DEPARTAMENTO", idSeleccionado);

            case INSTITUCIONAL -> {
            }
        }

        if (formato == Formato.XLSX) {

            parametros.put(
                    JRParameter.IS_IGNORE_PAGINATION,
                    Boolean.TRUE
            );
        }

        JasperReport reporteCompilado;

        try (InputStream plantilla =
                     getClass().getResourceAsStream(rutaPlantilla)) {

            if (plantilla == null) {

                throw new IOException(
                        "No se encontró la plantilla: " + rutaPlantilla
                );
            }

            reporteCompilado =
                    JasperCompileManager.compileReport(plantilla);
        }

        JasperPrint reporteGenerado = llenarReporte(
                reporteCompilado,
                parametros
        );

        IExportar exportador = switch (formato) {

            case PDF -> new ExportarPDF();

            case XLSX -> new ExportarExcel();
        };

        exportador.exportar(
                reporteGenerado,
                destino
        );
    }

    private JasperPrint llenarReporte(
            JasperReport reporte,
            Map<String, Object> parametros
    ) throws JRException {

        EntityManager em = JpaUtil.getEntityManager();

        try {

            em.getTransaction().begin();

            JasperPrint[] resultado = new JasperPrint[1];

            JRException[] errorReporte = new JRException[1];

            em.unwrap(Session.class).doWork(conexion -> {

                try {

                    resultado[0] = JasperFillManager.fillReport(
                            reporte,
                            parametros,
                            conexion
                    );

                } catch (JRException e) {

                    errorReporte[0] = e;
                }
            });

            if (errorReporte[0] != null) {
                throw errorReporte[0];
            }

            em.getTransaction().commit();

            return resultado[0];

        } catch (JRException | RuntimeException e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    public void reporteIndividual(
            File destino,
            Formato formato,
            UUID idPeriodo,
            UUID idEmpleado
    ) throws JRException, IOException {

        generar(
                TipoReporte.INDIVIDUAL,
                formato,
                idPeriodo,
                idEmpleado,
                destino
        );
    }

    public void reporteDepartamental(
            File destino,
            Formato formato,
            UUID idPeriodo,
            UUID idDepartamento
    ) throws JRException, IOException {

        generar(
                TipoReporte.DEPARTAMENTAL,
                formato,
                idPeriodo,
                idDepartamento,
                destino
        );
    }

    public void reporteInstitucional(
            File destino,
            Formato formato,
            UUID idPeriodo
    ) throws JRException, IOException {

        generar(
                TipoReporte.INSTITUCIONAL,
                formato,
                idPeriodo,
                null,
                destino
        );
    }
}
