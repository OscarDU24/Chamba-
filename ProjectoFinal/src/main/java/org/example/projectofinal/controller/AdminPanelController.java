
package org.example.projectofinal.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.projectofinal.model.CategoriaPersonal;
import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.service.AdminPanelService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdminPanelController {

    @FXML private TextField txtNombreCompleto;
    @FXML private TextField txtCif;
    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtContrasena;

    @FXML private ComboBox<CategoriaPersonal> cmbCategoria;
    @FXML private ComboBox<Departamento> cmbDepartamento;
    @FXML private ComboBox<Departamento> cmbFiltroDepartamento;

    @FXML private TextField txtBuscar;

    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colCif;
    @FXML private TableColumn<Empleado, String> colCorreo;
    @FXML private TableColumn<Empleado, String> colCategoria;
    @FXML private TableColumn<Empleado, String> colDepartamento;
    @FXML private TableColumn<Empleado, String> colEstado;

    @FXML private Label lblEstado;
    @FXML private Button btnGuardar;

    private final AdminPanelService service = new AdminPanelService();

    private final ObservableList<Empleado> empleados =
            FXCollections.observableArrayList();

    private Map<UUID, Boolean> estadosCuentas;

    private Empleado empleadoSeleccionado;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarCombos();
        actualizarTabla();

        txtBuscar.textProperty().addListener(
                (observable, anterior, nuevo) -> filtrarEmpleados()
        );

        cmbFiltroDepartamento.setOnAction(
                event -> filtrarEmpleados()
        );

        tablaEmpleados.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionado) ->
                        empleadoSeleccionado = seleccionado
                );
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getNombreCompleto()
                )
        );

        colCif.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCif()
                )
        );

        colCorreo.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCorreoInstitucional()
                )
        );

        colCategoria.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getIdCategoria() != null
                                ? data.getValue().getIdCategoria().getNombre()
                                : ""
                )
        );

        colDepartamento.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getIdDepartamento() != null
                                ? data.getValue().getIdDepartamento().getNombre()
                                : "Sin departamento"
                )
        );

        colEstado.setCellValueFactory(data -> {
            Boolean activo = estadosCuentas == null
                    ? null
                    : estadosCuentas.get(data.getValue().getId());

            return new SimpleStringProperty(
                    Boolean.TRUE.equals(activo) ? "Activa" : "Inactiva"
            );
        });

        tablaEmpleados.setItems(empleados);
    }

    private void cargarCombos() {
        cmbCategoria.setItems(
                FXCollections.observableArrayList(
                        service.listarCategorias()
                )
        );

        cmbDepartamento.setItems(
                FXCollections.observableArrayList(
                        service.listarDepartamentos()
                )
        );

        cmbFiltroDepartamento.setItems(
                FXCollections.observableArrayList(
                        service.listarDepartamentos()
                )
        );

        configurarPresentacionComboCategoria();
        configurarPresentacionComboDepartamento();
        configurarPresentacionFiltroDepartamento();
    }

    private void configurarPresentacionComboCategoria() {
        cmbCategoria.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(CategoriaPersonal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        cmbCategoria.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CategoriaPersonal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
    }

    private void configurarPresentacionComboDepartamento() {
        cmbDepartamento.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Departamento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        cmbDepartamento.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Departamento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
    }

    private void configurarPresentacionFiltroDepartamento() {
        cmbFiltroDepartamento.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Departamento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        cmbFiltroDepartamento.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Departamento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? "Todos los departamentos"
                        : item.getNombre());
            }
        });
    }

    @FXML
    public void guardarEmpleado() {
        if (!validarFormulario()) {
            return;
        }

        Empleado empleado = empleadoSeleccionado == null
                ? new Empleado()
                : empleadoSeleccionado;

        empleado.setNombreCompleto(txtNombreCompleto.getText().trim());
        empleado.setCif(txtCif.getText().trim());
        empleado.setCorreoInstitucional(txtCorreo.getText().trim());
        empleado.setIdCategoria(cmbCategoria.getValue());
        empleado.setIdDepartamento(cmbDepartamento.getValue());

        boolean resultado;

        if (empleadoSeleccionado == null) {
            resultado = service.registrarEmpleado(
                    empleado,
                    txtContrasena.getText()
            );
        } else {
            resultado = service.actualizarEmpleado(empleado);

            if (resultado && !txtContrasena.getText().isBlank()) {
                resultado = service.cambiarContrasena(
                        empleado.getId(),
                        txtContrasena.getText()
                );
            }
        }

        if (resultado) {
            mostrarEstado("Empleado guardado correctamente.");
            limpiarFormulario();
            actualizarTabla();
        } else {
            mostrarEstado("No se pudo completar la operación.");
        }
    }

    private boolean validarFormulario() {
        if (txtNombreCompleto.getText().isBlank()
                || txtCif.getText().isBlank()
                || txtCorreo.getText().isBlank()
                || cmbCategoria.getValue() == null) {

            mostrarEstado("Complete los campos obligatorios.");
            return false;
        }

        if (empleadoSeleccionado == null
                && txtContrasena.getText().isBlank()) {

            mostrarEstado("Debe establecer una contraseña inicial.");
            return false;
        }

        if (txtContrasena.getText().length() > 72) {
            mostrarEstado("La contraseña no debe superar 72 caracteres.");
            return false;
        }

        return true;
    }

    @FXML
    public void limpiarFormulario() {
        txtNombreCompleto.clear();
        txtCif.clear();
        txtCorreo.clear();
        txtContrasena.clear();

        cmbCategoria.getSelectionModel().clearSelection();
        cmbDepartamento.getSelectionModel().clearSelection();

        empleadoSeleccionado = null;
        tablaEmpleados.getSelectionModel().clearSelection();

        btnGuardar.setText("Registrar empleado");
    }

    @FXML
    public void actualizarTabla() {
        try {
            empleados.setAll(service.listarEmpleados());
            estadosCuentas = service.listarEstadosCuentas();
            filtrarEmpleados();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarEstado("Error al cargar los empleados.");
        }
    }

    private void filtrarEmpleados() {
        String busqueda = txtBuscar.getText() == null
                ? ""
                : txtBuscar.getText().trim().toLowerCase();

        Departamento departamento = cmbFiltroDepartamento.getValue();

        List<Empleado> filtrados = empleados.stream()
                .filter(e ->
                        e.getNombreCompleto().toLowerCase().contains(busqueda)
                                || e.getCif().toLowerCase().contains(busqueda)
                                || e.getCorreoInstitucional().toLowerCase().contains(busqueda)
                )
                .filter(e ->
                        departamento == null
                                || (e.getIdDepartamento() != null
                                && e.getIdDepartamento().getId()
                                .equals(departamento.getId()))
                )
                .collect(Collectors.toList());

        tablaEmpleados.setItems(
                FXCollections.observableArrayList(filtrados)
        );
    }

    @FXML
    public void editarEmpleado() {
        Empleado seleccionado =
                tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarEstado("Seleccione un empleado para editar.");
            return;
        }

        empleadoSeleccionado = seleccionado;

        txtNombreCompleto.setText(seleccionado.getNombreCompleto());
        txtCif.setText(seleccionado.getCif());
        txtCorreo.setText(seleccionado.getCorreoInstitucional());
        txtContrasena.clear();

        cmbCategoria.setValue(seleccionado.getIdCategoria());
        cmbDepartamento.setValue(seleccionado.getIdDepartamento());

        btnGuardar.setText("Guardar cambios");

        mostrarEstado("Modificando información del empleado.");
    }

    @FXML
    public void eliminarEmpleado() {
        Empleado seleccionado =
                tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarEstado("Seleccione un empleado para desactivar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar operación");
        confirmacion.setHeaderText("Desactivar empleado");
        confirmacion.setContentText(
                "¿Desea desactivar a "
                        + seleccionado.getNombreCompleto() + "?"
        );

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        if (service.desactivarEmpleado(seleccionado.getId())) {
            mostrarEstado("Empleado y cuenta desactivados correctamente.");
            limpiarFormulario();
            actualizarTabla();
        } else {
            mostrarEstado("No se pudo desactivar el empleado.");
        }
    }

    @FXML
    public void cambiarEstadoCuenta() {
        Empleado seleccionado =
                tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarEstado("Seleccione un empleado.");
            return;
        }

        if (!Boolean.TRUE.equals(seleccionado.getActivo())) {
            mostrarEstado("El empleado está inactivo.");
            return;
        }

        if (service.cambiarEstadoCuenta(seleccionado.getId())) {
            mostrarEstado("Estado de cuenta actualizado.");
            actualizarTabla();
        } else {
            mostrarEstado("No se pudo modificar el estado de la cuenta.");
        }
    }

    private void mostrarEstado(String mensaje) {
        lblEstado.setText(mensaje);
    }
}
