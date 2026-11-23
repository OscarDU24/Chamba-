package org.example.projectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.projectofinal.model.CuentaUsuario;
import org.example.projectofinal.service.CuentaUsuarioService;
import org.example.projectofinal.util.SesionUsuario;

import java.util.Optional;

public class UsuariosController {

    @FXML private TableView<CuentaUsuario> tabla;
    @FXML private TableColumn<CuentaUsuario, String> colNombre;
    @FXML private TableColumn<CuentaUsuario, String> colCorreo;
    @FXML private TableColumn<CuentaUsuario, String> colRol;
    @FXML private TableColumn<CuentaUsuario, String> colEstado;
    @FXML private Label lblEstado;

    private final CuentaUsuarioService service = new CuentaUsuarioService();

    @FXML
    public void initialize() {
        UiUtil.columna(colNombre, c -> c.getIdEmpleado().getNombreCompleto());
        UiUtil.columna(colCorreo, c -> c.getIdEmpleado().getCorreoInstitucional());
        UiUtil.columna(colRol, CuentaUsuario::getRol);
        UiUtil.columna(colEstado, c -> !Boolean.TRUE.equals(c.getIdEmpleado().getActivo())
                ? "Empleado inactivo" : UiUtil.estado(c.getActivo()));

        actualizarTabla();
    }

    @FXML
    private void actualizarTabla() {
        tabla.setItems(FXCollections.observableArrayList(service.listar()));
        lblEstado.setText(tabla.getItems().size() + " cuenta(s) de usuario.");
    }

    private CuentaUsuario seleccionada() {
        CuentaUsuario c = tabla.getSelectionModel().getSelectedItem();
        if (c == null) {
            UiUtil.error("Selecciona una cuenta de la tabla.");
        }
        return c;
    }

    private boolean esCuentaPropia(CuentaUsuario c) {
        CuentaUsuario actual = SesionUsuario.getCuentaUsuario();
        return actual != null && actual.getId().equals(c.getId());
    }

    @FXML
    private void cambiarEstado() {
        CuentaUsuario c = seleccionada();
        if (c == null) {
            return;
        }
        if (esCuentaPropia(c)) {
            UiUtil.error("No puedes desactivar tu propia cuenta.");
            return;
        }
        if (!Boolean.TRUE.equals(c.getIdEmpleado().getActivo())) {
            UiUtil.error("El empleado está inactivo; actívalo primero en Gestión de empleados.");
            return;
        }

        String accion = Boolean.TRUE.equals(c.getActivo()) ? "desactivar" : "activar";
        if (!UiUtil.confirmar("¿Deseas " + accion + " la cuenta de " + c.getIdEmpleado().getNombreCompleto() + "?")) {
            return;
        }

        if (service.cambiarEstado(c.getIdEmpleado().getId())) {
            actualizarTabla();
            lblEstado.setText("Estado de la cuenta actualizado.");
        } else {
            UiUtil.error("No se pudo cambiar el estado de la cuenta.");
        }
    }

    @FXML
    private void cambiarRol() {
        CuentaUsuario c = seleccionada();
        if (c == null) {
            return;
        }
        if (esCuentaPropia(c)) {
            UiUtil.error("No puedes cambiar el rol de tu propia cuenta.");
            return;
        }

        String nuevoRol = CuentaUsuarioService.ADMINISTRADOR.equals(c.getRol())
                ? CuentaUsuarioService.EMPLEADO : CuentaUsuarioService.ADMINISTRADOR;
        if (!UiUtil.confirmar("¿Cambiar el rol de " + c.getIdEmpleado().getNombreCompleto()
                + " a " + nuevoRol + "?")) {
            return;
        }

        if (service.cambiarRol(c.getId())) {
            actualizarTabla();
            lblEstado.setText("Rol actualizado a " + nuevoRol + ".");
        } else {
            UiUtil.error("No se pudo cambiar el rol.");
        }
    }

    @FXML
    private void eliminarCuenta() {
        CuentaUsuario c = seleccionada();
        if (c == null) {
            return;
        }
        if (esCuentaPropia(c)) {
            UiUtil.error("No puedes eliminar la cuenta con la que iniciaste sesión.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Seguro que deseas eliminar la cuenta de "
                + c.getIdEmpleado().getNombreCompleto() + "?");
        confirmacion.setContentText("La persona ya no podrá iniciar sesión. El empleado y su historial se conservan."
                + "\nEsta acción no se puede deshacer; para darle acceso otra vez, créale una cuenta nueva"
                + " escribiendo una contraseña al editarlo en Gestión de empleados.");

        ButtonType eliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(cancelar, eliminar);
        ((Button) confirmacion.getDialogPane().lookupButton(eliminar)).setDefaultButton(false);
        ((Button) confirmacion.getDialogPane().lookupButton(cancelar)).setDefaultButton(true);

        if (confirmacion.showAndWait().orElse(cancelar) != eliminar) {
            return;
        }

        if (service.eliminarCuenta(c.getId())) {
            actualizarTabla();
            lblEstado.setText("Cuenta de " + c.getIdEmpleado().getNombreCompleto() + " eliminada.");
        } else {
            UiUtil.error("No se pudo eliminar la cuenta.");
        }
    }

    @FXML
    private void restablecerContrasena() {
        CuentaUsuario c = seleccionada();
        if (c == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Restablecer contraseña");
        dialog.setHeaderText("Nueva contraseña para " + c.getIdEmpleado().getNombreCompleto());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        PasswordField nueva = new PasswordField();
        nueva.setPromptText("Mín. 8: mayúscula, minúscula, número y signo");
        PasswordField confirmar = new PasswordField();
        confirmar.setPromptText("Confirmar contraseña");
        dialog.getDialogPane().setContent(new VBox(10, nueva, confirmar));

        Optional<ButtonType> respuesta = dialog.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        String error = service.restablecerContrasena(
                c.getIdEmpleado().getId(), nueva.getText(), confirmar.getText());

        if (error == null) {
            UiUtil.info("Contraseña restablecida.");
        } else {
            UiUtil.error(error);
        }
    }
}
