package org.example.projectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.example.projectofinal.model.PeriodoEvaluacion;
import org.example.projectofinal.service.PeriodoEvaluacionService;
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

    private final ReporteService service = new ReporteService();
    private final PeriodoEvaluacionService periodoService = new PeriodoEvaluacionService();

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
                out.println("Departamento;Empleados;Objetivos;Avance (%);Evaluación final;Estado");
                for (FilaDepartamento f : tablaDepartamentos.getItems()) {
                    out.println(String.join(";", csv(f.departamento()), String.valueOf(f.empleados()),
                            String.valueOf(f.objetivos()), csv(f.avance()), csv(f.evaluacionFinal()),
                            csv(f.estadoEvaluacion())));
                }
            } else {
                out.println("Empleado;Departamento;Objetivos;Avance (%);Evaluación final;Estado");
                for (FilaEmpleado f : tablaEmpleados.getItems()) {
                    out.println(String.join(";", csv(f.empleado()), csv(f.departamento()),
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
}
