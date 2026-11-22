package org.example.projectofinal.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import org.example.projectofinal.model.CategoriaPersonal;
import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.service.AdminPanelService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminPanelController {

    // =========================================================
    // CAMPOS DEL FORMULARIO
    // =========================================================

    @FXML
    private TextField txtNombreCompleto;

    @FXML
    private TextField txtCif;

    @FXML
    private TextField txtCorreo;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private ComboBox<CategoriaPersonal> cmbCategoria;

    @FXML
    private ComboBox<Departamento> cmbDepartamento;

    @FXML
    private Button btnGuardar;

    // =========================================================
    // BUSQUEDA Y FILTROS
    // =========================================================

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<Departamento> cmbFiltroDepartamento;

    // =========================================================
    // TABLA
    // =========================================================

    @FXML
    private TableView<Empleado> tablaEmpleados;

    @FXML
    private TableColumn<Empleado, String> colNombre;

    @FXML
    private TableColumn<Empleado, String> colCif;

    @FXML
    private TableColumn<Empleado, String> colCorreo;

    @FXML
    private TableColumn<Empleado, String> colCategoria;

    @FXML
    private TableColumn<Empleado, String> colDepartamento;

    @FXML
    private TableColumn<Empleado, String> colEstado;

    // =========================================================
    // ESTADO Y SERVICIO
    // =========================================================

    @FXML
    private Label lblEstado;

    private final AdminPanelService service = new AdminPanelService();

    private final ObservableList<Empleado> empleados =
            FXCollections.observableArrayList();

    private final ObservableList<Empleado> datosTabla =
            FXCollections.observableArrayList();

    private final Map<UUID, Boolean> estadosCuentas = new HashMap<>();

    private Empleado empleadoSeleccionado;

    // =========================================================
    // INICIALIZACION
    // =========================================================

    @FXML
    public void initialize() {

        configurarTabla();
        configurarCombos();
        configurarFiltros();

        actualizarTabla();

    }

    // =========================================================
    // CONFIGURAR TABLA
    // =========================================================

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

        colCategoria.setCellValueFactory(data -> {

            CategoriaPersonal categoria =
                    data.getValue().getIdCategoria();

            return new SimpleStringProperty(
                    categoria != null ? categoria.getNombre() : ""
            );
        });

        colDepartamento.setCellValueFactory(data -> {

            Departamento departamento =
                    data.getValue().getIdDepartamento();

            return new SimpleStringProperty(
                    departamento != null ? departamento.getNombre() : "Sin departamento"
            );
        });

        colEstado.setCellValueFactory(data -> {

            Empleado empleado = data.getValue();

            if (!Boolean.TRUE.equals(empleado.getActivo())) {
                return new SimpleStringProperty("Empleado inactivo");
            }

            Boolean activo = estadosCuentas.get(empleado.getId());

            if (activo == null) {
                return new SimpleStringProperty("Sin cuenta");
            }

            return new SimpleStringProperty(
                    activo ? "Cuenta activa" : "Cuenta inactiva"
            );
        });

        tablaEmpleados.setItems(datosTabla);

        tablaEmpleados.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionado) -> {

                    if (seleccionado != null) {
                        empleadoSeleccionado = seleccionado;
                        cargarEmpleadoFormulario(seleccionado);
                    }

                });
    }

    // =========================================================
    // CONFIGURAR COMBOS
    // =========================================================

    private void configurarCombos() {

        StringConverter<CategoriaPersonal> conversorCategoria =
                new StringConverter<>() {

                    @Override
                    public String toString(CategoriaPersonal categoria) {
                        return categoria == null ? "" : categoria.getNombre();
                    }

                    @Override
                    public CategoriaPersonal fromString(String texto) {
                        return null;
                    }
                };

        StringConverter<Departamento> conversorDepartamento =
                new StringConverter<>() {

                    @Override
                    public String toString(Departamento departamento) {
                        return departamento == null ? "" : departamento.getNombre();
                    }

                    @Override
                    public Departamento fromString(String texto) {
                        return null;
                    }
                };

        cmbCategoria.setConverter(conversorCategoria);
        cmbDepartamento.setConverter(conversorDepartamento);
        cmbFiltroDepartamento.setConverter(conversorDepartamento);

        cmbCategoria.setCellFactory(param ->
                new ListCell<>() {
                    @Override
                    protected void updateItem(CategoriaPersonal item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getNombre());
                    }
                }
        );

        cmbDepartamento.setCellFactory(param ->
                new ListCell<>() {
                    @Override
                    protected void updateItem(Departamento item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getNombre());
                    }
                }
        );

        cmbFiltroDepartamento.setCellFactory(param ->
                new ListCell<>() {
                    @Override
                    protected void updateItem(Departamento item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getNombre());
                    }
                }
        );

        cargarCombos();
    }

    private void cargarCombos() {

        try {

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

            cmbFiltroDepartamento.setPromptText("Todos los departamentos");

        } catch (Exception e) {

            e.printStackTrace();
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudieron cargar las categorías y departamentos."
            );
        }
    }

    // =========================================================
    // CONFIGURAR FILTROS
    // =========================================================

    private void configurarFiltros() {

        txtBuscar.textProperty().addListener(
                (observable, anterior, nuevo) -> filtrarEmpleados()
        );

        cmbFiltroDepartamento.valueProperty().addListener(
                (observable, anterior, nuevo) -> filtrarEmpleados()
        );
    }

    // =========================================================
    // REGISTRAR O ACTUALIZAR EMPLEADO
    // =========================================================

    @FXML
    private void guardarEmpleado() {

        if (!validarFormulario()) {
            return;
        }

        boolean esNuevo = empleadoSeleccionado == null;

        Empleado empleado = esNuevo
                ? new Empleado()
                : empleadoSeleccionado;

        empleado.setNombreCompleto(txtNombreCompleto.getText().trim());
        empleado.setCif(txtCif.getText().trim());
        empleado.setCorreoInstitucional(txtCorreo.getText().trim());
        empleado.setIdCategoria(cmbCategoria.getValue());
        empleado.setIdDepartamento(cmbDepartamento.getValue());

        boolean resultado;

        if (esNuevo) {

            empleado.setActivo(true);

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

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Operación exitosa",
                    esNuevo
                            ? "Empleado y cuenta registrados correctamente."
                            : "Los datos del empleado fueron actualizados."
            );

            limpiarFormulario();
            actualizarTabla();

        } else {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo completar la operación."
            );
        }
    }

    // =========================================================
    // VALIDAR FORMULARIO
    // =========================================================

    private boolean validarFormulario() {

        if (txtNombreCompleto.getText().isBlank()
                || txtCif.getText().isBlank()
                || txtCorreo.getText().isBlank()
                || cmbCategoria.getValue() == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Campos obligatorios",
                    "Complete el nombre, CIF, correo y categoría."
            );

            return false;
        }

        String correo = txtCorreo.getText().trim();

        if (!correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Correo inválido",
                    "Introduzca una dirección de correo válida."
            );

            return false;
        }

        if (empleadoSeleccionado == null) {

            String contrasena = txtContrasena.getText();

            if (contrasena == null || contrasena.length() < 8
                    || contrasena.length() > 128) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Contraseña inválida",
                        "La contraseña inicial debe tener entre 8 y 128 caracteres."
                );

                return false;
            }

        } else if (!txtContrasena.getText().isBlank()
                && (txtContrasena.getText().length() < 8
                || txtContrasena.getText().length() > 128)) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Contraseña inválida",
                    "La nueva contraseña debe tener entre 8 y 128 caracteres."
            );

            return false;
        }

        return true;
    }

    // =========================================================
    // CARGAR EMPLEADO EN EL FORMULARIO
    // =========================================================

    private void cargarEmpleadoFormulario(Empleado empleado) {

        txtNombreCompleto.setText(empleado.getNombreCompleto());
        txtCif.setText(empleado.getCif());
        txtCorreo.setText(empleado.getCorreoInstitucional());

        cmbCategoria.setValue(empleado.getIdCategoria());
        cmbDepartamento.setValue(empleado.getIdDepartamento());

        txtContrasena.clear();

        btnGuardar.setText("Actualizar");
    }

    // =========================================================
    // LIMPIAR FORMULARIO
    // =========================================================

    @FXML
    private void limpiarFormulario() {

        empleadoSeleccionado = null;

        txtNombreCompleto.clear();
        txtCif.clear();
        txtCorreo.clear();
        txtContrasena.clear();

        cmbCategoria.setValue(null);
        cmbDepartamento.setValue(null);

        tablaEmpleados.getSelectionModel().clearSelection();

        btnGuardar.setText("Guardar");

        mostrarEstado("Formulario limpio.");
    }

    // =========================================================
    // ACTUALIZAR TABLA
    // =========================================================

    @FXML
    private void actualizarTabla() {

        try {

            empleados.setAll(service.listarEmpleados());

            estadosCuentas.clear();
            estadosCuentas.putAll(service.listarEstadosCuentas());

            filtrarEmpleados();

            tablaEmpleados.refresh();

            mostrarEstado("Registros actualizados: " + empleados.size());

        } catch (Exception e) {

            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo actualizar la tabla de empleados."
            );
        }
    }

    // =========================================================
    // FILTRAR EMPLEADOS
    // =========================================================

    private void filtrarEmpleados() {

        String busqueda = txtBuscar.getText() == null
                ? ""
                : txtBuscar.getText().trim().toLowerCase();

        Departamento departamentoSeleccionado =
                cmbFiltroDepartamento.getValue();

        datosTabla.setAll(
                empleados.stream()
                        .filter(empleado -> {

                            boolean coincideBusqueda =
                                    empleado.getNombreCompleto().toLowerCase().contains(busqueda)
                                            || empleado.getCif().toLowerCase().contains(busqueda)
                                            || empleado.getCorreoInstitucional().toLowerCase().contains(busqueda);

                            boolean coincideDepartamento =
                                    departamentoSeleccionado == null
                                            || (empleado.getIdDepartamento() != null
                                            && empleado.getIdDepartamento().getId()
                                            .equals(departamentoSeleccionado.getId()));

                            return coincideBusqueda && coincideDepartamento;
                        })
                        .toList()
        );

        tablaEmpleados.refresh();
    }

    // =========================================================
    // EDITAR EMPLEADO
    // =========================================================

    @FXML
    private void editarEmpleado() {

        Empleado seleccionado =
                tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Selección requerida",
                    "Seleccione un empleado de la tabla."
            );

            return;
        }

        empleadoSeleccionado = seleccionado;
        cargarEmpleadoFormulario(seleccionado);
    }

    // =========================================================
    // DESACTIVAR EMPLEADO
    // =========================================================

    @FXML
    private void eliminarEmpleado() {

        Empleado seleccionado =
                tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Selección requerida",
                    "Seleccione un empleado."
            );

            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Confirmar desactivación");
        confirmacion.setHeaderText("¿Desea desactivar este empleado?");
        confirmacion.setContentText(
                seleccionado.getNombreCompleto()
                        + "\nTambién se desactivará su cuenta de usuario."
        );

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (respuesta == ButtonType.OK) {

                boolean resultado =
                        service.desactivarEmpleado(seleccionado.getId());

                if (resultado) {

                    mostrarAlerta(
                            Alert.AlertType.INFORMATION,
                            "Operación exitosa",
                            "El empleado y su cuenta fueron desactivados."
                    );

                    limpiarFormulario();
                    actualizarTabla();

                } else {

                    mostrarAlerta(
                            Alert.AlertType.ERROR,
                            "Error",
                            "No se pudo desactivar el empleado."
                    );
                }
            }
        });
    }

    // =========================================================
    // REACTIVAR EMPLEADO
    // =========================================================

    @FXML
    private void reactivarEmpleado() {

        Empleado seleccionado =
                tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Selección requerida",
                    "Seleccione un empleado."
            );

            return;
        }

        if (Boolean.TRUE.equals(seleccionado.getActivo())) {

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Empleado activo",
                    "El empleado seleccionado ya está activo."
            );

            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Confirmar reactivación");
        confirmacion.setHeaderText("¿Desea reactivar este empleado?");
        confirmacion.setContentText(
                seleccionado.getNombreCompleto()
                        + "\nTambién se reactivará su cuenta de usuario."
        );

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (respuesta == ButtonType.OK) {

                if (service.reactivarEmpleado(seleccionado.getId())) {

                    mostrarAlerta(
                            Alert.AlertType.INFORMATION,
                            "Operación exitosa",
                            "El empleado y su cuenta fueron reactivados."
                    );

                    limpiarFormulario();
                    actualizarTabla();

                } else {

                    mostrarAlerta(
                            Alert.AlertType.ERROR,
                            "Error",
                            "No se pudo reactivar el empleado."
                    );
                }
            }
        });
    }

    // =========================================================
    // CAMBIAR ESTADO DE CUENTA
    // =========================================================

    @FXML
    private void cambiarEstadoCuenta() {

        Empleado seleccionado =
                tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Selección requerida",
                    "Seleccione un empleado."
            );

            return;
        }

        if (!Boolean.TRUE.equals(seleccionado.getActivo())) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Empleado inactivo",
                    "No se puede activar una cuenta perteneciente a un empleado inactivo."
            );

            return;
        }

        boolean resultado =
                service.cambiarEstadoCuenta(seleccionado.getId());

        if (resultado) {

            actualizarTabla();

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Operación exitosa",
                    "El estado de la cuenta fue cambiado correctamente."
            );

        } else {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo cambiar el estado de la cuenta."
            );
        }
    }

    // =========================================================
    // MENSAJES
    // =========================================================

    private void mostrarEstado(String mensaje) {

        if (lblEstado != null) {
            lblEstado.setText(mensaje);
        }
    }

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensaje) {

        Alert alerta = new Alert(tipo);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}