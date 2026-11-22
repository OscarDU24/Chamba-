package org.example.projectofinal.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.projectofinal.model.CuentaUsuario;
import org.example.projectofinal.service.LoginService;
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

    // Opciones del menú que solo ve el ADMINISTRADOR
    @FXML
    private VBox seccionAdministracion;

    @FXML
    private Button btnPeriodos;

    @FXML
    private Button btnObjetivos;

    @FXML
    private Button btnFormularios;

    @FXML
    private Button btnReportes;

    private final LoginService loginService = new LoginService();

    private boolean esAdministrador;

    @FXML
    public void initialize() {

        CuentaUsuario cuenta = SesionUsuario.getCuentaUsuario();

        esAdministrador = cuenta != null && "ADMINISTRADOR".equals(cuenta.getRol());

        if (cuenta != null) {
            lblUsuario.setText(cuenta.getIdEmpleado().getNombreCompleto());
            lblRol.setText(cuenta.getRol());
        } else {
            lblUsuario.setText("Sin sesión");
            lblRol.setText("");
        }

        aplicarPermisos();
        mostrarDashboard();
    }

    // =========================================================
    // PERMISOS POR ROL
    // ADMINISTRADOR: todo el menú.
    // EMPLEADO: Dashboard, Seguimientos (los suyos) y Evaluación final (sus resultados).
    // =========================================================

    private void aplicarPermisos() {

        for (Node opcion : new Node[]{seccionAdministracion, btnPeriodos, btnObjetivos, btnFormularios, btnReportes}) {
            opcion.setVisible(esAdministrador);
            opcion.setManaged(esAdministrador);
        }
    }

    /** Segunda barrera: aunque el botón esté oculto, no abre pantallas de administración. */
    private boolean permitirSoloAdministrador() {

        if (!esAdministrador) {
            mostrarAlerta(
                    "Acceso restringido",
                    "Esta opción solo está disponible para administradores."
            );
        }
        return esAdministrador;
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

        if (!permitirSoloAdministrador()) return;

        cargarVista(
                "/org/example/projectofinal/adminpanel.fxml",
                "Gestión de empleados"
        );
    }

    @FXML
    private void mostrarDepartamentos() {

        if (!permitirSoloAdministrador()) return;

        cargarVista(
                "/org/example/projectofinal/view/departamentos.fxml",
                "Departamentos"
        );
    }

    @FXML
    private void mostrarCategorias() {

        if (!permitirSoloAdministrador()) return;

        cargarVista(
                "/org/example/projectofinal/view/categorias.fxml",
                "Categorías"
        );
    }

    @FXML
    private void mostrarUsuarios() {

        if (!permitirSoloAdministrador()) return;

        cargarVista(
                "/org/example/projectofinal/view/usuarios.fxml",
                "Usuarios"
        );
    }

    @FXML
    private void mostrarSupervisiones() {

        if (!permitirSoloAdministrador()) return;

        cargarVista(
                "/org/example/projectofinal/view/supervisiones.fxml",
                "Supervisiones"
        );
    }

    @FXML
    private void mostrarPeriodos() {

        if (!permitirSoloAdministrador()) return;

        cargarVista(
                "/org/example/projectofinal/view/periodos.fxml",
                "Períodos de evaluación"
        );
    }

    @FXML
    private void mostrarObjetivos() {

        if (!permitirSoloAdministrador()) return;

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

        if (!permitirSoloAdministrador()) return;

        cargarVista(
                "/org/example/projectofinal/view/evaluaciones/GestionEvaluaciones.fxml",
                "Formularios de evaluación"
        );
    }

    @FXML
    private void mostrarEvaluacionFinal() {

        cargarVista(
                "/org/example/projectofinal/view/evaluacionFinal.fxml",
                "Evaluación final"
        );
    }

    @FXML
    private void mostrarReportes() {

        if (!permitirSoloAdministrador()) return;

        cargarVista(
                "/org/example/projectofinal/view/reportes.fxml",
                "Reportes"
        );
    }

    // =========================================================
    // CERRAR SESION: vuelve a la pantalla de login
    // =========================================================

    @FXML
    private void cerrarSesion() {

        loginService.cerrarSesion();

        try {
            Parent login = new FXMLLoader(
                    getClass().getResource("/org/example/projectofinal/loginUsuario.fxml")
            ).load();

            Stage stage = (Stage) contenedorPrincipal.getScene().getWindow();
            stage.setScene(new Scene(login, 900, 600));
            stage.setTitle("Iniciar sesión");
            stage.centerOnScreen();

        } catch (IOException | RuntimeException e) {

            mostrarAlerta(
                    "Error",
                    "No se pudo volver a la pantalla de inicio de sesión: " + e.getMessage()
            );
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}
