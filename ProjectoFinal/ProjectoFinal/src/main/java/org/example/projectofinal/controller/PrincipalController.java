package org.example.projectofinal.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.application.Platform;

import java.io.IOException;

public class PrincipalController {

    @FXML
    private StackPane contenedorPrincipal;

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblRol;

    @FXML
    public void initialize() {

        lblUsuario.setText("Administrador de prueba");
        lblRol.setText("ADMINISTRADOR");

        mostrarDashboard();
    }

    private void cargarVista(String ruta, String titulo) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(ruta)
            );

            Parent vista = loader.load();

            contenedorPrincipal.getChildren().setAll(vista);

            lblTitulo.setText(titulo);

        } catch (IOException | RuntimeException e) {

            mostrarAlerta(
                    "Error de navegación",
                    "No se pudo cargar la pantalla: "
                            + titulo
                            + "\n\n"
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void mostrarDashboard() {

        cargarVista(
                "/org/example/projectofinal/dashboard.fxml",
                "Dashboard"
        );
    }

    @FXML
    private void mostrarEmpleados() {

        cargarVista(
                "/org/example/projectofinal/adminpanel.fxml",
                "Gestión de empleados"
        );
    }

    @FXML
    private void mostrarDepartamentos() {
        mostrarModuloPendiente("Departamentos");
    }

    @FXML
    private void mostrarCategorias() {
        mostrarModuloPendiente("Categorías");
    }

    @FXML
    private void mostrarUsuarios() {
        mostrarModuloPendiente("Usuarios");
    }

    @FXML
    private void mostrarSupervisiones() {
        mostrarModuloPendiente("Supervisiones");
    }

    @FXML
    private void mostrarPeriodos() {
        mostrarModuloPendiente("Períodos");
    }

    @FXML
    private void mostrarObjetivos() {
        mostrarModuloPendiente("Objetivos");
    }

    @FXML
    private void mostrarSeguimientos() {
        mostrarModuloPendiente("Seguimientos");
    }

    @FXML
    private void mostrarEvaluaciones() {

        cargarVista(
                "/org/example/projectofinal/view/evaluaciones/GestionEvaluaciones.fxml",
                "Evaluaciones"
        );
    }

    @FXML
    private void mostrarReportes() {
        mostrarModuloPendiente("Reportes");
    }

    private void mostrarModuloPendiente(String modulo) {

        mostrarAlerta(
                "Módulo pendiente",
                "El módulo " + modulo
                        + " todavía no está implementado."
        );
    }

    @FXML
    private void cerrarSesion() {

        Platform.exit();
    }

    private void mostrarAlerta(String titulo, String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}
