package org.example.projectofinal.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminPanelController implements Initializable {
    @FXML private TextField txtNombreCompleto;
    @FXML private TextField txtCif;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbDepartamento;
    @FXML private ComboBox<String> cmbFiltroDepartamento;
    @FXML private TextField txtBuscar;
    @FXML private TableView<EmpleadoTemporal> tablaEmpleados;
    @FXML private TableColumn<EmpleadoTemporal, String> colNombre;
    @FXML private TableColumn<EmpleadoTemporal, String> colCif;
    @FXML private TableColumn<EmpleadoTemporal, String> colCorreo;
    @FXML private TableColumn<EmpleadoTemporal, String> colCategoria;
    @FXML private TableColumn<EmpleadoTemporal, String> colDepartamento;
    @FXML private TableColumn<EmpleadoTemporal, String> colEstado;
    @FXML private Button btnGuardar;
    @FXML private Label lblEstado;

    private final ObservableList<EmpleadoTemporal> empleados = FXCollections.observableArrayList();
    private FilteredList<EmpleadoTemporal> empleadosFiltrados;
    private EmpleadoTemporal empleadoEditando;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarTabla();
        cargarOpciones();
        empleadosFiltrados = new FilteredList<>(empleados, empleado -> true);
        tablaEmpleados.setItems(empleadosFiltrados);
        txtBuscar.textProperty().addListener((observable, anterior, actual) -> aplicarFiltros());
        cmbFiltroDepartamento.valueProperty().addListener((observable, anterior, actual) -> aplicarFiltros());
    }

    // Configuración de columnas
    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colCif.setCellValueFactory(new PropertyValueFactory<>("cif"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colDepartamento.setCellValueFactory(new PropertyValueFactory<>("departamento"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
    }

    // Opciones temporales para las listas
    private void cargarOpciones() {
        cmbCategoria.setItems(FXCollections.observableArrayList(
                "DOCENTES",
                "MANTENIMIENTO",
                "SEGURIDAD",
                "SERVICIOS_ESTUDIANTES",
                "OTROS"
        ));
        ObservableList<String> departamentos = FXCollections.observableArrayList(
                "Administración",
                "Académico",
                "Servicios generales"
        );
        cmbDepartamento.setItems(FXCollections.observableArrayList(departamentos));
        cmbFiltroDepartamento.setItems(FXCollections.observableArrayList());
        cmbFiltroDepartamento.getItems().add("Todos los departamentos");
        cmbFiltroDepartamento.getItems().addAll(departamentos);
        cmbFiltroDepartamento.setValue("Todos los departamentos");
    }

    // Registrar o guardar cambios
    @FXML
    private void guardarEmpleado() {
        String nombre = txtNombreCompleto.getText().trim();
        String cif = txtCif.getText().trim();
        String correo = txtCorreo.getText().trim();
        String categoria = cmbCategoria.getValue();
        String departamento = cmbDepartamento.getValue();

        if (nombre.isEmpty() || cif.isEmpty() || correo.isEmpty()
                || categoria == null || departamento == null) {
            mostrarMensaje("Complete todos los campos.");
            return;
        }

        if (!correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            mostrarMensaje("Ingrese un correo electrónico válido.");
            return;
        }

        boolean duplicado = empleados.stream().anyMatch(empleado ->
                empleado != empleadoEditando &&
                        (empleado.getCif().equalsIgnoreCase(cif)
                                || empleado.getCorreo().equalsIgnoreCase(correo))
        );

        if (duplicado) {
            mostrarMensaje("El CIF o el correo ya están registrados.");
            return;
        }

        if (empleadoEditando == null) {
            empleados.add(new EmpleadoTemporal(nombre, cif, correo, categoria, departamento));
            mostrarMensaje("Empleado registrado correctamente.");
        } else {
            empleadoEditando.setNombreCompleto(nombre);
            empleadoEditando.setCif(cif);
            empleadoEditando.setCorreo(correo);
            empleadoEditando.setCategoria(categoria);
            empleadoEditando.setDepartamento(departamento);
            mostrarMensaje("Información del empleado actualizada.");
        }

        limpiarFormulario();
        aplicarFiltros();
    }

    // Cargar datos en el formulario para editar
    @FXML
    private void editarEmpleado() {
        EmpleadoTemporal seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarMensaje("Seleccione un empleado de la tabla.");
            return;
        }

        empleadoEditando = seleccionado;
        txtNombreCompleto.setText(seleccionado.getNombreCompleto());
        txtCif.setText(seleccionado.getCif());
        txtCorreo.setText(seleccionado.getCorreo());
        cmbCategoria.setValue(seleccionado.getCategoria());
        cmbDepartamento.setValue(seleccionado.getDepartamento());
        btnGuardar.setText("Guardar cambios");
        mostrarMensaje("Editando: " + seleccionado.getNombreCompleto());
    }

    // Limpiar formulario y cancelar edición
    @FXML
    private void limpiarFormulario() {
        txtNombreCompleto.clear();
        txtCif.clear();
        txtCorreo.clear();
        cmbCategoria.setValue(null);
        cmbDepartamento.setValue(null);
        empleadoEditando = null;
        btnGuardar.setText("Registrar empleado");
        tablaEmpleados.getSelectionModel().clearSelection();
        mostrarMensaje("Formulario limpio.");
    }

    // Eliminar empleado con confirmación
    @FXML
    private void eliminarEmpleado() {
        EmpleadoTemporal seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarMensaje("Seleccione un empleado para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("Eliminar empleado");
        confirmacion.setContentText("¿Desea eliminar el registro de "
                + seleccionado.getNombreCompleto() + "?");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            empleados.remove(seleccionado);
            if (empleadoEditando == seleccionado) {
                limpiarFormulario();
            }
            mostrarMensaje("Empleado eliminado correctamente.");
        }
    }

    // Activar o desactivar cuenta
    @FXML
    private void cambiarEstadoCuenta() {
        EmpleadoTemporal seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarMensaje("Seleccione un empleado de la tabla.");
            return;
        }

        String nuevoEstado = seleccionado.getEstado().equals("Activo") ? "Inactivo" : "Activo";
        seleccionado.setEstado(nuevoEstado);
        tablaEmpleados.refresh();
        mostrarMensaje("Estado de cuenta actualizado: " + nuevoEstado);
    }

    // Actualizar tabla
    @FXML
    private void actualizarTabla() {
        aplicarFiltros();
        tablaEmpleados.refresh();
        mostrarMensaje("Tabla actualizada.");
    }

    // Aplicar búsqueda y filtro
    private void aplicarFiltros() {
        if (empleadosFiltrados == null) return;

        String busqueda = txtBuscar.getText() == null
                ? ""
                : txtBuscar.getText().trim().toLowerCase();

        String departamento = cmbFiltroDepartamento.getValue();

        empleadosFiltrados.setPredicate(empleado -> {
            boolean coincideBusqueda =
                    empleado.getNombreCompleto().toLowerCase().contains(busqueda)
                            || empleado.getCif().toLowerCase().contains(busqueda)
                            || empleado.getCorreo().toLowerCase().contains(busqueda);

            boolean coincideDepartamento =
                    departamento == null
                            || departamento.equals("Todos los departamentos")
                            || empleado.getDepartamento().equals(departamento);

            return coincideBusqueda && coincideDepartamento;
        });
    }

    private void mostrarMensaje(String mensaje) {
        lblEstado.setText(mensaje);
    }

    // Modelo temporal para probar la tabla
    public static class EmpleadoTemporal {
        private final StringProperty nombreCompleto;
        private final StringProperty cif;
        private final StringProperty correo;
        private final StringProperty categoria;
        private final StringProperty departamento;
        private final StringProperty estado;

        public EmpleadoTemporal(String nombreCompleto, String cif, String correo,
                                String categoria, String departamento) {
            this.nombreCompleto = new SimpleStringProperty(nombreCompleto);
            this.cif = new SimpleStringProperty(cif);
            this.correo = new SimpleStringProperty(correo);
            this.categoria = new SimpleStringProperty(categoria);
            this.departamento = new SimpleStringProperty(departamento);
            this.estado = new SimpleStringProperty("Activo");
        }

        public String getNombreCompleto() { return nombreCompleto.get(); }
        public void setNombreCompleto(String valor) { nombreCompleto.set(valor); }
        public String getCif() { return cif.get(); }
        public void setCif(String valor) { cif.set(valor); }
        public String getCorreo() { return correo.get(); }
        public void setCorreo(String valor) { correo.set(valor); }
        public String getCategoria() { return categoria.get(); }
        public void setCategoria(String valor) { categoria.set(valor); }
        public String getDepartamento() { return departamento.get(); }
        public void setDepartamento(String valor) { departamento.set(valor); }
        public String getEstado() { return estado.get(); }
        public void setEstado(String valor) { estado.set(valor); }
    }
}
