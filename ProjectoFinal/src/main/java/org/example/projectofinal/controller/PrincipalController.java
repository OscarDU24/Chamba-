package org.example.projectofinal.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.application.Platform;
import org.example.projectofinal.model.CuentaUsuario;
import org.example.projectofinal.util.SesionUsuario;

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

        CuentaUsuario cuenta = SesionUsuario.getCuentaUsuario();

        if (cuenta != null) {
            lblUsuario.setText(cuenta.getIdEmpleado().getNombreCompleto());
            lblRol.setText(cuenta.getRol());
        } else {
            lblUsuario.setText("Sin sesión");
            lblRol.setText("");
        }

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

        cargarVista(
                "/org/example/projectofinal/view/departamentos.fxml",
                "Departamentos"
        );
    }

    @FXML
    private void mostrarCategorias() {

        cargarVista(
                "/org/example/projectofinal/view/categorias.fxml",
                "Categorías"
        );
    }

    @FXML
    private void mostrarUsuarios() {

        cargarVista(
                "/org/example/projectofinal/view/usuarios.fxml",
                "Usuarios"
        );
    }

    @FXML
    private void mostrarSupervisiones() {

        cargarVista(
                "/org/example/projectofinal/view/supervisiones.fxml",
                "Supervisiones"
        );
    }

    @FXML
    private void mostrarPeriodos() {

        cargarVista(
                "/org/example/projectofinal/view/periodos.fxml",
                "Períodos de evaluación"
        );
    }

    @FXML
    private void mostrarObjetivos() {

        cargarVista(
                "/org/example/projectofinal/view/objetivos.fxml",
                "Objetivos"
        );
    }

    @FXML
    private void mostrarSeguimientos() {

        cargarVista(
                "/org/example/projectofinal/view/seguimientos.fxml",
                "Seguimientos"
        );
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
