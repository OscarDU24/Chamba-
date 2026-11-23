package org.example.projectofinal.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import org.example.projectofinal.model.CategoriaPersonal;
import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.service.CategoriaPersonalService;
import org.example.projectofinal.service.DepartamentoService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CategoriasController {

    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private Button btnGuardar;

    @FXML private TableView<CategoriaPersonal> tabla;
    @FXML private TableColumn<CategoriaPersonal, String> colNombre;
    @FXML private TableColumn<CategoriaPersonal, String> colDescripcion;
    @FXML private TableColumn<CategoriaPersonal, String> colEstado;
    @FXML private TableColumn<CategoriaPersonal, String> colDepartamentos;
    @FXML private Label lblEstado;

    // Departamentos permitidos para la categoría seleccionada
    @FXML private Label lblDepartamentos;
    @FXML private ListView<Departamento> listaDepartamentos;

    private final CategoriaPersonalService service = new CategoriaPersonalService();
    private final DepartamentoService departamentoService = new DepartamentoService();
    private CategoriaPersonal seleccionada;

    private final Map<UUID, BooleanProperty> marcados = new HashMap<>();
    private Map<UUID, Set<UUID>> reglas = Map.of();

    @FXML
    public void initialize() {
        UiUtil.columna(colNombre, CategoriaPersonal::getNombre);
        UiUtil.columna(colDescripcion, CategoriaPersonal::getDescripcion);
        UiUtil.columna(colEstado, c -> UiUtil.estado(c.getActivo()));
        UiUtil.columna(colDepartamentos, c -> {
            Set<UUID> ids = reglas.getOrDefault(c.getId(), Set.of());
            return ids.isEmpty() ? "Cualquiera" : ids.size() + " departamento(s)";
        });

        listaDepartamentos.setCellFactory(CheckBoxListCell.forListView(
                d -> marcados.computeIfAbsent(d.getId(), id -> new SimpleBooleanProperty(false)),
                UiUtil.conversor(Departamento::getNombre)));

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, ant, nueva) -> {
            if (nueva != null) {
                seleccionada = nueva;
                txtNombre.setText(UiUtil.valor(nueva.getNombre()));
                txtDescripcion.setText(UiUtil.valor(nueva.getDescripcion()));
                btnGuardar.setText("Guardar cambios");
                cargarDepartamentosPermitidos(nueva);
            }
        });

        actualizarTabla();
    }

    @FXML
    private void actualizarTabla() {
        List<CategoriaPersonal> categorias = service.listarOrdenadas();

        Map<UUID, Set<UUID>> nuevasReglas = new HashMap<>();
        for (CategoriaPersonal c : categorias) {
            nuevasReglas.put(c.getId(), service.departamentosPermitidos(c.getId()));
        }
        reglas = nuevasReglas;

        listaDepartamentos.setItems(FXCollections.observableArrayList(departamentoService.listarActivos()));
        tabla.setItems(FXCollections.observableArrayList(categorias));
        tabla.refresh();
        lblEstado.setText(categorias.size() + " categoría(s) registradas.");

        if (seleccionada != null) {
            cargarDepartamentosPermitidos(seleccionada);
        } else {
            limpiarDepartamentos();
        }
    }

    private void cargarDepartamentosPermitidos(CategoriaPersonal c) {
        Set<UUID> permitidos = reglas.getOrDefault(c.getId(), Set.of());
        for (Departamento d : listaDepartamentos.getItems()) {
            marcados.computeIfAbsent(d.getId(), id -> new SimpleBooleanProperty(false))
                    .set(permitidos.contains(d.getId()));
        }
        listaDepartamentos.setDisable(false);
        lblDepartamentos.setText("Departamentos permitidos para " + c.getNombre()
                + " (sin ninguno marcado = puede ir en cualquier departamento):");
    }

    private void limpiarDepartamentos() {
        marcados.values().forEach(p -> p.set(false));
        listaDepartamentos.setDisable(true);
        lblDepartamentos.setText("Selecciona una categoría en la tabla para ver sus departamentos permitidos.");
    }

    @FXML
    private void guardarDepartamentos() {
        if (seleccionada == null) {
            UiUtil.error("Selecciona una categoría de la tabla.");
            return;
        }

        Set<UUID> ids = listaDepartamentos.getItems().stream()
                .map(Departamento::getId)
                .filter(id -> marcados.containsKey(id) && marcados.get(id).get())
                .collect(Collectors.toSet());

        String error = service.guardarDepartamentosPermitidos(seleccionada.getId(), ids);
        if (error != null) {
            UiUtil.error(error);
            return;
        }

        actualizarTabla();
        lblEstado.setText("Departamentos permitidos de " + seleccionada.getNombre() + " actualizados.");
    }

    @FXML
    private void limpiarFormulario() {
        seleccionada = null;
        txtNombre.clear();
        txtDescripcion.clear();
        btnGuardar.setText("Registrar categoría");
        tabla.getSelectionModel().clearSelection();
        limpiarDepartamentos();
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
