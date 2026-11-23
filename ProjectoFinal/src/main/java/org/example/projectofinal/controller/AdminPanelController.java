package org.example.projectofinal.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import org.example.projectofinal.model.CategoriaPersonal;
import org.example.projectofinal.model.CuentaUsuario;
import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.service.AdminPanelService;
import org.example.projectofinal.util.SesionUsuario;
import org.example.projectofinal.util.Validaciones;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private List<Departamento> todosDepartamentos = List.of();

    // =========================================================
    // INICIALIZACION
    // =========================================================

    @FXML
    public void initialize() {

        configurarTabla();
        configurarCombos();
        configurarFiltros();
        configurarCampos();

        actualizarTabla();

    }

    // =========================================================
    // CAMPOS: el nombre solo acepta letras y espacios al escribir
    // =========================================================

    private void configurarCampos() {

        txtNombreCompleto.setTextFormatter(new TextFormatter<String>(cambio ->
                Validaciones.esTextoDeNombre(cambio.getControlNewText()) ? cambio : null));

        txtCorreo.setPromptText("usuario" + Validaciones.DOMINIO_CORREO);

        txtContrasena.setPromptText("Mín. 8: mayúscula, minúscula, número y signo");
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

            todosDepartamentos = service.listarDepartamentos();

            filtrarDepartamentosPorCategoria(cmbCategoria.getValue());

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
    // DEPARTAMENTOS PERMITIDOS SEGÚN LA CATEGORÍA
    // =========================================================

    /**
     * Deja en el combo de departamento solo los permitidos para la categoría
     * (tabla categoria_departamento). Si la categoría no tiene restricción
     * se muestran todos y el departamento es opcional.
     */
    private void filtrarDepartamentosPorCategoria(CategoriaPersonal categoria) {

        Departamento elegido = cmbDepartamento.getValue();

        Set<UUID> permitidos = categoria == null
                ? Set.of()
                : service.departamentosPermitidos(categoria.getId());

        List<Departamento> lista = permitidos.isEmpty()
                ? todosDepartamentos
                : todosDepartamentos.stream()
                    .filter(d -> permitidos.contains(d.getId()))
                    .toList();

        cmbDepartamento.setItems(FXCollections.observableArrayList(lista));

        cmbDepartamento.setPromptText(categoria == null
                ? "Seleccione primero la categoría"
                : permitidos.isEmpty()
                    ? "Sin departamento (opcional)"
                    : "Seleccione departamento");

        // Conserva el departamento elegido solo si sigue siendo válido
        cmbDepartamento.setValue(elegido == null ? null : lista.stream()
                .filter(d -> d.getId().equals(elegido.getId()))
                .findFirst()
                .orElse(null));
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

        cmbCategoria.valueProperty().addListener(
                (observable, anterior, nueva) -> filtrarDepartamentosPorCategoria(nueva)
        );
    }

    // =========================================================
    // REGISTRAR O ACTUALIZAR EMPLEADO
    // =========================================================

    @FXML
    private void guardarEmpleado() {

        boolean esNuevo = empleadoSeleccionado == null;
        String contrasena = txtContrasena.getText();

        // Se validan los datos del formulario en una copia, para no cambiar
        // la fila de la tabla si el service los rechaza.
        Empleado datos = new Empleado();
        if (!esNuevo) {
            datos.setId(empleadoSeleccionado.getId());
        }
        leerFormulario(datos);

        // Las reglas (nombre, CIF, correo, categoría, contraseña) están en el service;
        // el controller solo muestra el mensaje que devuelve.
        String error = service.validarEmpleado(datos, contrasena);

        if (error != null) {

            mostrarAlerta(Alert.AlertType.WARNING, "Revise los datos", error);
            return;
        }

        Empleado empleado = esNuevo ? datos : empleadoSeleccionado;

        if (!esNuevo) {
            leerFormulario(empleado);
        }

        boolean resultado;
        boolean cuentaCreada = false;

        if (esNuevo) {

            empleado.setActivo(true);

            resultado = service.registrarEmpleado(empleado, contrasena);

        } else {

            resultado = service.actualizarEmpleado(empleado);

            if (resultado && !contrasena.isEmpty()) {

                if (estadosCuentas.containsKey(empleado.getId())) {

                    resultado = service.cambiarContrasena(empleado.getId(), contrasena);

                } else {

                    // El empleado no tenía cuenta (p. ej. se eliminó en Usuarios)
                    resultado = service.crearCuenta(empleado.getId(), contrasena);
                    cuentaCreada = resultado;
                }
            }
        }

        if (resultado) {

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Operación exitosa",
                    esNuevo
                            ? "Empleado y cuenta registrados correctamente."
                            : cuentaCreada
                                ? "Datos actualizados y cuenta de usuario creada."
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
            actualizarTabla();
        }
    }

    /** Copia lo escrito en el formulario al empleado (sin validar). */
    private void leerFormulario(Empleado empleado) {

        empleado.setNombreCompleto(txtNombreCompleto.getText());
        empleado.setCif(txtCif.getText());
        empleado.setCorreoInstitucional(txtCorreo.getText());
        empleado.setIdCategoria(cmbCategoria.getValue());
        empleado.setIdDepartamento(cmbDepartamento.getValue());
    }

    // =========================================================
    // CARGAR EMPLEADO EN EL FORMULARIO
    // =========================================================

    private void cargarEmpleadoFormulario(Empleado empleado) {

        // Un nombre antiguo con caracteres no permitidos no se puede cargar en el
        // campo filtrado; se limpia para que se pueda corregir.
        String nombre = empleado.getNombreCompleto() == null ? "" : empleado.getNombreCompleto();
        txtNombreCompleto.setText(Validaciones.esTextoDeNombre(nombre)
                ? nombre
                : nombre.replaceAll("[^\\p{L} ]", ""));
        txtCif.setText(empleado.getCif());
        txtCorreo.setText(empleado.getCorreoInstitucional());

        // Primero la categoría (filtra los departamentos) y luego el departamento
        cmbCategoria.setValue(empleado.getIdCategoria() == null ? null
                : cmbCategoria.getItems().stream()
                    .filter(c -> c.getId().equals(empleado.getIdCategoria().getId()))
                    .findFirst()
                    .orElse(empleado.getIdCategoria()));

        cmbDepartamento.setValue(empleado.getIdDepartamento() == null ? null
                : cmbDepartamento.getItems().stream()
                    .filter(d -> d.getId().equals(empleado.getIdDepartamento().getId()))
                    .findFirst()
                    .orElse(null));

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
    // ELIMINAR EMPLEADO (definitivo, con confirmación)
    // =========================================================

    @FXML
    private void eliminarEmpleadoDefinitivo() {

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

        CuentaUsuario sesion = SesionUsuario.getCuentaUsuario();

        if (sesion != null && sesion.getIdEmpleado().getId().equals(seleccionado.getId())) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Acción no permitida",
                    "No puede eliminar al empleado con el que inició sesión."
            );

            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Seguro que desea eliminar a " + seleccionado.getNombreCompleto() + "?");
        confirmacion.setContentText(
                "Se borrará el empleado y su cuenta de usuario de forma permanente."
                        + "\nEsta acción no se puede deshacer."
                        + "\n\nSi solo quiere quitarle el acceso, use \"Desactivar empleado\"."
        );

        ButtonType eliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(cancelar, eliminar);

        // Cancelar queda como opción por defecto para evitar borrar por error
        ((Button) confirmacion.getDialogPane().lookupButton(eliminar)).setDefaultButton(false);
        ((Button) confirmacion.getDialogPane().lookupButton(cancelar)).setDefaultButton(true);

        if (confirmacion.showAndWait().orElse(cancelar) != eliminar) {
            return;
        }

        String error = service.eliminarEmpleado(seleccionado.getId());

        if (error == null) {

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Empleado eliminado",
                    seleccionado.getNombreCompleto() + " y su cuenta fueron eliminados."
            );

            limpiarFormulario();
            actualizarTabla();

        } else {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "No se puede eliminar",
                    error
            );
        }
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