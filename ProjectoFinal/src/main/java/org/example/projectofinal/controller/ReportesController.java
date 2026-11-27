package org.example.projectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.concurrent.Task;
import javafx.scene.layout.VBox;
import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.model.PeriodoEvaluacion;
import org.example.projectofinal.service.AdminPanelService;
import org.example.projectofinal.service.DepartamentoService;
import org.example.projectofinal.service.PeriodoEvaluacionService;
import org.example.projectofinal.service.ReporteJasperService;
import org.example.projectofinal.service.ReporteJasperService.Formato;
import org.example.projectofinal.service.ReporteService;
import org.example.projectofinal.service.ReporteService.FilaDepartamento;
import org.example.projectofinal.service.ReporteService.FilaEmpleado;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ReportesController {

    @FXML private ComboBox<PeriodoEvaluacion> cmbPeriodo;
    @FXML private TabPane tabs;
    @FXML private Tab tabDepartamentos;

    @FXML private Label lblPromedioDepartamentos;
    @FXML private Label lblPromedioEmpleados;
    @FXML private Label lblEvaluados;

    @FXML private TableView<FilaDepartamento> tablaDepartamentos;
    @FXML private TableColumn<FilaDepartamento, String> colDepNombre;
    @FXML private TableColumn<FilaDepartamento, String> colDepEmpleados;
    @FXML private TableColumn<FilaDepartamento, String> colDepObjetivos;
    @FXML private TableColumn<FilaDepartamento, String> colDepAvance;
    @FXML private TableColumn<FilaDepartamento, String> colDepFinal;
    @FXML private TableColumn<FilaDepartamento, String> colDepEstado;

    @FXML private TableView<FilaEmpleado> tablaEmpleados;
    @FXML private TableColumn<FilaEmpleado, String> colEmpNombre;
    @FXML private TableColumn<FilaEmpleado, String> colEmpDepartamento;
    @FXML private TableColumn<FilaEmpleado, String> colEmpObjetivos;
    @FXML private TableColumn<FilaEmpleado, String> colEmpAvance;
    @FXML private TableColumn<FilaEmpleado, String> colEmpFinal;
    @FXML private TableColumn<FilaEmpleado, String> colEmpEstado;

    @FXML private Label lblEstado;

    // Exportación con las plantillas de Jasper (PDF y Excel)
    @FXML private ComboBox<Departamento> cmbDepartamentoReporte;
    @FXML private ComboBox<Empleado> cmbEmpleadoReporte;
    @FXML private VBox panelExportar;

    private final ReporteService service = new ReporteService();
    private final PeriodoEvaluacionService periodoService = new PeriodoEvaluacionService();
    private final ReporteJasperService jasperService = new ReporteJasperService();
    private final DepartamentoService departamentoService = new DepartamentoService();
    private final AdminPanelService empleadoService = new AdminPanelService();

    @FXML
    public void initialize() {
        cmbPeriodo.setConverter(UiUtil.conversor(p -> p.getNombre() + " (" + p.getEstado() + ")"));
        cmbPeriodo.valueProperty().addListener((obs, ant, nuevo) -> generar());

        UiUtil.columna(colDepNombre, FilaDepartamento::departamento);
        UiUtil.columna(colDepEmpleados, f -> String.valueOf(f.empleados()));
        UiUtil.columna(colDepObjetivos, f -> String.valueOf(f.objetivos()));
        UiUtil.columna(colDepAvance, f -> porcentaje(f.avance()));
        UiUtil.columna(colDepFinal, f -> numero(f.evaluacionFinal()));
        UiUtil.columna(colDepEstado, FilaDepartamento::estadoEvaluacion);

        UiUtil.columna(colEmpNombre, FilaEmpleado::empleado);
        UiUtil.columna(colEmpDepartamento, FilaEmpleado::departamento);
        UiUtil.columna(colEmpObjetivos, f -> String.valueOf(f.objetivos()));
        UiUtil.columna(colEmpAvance, f -> porcentaje(f.avance()));
        UiUtil.columna(colEmpFinal, f -> numero(f.evaluacionFinal()));
        UiUtil.columna(colEmpEstado, FilaEmpleado::estadoEvaluacion);

        cmbDepartamentoReporte.setConverter(UiUtil.conversor(Departamento::getNombre));
        cmbDepartamentoReporte.setItems(FXCollections.observableArrayList(departamentoService.listarActivos()));
        cmbEmpleadoReporte.setConverter(UiUtil.conversor(e -> e.getNombreCompleto() + " (" + e.getCif() + ")"));
        cmbEmpleadoReporte.setItems(FXCollections.observableArrayList(empleadoService.listarEmpleados().stream()
                .filter(e -> Boolean.TRUE.equals(e.getActivo()))
                .toList()));

        List<PeriodoEvaluacion> periodos = periodoService.listarOrdenados();
        cmbPeriodo.setItems(FXCollections.observableArrayList(periodos));

        PeriodoEvaluacion actual = periodoService.buscarPeriodoActual();
        if (actual != null) {
            cmbPeriodo.setValue(periodos.stream()
                    .filter(p -> p.getId().equals(actual.getId())).findFirst().orElse(null));
        } else if (!periodos.isEmpty()) {
            cmbPeriodo.setValue(periodos.get(0));
        } else {
            lblEstado.setText("No hay períodos de evaluación. Créalos en la pantalla Períodos.");
        }
    }

    private static String numero(BigDecimal valor) {
        return valor == null ? "—" : valor.stripTrailingZeros().toPlainString();
    }

    private static String porcentaje(BigDecimal valor) {
        return valor == null ? "Sin objetivos" : valor.stripTrailingZeros().toPlainString() + " %";
    }

    @FXML
    private void generar() {
        PeriodoEvaluacion periodo = cmbPeriodo.getValue();
        if (periodo == null) {
            tablaDepartamentos.getItems().clear();
            tablaEmpleados.getItems().clear();
            return;
        }

        List<FilaDepartamento> deps = service.porDepartamento(periodo.getId());
        List<FilaEmpleado> emps = service.porEmpleado(periodo.getId());

        tablaDepartamentos.setItems(FXCollections.observableArrayList(deps));
        tablaEmpleados.setItems(FXCollections.observableArrayList(emps));

        lblPromedioDepartamentos.setText(porcentaje(
                ReporteService.promedio(deps.stream().map(FilaDepartamento::avance).toList())));
        lblPromedioEmpleados.setText(porcentaje(
                ReporteService.promedio(emps.stream().map(FilaEmpleado::avance).toList())));
        long evaluados = emps.stream().filter(f -> f.evaluacionFinal() != null).count();
        lblEvaluados.setText(evaluados + " de " + emps.size());

        lblEstado.setText("Reporte de " + periodo.getNombre() + " ("
                + UiUtil.fecha(periodo.getFechaInicio()) + " – " + UiUtil.fecha(periodo.getFechaFin()) + ").");
    }

    @FXML
    private void exportarCsv() {
        PeriodoEvaluacion periodo = cmbPeriodo.getValue();
        if (periodo == null) {
            UiUtil.error("Selecciona un período.");
            return;
        }

        boolean departamentos = tabs.getSelectionModel().getSelectedItem() == tabDepartamentos;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar reporte");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (Excel)", "*.csv"));
        chooser.setInitialFileName("reporte_" + (departamentos ? "departamentos_" : "empleados_")
                + periodo.getNombre().replaceAll("[^A-Za-z0-9_-]+", "_") + ".csv");

        File archivo = chooser.showSaveDialog(tabs.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        // Separador ';' y BOM UTF-8 para que Excel en español lo abra bien
        try (PrintWriter out = new PrintWriter(archivo, StandardCharsets.UTF_8)) {
            out.print('﻿');
            if (departamentos) {
                out.println("Departamento | Empleados | Objetivos | Avance (%) | Evaluación final | Estado");
                for (FilaDepartamento f : tablaDepartamentos.getItems()) {
                    out.println(String.join("|", csv(f.departamento()), String.valueOf(f.empleados()),
                            String.valueOf(f.objetivos()), csv(f.avance()), csv(f.evaluacionFinal()),
                            csv(f.estadoEvaluacion())));
                }
            } else {
                out.println("Empleado | Departamento | Objetivos | Avance (%) | Evaluación final | Estado");
                for (FilaEmpleado f : tablaEmpleados.getItems()) {
                    out.println(String.join("|", csv(f.empleado()), csv(f.departamento()),
                            String.valueOf(f.objetivos()), csv(f.avance()), csv(f.evaluacionFinal()),
                            csv(f.estadoEvaluacion())));
                }
            }
            lblEstado.setText("Reporte exportado a " + archivo.getAbsolutePath());
        } catch (IOException e) {
            UiUtil.error("No se pudo guardar el archivo: " + e.getMessage());
        }
    }

    private static String csv(Object valor) {
        if (valor == null) {
            return "";
        }
        String texto = valor instanceof BigDecimal b
                ? b.stripTrailingZeros().toPlainString().replace('.', ',')
                : valor.toString();
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    // =========================================================
    // EXPORTAR CON LAS PLANTILLAS DE JASPER (PDF Y EXCEL)
    // El service llena la plantilla y escribe el archivo; el controller
    // solo pide los datos, dónde guardar y muestra el resultado.
    // =========================================================

    @FXML private void generalPdf() { exportarGeneral(Formato.PDF); }
    @FXML private void generalExcel() { exportarGeneral(Formato.EXCEL); }
    @FXML private void departamentalPdf() { exportarDepartamental(Formato.PDF); }
    @FXML private void departamentalExcel() { exportarDepartamental(Formato.EXCEL); }
    @FXML private void individualPdf() { exportarIndividual(Formato.PDF); }
    @FXML private void individualExcel() { exportarIndividual(Formato.EXCEL); }

    private void exportarGeneral(Formato formato) {
        PeriodoEvaluacion periodo = cmbPeriodo.getValue();
        if (periodo == null) {
            UiUtil.error("Selecciona un período.");
            return;
        }
        File archivo = elegirArchivo(formato, "reporte_general_" + nombreArchivo(periodo.getNombre()));
        if (archivo != null) {
            ejecutar(archivo, "Reporte general",
                    () -> jasperService.exportarGeneral(archivo, formato, periodo));
        }
    }

    private void exportarDepartamental(Formato formato) {
        PeriodoEvaluacion periodo = cmbPeriodo.getValue();
        Departamento departamento = cmbDepartamentoReporte.getValue();
        if (periodo == null || departamento == null) {
            UiUtil.error("Selecciona el período y el departamento.");
            return;
        }
        File archivo = elegirArchivo(formato, "reporte_departamental_"
                + nombreArchivo(departamento.getNombre()) + "_" + nombreArchivo(periodo.getNombre()));
        if (archivo != null) {
            ejecutar(archivo, "Reporte departamental",
                    () -> jasperService.exportarDepartamental(archivo, formato, periodo, departamento));
        }
    }

    private void exportarIndividual(Formato formato) {
        PeriodoEvaluacion periodo = cmbPeriodo.getValue();
        Empleado empleado = cmbEmpleadoReporte.getValue();
        if (periodo == null || empleado == null) {
            UiUtil.error("Selecciona el período y el empleado.");
            return;
        }
        File archivo = elegirArchivo(formato, "reporte_individual_"
                + nombreArchivo(empleado.getNombreCompleto()) + "_" + nombreArchivo(periodo.getNombre()));
        if (archivo != null) {
            ejecutar(archivo, "Reporte individual",
                    () -> jasperService.exportarIndividual(archivo, formato, periodo, empleado));
        }
    }

    /** Acción que genera el archivo (puede tardar unos segundos la primera vez). */
    @FunctionalInterface
    private interface Generacion {
        void generar() throws Exception;
    }

    /** Genera el reporte en segundo plano para no congelar la ventana. */
    private void ejecutar(File archivo, String nombre, Generacion generacion) {
        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() throws Exception {
                generacion.generar();
                return null;
            }
        };

        panelExportar.setDisable(true);
        lblEstado.setText("Generando " + nombre.toLowerCase() + "…");

        tarea.setOnSucceeded(e -> {
            panelExportar.setDisable(false);
            lblEstado.setText(nombre + " guardado en " + archivo.getAbsolutePath());
        });
        tarea.setOnFailed(e -> {
            panelExportar.setDisable(false);
            lblEstado.setText("");
            Throwable error = tarea.getException();
            if (error != null) {
                error.printStackTrace();
            }
            UiUtil.error("No se pudo generar el reporte: "
                    + (error == null ? "error desconocido" : error.getMessage()));
        });

        Thread hilo = new Thread(tarea, "exportar-reporte");
        hilo.setDaemon(true);
        hilo.start();
    }

    /** Diálogo para elegir dónde guardar (null si se cancela). */
    private File elegirArchivo(Formato formato, String nombreSugerido) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar reporte");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(formato.descripcion, "*." + formato.extension));
        chooser.setInitialFileName(nombreSugerido + "." + formato.extension);
        return chooser.showSaveDialog(tabs.getScene().getWindow());
    }

    /** Nombre de archivo sin tildes ni espacios. */
    private static String nombreArchivo(String texto) {
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9_-]+", "_");
    }
}
