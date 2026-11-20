package org.example.projectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.projectofinal.model.CategoriaPersonal;
import org.example.projectofinal.service.CategoriaPersonalService;

public class CategoriasController {

    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private Button btnGuardar;

    @FXML private TableView<CategoriaPersonal> tabla;
    @FXML private TableColumn<CategoriaPersonal, String> colNombre;
    @FXML private TableColumn<CategoriaPersonal, String> colDescripcion;
    @FXML private TableColumn<CategoriaPersonal, String> colEstado;
    @FXML private Label lblEstado;

    private final CategoriaPersonalService service = new CategoriaPersonalService();
    private CategoriaPersonal seleccionada;

    @FXML
    public void initialize() {
        UiUtil.columna(colNombre, CategoriaPersonal::getNombre);
        UiUtil.columna(colDescripcion, CategoriaPersonal::getDescripcion);
        UiUtil.columna(colEstado, c -> UiUtil.estado(c.getActivo()));

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, ant, nueva) -> {
            if (nueva != null) {
                seleccionada = nueva;
                txtNombre.setText(UiUtil.valor(nueva.getNombre()));
                txtDescripcion.setText(UiUtil.valor(nueva.getDescripcion()));
                btnGuardar.setText("Guardar cambios");
            }
        });

        actualizarTabla();
    }

    @FXML
    private void actualizarTabla() {
        tabla.setItems(FXCollections.observableArrayList(service.listarOrdenadas()));
        lblEstado.setText(tabla.getItems().size() + " categoría(s) registradas.");
    }

    @FXML
    private void limpiarFormulario() {
        seleccionada = null;
        txtNombre.clear();
        txtDescripcion.clear();
        btnGuardar.setText("Registrar categoría");
        tabla.getSelectionModel().clearSelection();
    }

    @FXML
    private void guardar() {
        String nombre = UiUtil.texto(txtNombre);

        if (nombre == null) {
            UiUtil.error("Escribe el nombre de la categoría.");
            return;
        }
        if (nombre.length() > 50) {
            UiUtil.error("El nombre no puede tener más de 50 caracteres.");
            return;
        }
        if (service.existeNombre(nombre, seleccionada == null ? null : seleccionada.getId())) {
            UiUtil.error("Ya existe una categoría con ese nombre.");
            return;
        }

        CategoriaPersonal c = seleccionada == null ? new CategoriaPersonal() : seleccionada;
        c.setNombre(nombre);
        c.setDescripcion(UiUtil.texto(txtDescripcion));

        boolean nueva = seleccionada == null;
        boolean ok = nueva ? service.registrar(c) : service.modificar(c);

        if (!ok) {
            UiUtil.error("No se pudo guardar la categoría.");
            actualizarTabla();
            return;
        }

        limpiarFormulario();
        actualizarTabla();
        lblEstado.setText(nueva ? "Categoría registrada." : "Categoría actualizada.");
    }

    @FXML
    private void cambiarEstado() {
        CategoriaPersonal c = tabla.getSelectionModel().getSelectedItem();
        if (c == null) {
            UiUtil.error("Selecciona una categoría de la tabla.");
            return;
        }

        String accion = Boolean.TRUE.equals(c.getActivo()) ? "desactivar" : "activar";
        if (!UiUtil.confirmar("¿Deseas " + accion + " la categoría " + c.getNombre() + "?")) {
            return;
        }

        if (service.cambiarEstado(c.getId())) {
            limpiarFormulario();
            actualizarTabla();
            lblEstado.setText("Estado de la categoría actualizado.");
        } else {
            UiUtil.error("No se pudo cambiar el estado.");
        }
    }
}
