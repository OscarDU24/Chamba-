package org.example.projectofinal.controller;

import java.io.IOException;
import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.example.projectofinal.model.CuentaUsuario;
import org.example.projectofinal.service.LoginService;

public class LoginEstudianteController {

    private final LoginService loginService = new LoginService();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    @FXML
    private TextField Correotxt;

    @FXML
    private PasswordField Passwordtxt;

    @FXML
    private Label feedbackLbl;

    @FXML
    public void onLogin() {
        String correo = obtenerTexto(Correotxt);
        String password = obtenerTexto(Passwordtxt);

        if (correo.isBlank() || password.isBlank()) {
            mostrarMensaje("Completa todos los campos.", true);
            return;
        }

        if (!EMAIL_PATTERN.matcher(correo).matches()) {
            mostrarMensaje("Ingresa un correo válido.", true);
            return;
        }

        CuentaUsuario cuenta = loginService.iniciarSesion(correo, password);

        if (cuenta == null) {
            mostrarMensaje("Correo o contraseña incorrectos, o la cuenta está inactiva.", true);
            return;
        }

        mostrarMensaje("Bienvenido, " + cuenta.getIdEmpleado().getNombreCompleto() + ".", false);
        abrirPrincipal();
    }

    private void abrirPrincipal() {
        try {
            Parent root = new FXMLLoader(
                    getClass().getResource("/org/example/projectofinal/principal.fxml")).load();

            Stage stage = (Stage) Correotxt.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
            stage.setTitle("Sistema de evaluación");
            stage.centerOnScreen();

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            mostrarMensaje("Sesión iniciada, pero no se pudo abrir la pantalla principal: "
                    + e.getMessage(), true);
        }
    }

    private String obtenerTexto(TextField campo) {
        if (campo == null || campo.getText() == null) {
            return "";
        }
        return campo.getText().trim();
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        if (feedbackLbl == null) {
            return;
        }

        feedbackLbl.setText(mensaje);
        feedbackLbl.setTextFill(error ? Color.web("#b42318") : Color.web("#15803d"));
        feedbackLbl.setStyle("-fx-font-size: 13px;");
    }
}
