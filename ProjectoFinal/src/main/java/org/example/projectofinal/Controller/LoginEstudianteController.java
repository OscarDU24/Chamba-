package org.example.projectofinal.Controller;

import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class LoginEstudianteController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    @FXML
    private TextField Correotxt;

    @FXML
    private PasswordField Passwordtxt;

    @FXML
    private PasswordField ConfirmPasswordtxt;

    @FXML
    private Label feedbackLbl;

    @FXML
    public void onLogin() {
        String correo = obtenerTexto(Correotxt);
        String password = obtenerTexto(Passwordtxt);
        String confirmPassword = obtenerTexto(ConfirmPasswordtxt);

        if (correo.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            mostrarMensaje("Completa todos los campos.", true);
            return;
        }

        if (!EMAIL_PATTERN.matcher(correo).matches()) {
            mostrarMensaje("Ingresa un correo válido.", true);
            return;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            mostrarMensaje("La contraseña debe tener mínimo 8 caracteres, incluir una minúscula, una mayúscula, un número y un carácter especial.", true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            mostrarMensaje("Las contraseñas no coinciden.", true);
            return;
        }

        mostrarMensaje("Datos válidos. Puedes continuar.", false);
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
