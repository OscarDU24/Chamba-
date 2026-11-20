package org.example.projectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.service.DepartamentoService;

import java.util.regex.Pattern;

public class DepartamentosController {

    private static final Pattern CORREO = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    @FXML private TextField txtNombre;
    @FXML private TextField txtUbicacion;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextArea txtDescripcion;
    @FXML private Button btnGuardar;

    @FXML private TableView<Departamento> tabla;
    @FXML private TableColumn<Departamento, String> colNombre;
    @FXML private TableColumn<Departamento, String> colUbicacion;
    @FXML private TableColumn<Departamento, String> colCorreo;
    @FXML private TableColumn<Departamento, String> colTelefono;
    @FXML private TableColumn<Departamento, String> colEstado;
    @FXML private Label lblEstado;

    private final DepartamentoService service = new DepartamentoService();
    private Departamento seleccionado;

    @FXML
    public void initialize() {
        UiUtil.columna(colNombre, Departamento::getNombre);
        UiUtil.columna(colUbicacion, Departamento::getUbicacion);
        UiUtil.columna(colCorreo, Departamento::getCorreoContacto);
        UiUtil.columna(colTelefono, Departamento::getTelefono);
        UiUtil.columna(colEstado, d -> UiUtil.estado(d.getActivo()));

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, ant, nuevo) -> {
            if (nuevo != null) {
                cargarFormulario(nuevo);
            }
        });

        actualizarTabla();
    }

    @FXML
    private void actualizarTabla() {
        tabla.setItems(FXCollections.observableArrayList(service.listarOrdenados()));
        lblEstado.setText(tabla.getItems().size() + " departamento(s) registrados.");
    }

    private void cargarFormulario(Departamento d) {
        seleccionado = d;
        txtNombre.setText(UiUtil.valor(d.getNombre()));
        txtUbicacion.setText(UiUtil.valor(d.getUbicacion()));
        txtCorreo.setText(UiUtil.valor(d.getCorreoContacto()));
        txtTelefono.setText(UiUtil.valor(d.getTelefono()));
        txtDescripcion.setText(UiUtil.valor(d.getDescripcion()));
        btnGuardar.setText("Guardar cambios");
    }

    @FXML
    private void limpiarFormulario() {
        seleccionado = null;
        txtNombre.clear();
        txtUbicacion.clear();
        txtCorreo.clear();
        txtTelefono.clear();
        txtDescripcion.clear();
        btnGuardar.setText("Registrar departamento");
        tabla.getSelectionModel().clearSelection();
    }

    @FXML
    private void guardar() {
        String nombre = UiUtil.texto(txtNombre);
        String correo = UiUtil.texto(txtCorreo);
        String telefono = UiUtil.texto(txtTelefono);

        if (nombre == null) {
            UiUtil.error("Escribe el nombre del departamento.");
            return;
        }
        if (nombre.length() > 100) {
            UiUtil.error("El nombre no puede tener más de 100 caracteres.");
            return;
        }
        if (correo != null && !CORREO.matcher(correo).matches()) {
            UiUtil.error("El correo de contacto no es válido.");
            return;
        }
        if (telefono != null && telefono.length() > 20) {
            UiUtil.error("El teléfono no puede tener más de 20 caracteres.");
            return;
        }
        if (service.existeNombre(nombre, seleccionado == null ? null : seleccionado.getId())) {
            UiUtil.error("Ya existe un departamento con ese nombre.");
            return;
        }

        Departamento d = seleccionado == null ? new Departamento() : seleccionado;
        d.setNombre(nombre);
        d.setUbicacion(UiUtil.texto(txtUbicacion));
        d.setCorreoContacto(correo);
        d.setTelefono(telefono);
        d.setDescripcion(UiUtil.texto(txtDescripcion));

        boolean nuevo = seleccionado == null;
        boolean ok = nuevo ? service.registrar(d) : service.modificar(d);

        if (!ok) {
            UiUtil.error("No se pudo guardar el departamento.");
            actualizarTabla();
            return;
        }

        limpiarFormulario();
        actualizarTabla();
        lblEstado.setText(nuevo ? "Departamento registrado." : "Departamento actualizado.");
    }

    @FXML
    private void cambiarEstado() {
        Departamento d = tabla.getSelectionModel().getSelectedItem();
        if (d == null) {
            UiUtil.error("Selecciona un departamento de la tabla.");
            return;
        }

        String accion = Boolean.TRUE.equals(d.getActivo()) ? "desactivar" : "activar";
        if (!UiUtil.confirmar("¿Deseas " + accion + " el departamento " + d.getNombre() + "?")) {
            return;
        }

        if (service.cambiarEstado(d.getId())) {
            limpiarFormulario();
            actualizarTabla();
            lblEstado.setText("Estado del departamento actualizado.");
        } else {
            UiUtil.error("No se pudo cambiar el estado.");
        }
    }
}
