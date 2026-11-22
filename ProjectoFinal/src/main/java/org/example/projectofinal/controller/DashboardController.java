package org.example.projectofinal.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.service.AdminPanelService;

public class DashboardController {

    @FXML
    private Label lblTotalEmpleados;

    @FXML
    private Label lblTotalDepartamentos;

    @FXML
    private Label lblTotalObjetivos;

    @FXML
    private Label lblTotalEvaluaciones;

    @FXML
    private Label lblPeriodoActual;

    @FXML
    private Label lblFechasPeriodo;

    @FXML
    private Label lblEstadoPeriodo;

    @FXML
    private Label lblObjetivosAsignados;

    @FXML
    private Label lblObjetivosSeguimiento;

    @FXML
    private Label lblObjetivosCumplidos;

    @FXML
    private TableView<?> tablaActividad;

    @FXML
    private TableColumn<?, ?> colFecha;

    @FXML
    private TableColumn<?, ?> colActividad;

    @FXML
    private TableColumn<?, ?> colResponsable;

    @FXML
    private TableColumn<?, ?> colEstado;

    private final AdminPanelService service =
            new AdminPanelService();

    @FXML
    public void initialize() {

        cargarIndicadores();
        cargarPeriodo();
        cargarResumenObjetivos();
        configurarActividad();
    }

    private void cargarIndicadores() {

        try {

            var empleados = service.listarEmpleados();

            long totalActivos = empleados.stream()
                    .filter(e -> Boolean.TRUE.equals(e.getActivo()))
                    .count();

            int totalDepartamentos =
                    service.listarDepartamentos().size();

            lblTotalEmpleados.setText(
                    String.valueOf(totalActivos)
            );

            lblTotalDepartamentos.setText(
                    String.valueOf(totalDepartamentos)
            );

            lblTotalObjetivos.setText("—");
            lblTotalEvaluaciones.setText("—");

        } catch (Exception e) {

            lblTotalEmpleados.setText("Error");
            lblTotalDepartamentos.setText("Error");

            lblTotalObjetivos.setText("—");
            lblTotalEvaluaciones.setText("—");

            e.printStackTrace();
        }
    }

    private void cargarPeriodo() {

        lblPeriodoActual.setText(
                "No hay un período configurado"
        );

        lblFechasPeriodo.setText(
                "Sin fechas disponibles"
        );

        lblEstadoPeriodo.setText(
                "Pendiente"
        );
    }

    private void cargarResumenObjetivos() {

        lblObjetivosAsignados.setText("—");

        lblObjetivosSeguimiento.setText("—");

        lblObjetivosCumplidos.setText("—");
    }

    private void configurarActividad() {

        tablaActividad.setPlaceholder(
                new Label("No hay actividades registradas.")
        );
    }
}
