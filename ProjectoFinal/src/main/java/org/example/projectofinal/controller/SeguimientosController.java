package org.example.projectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.projectofinal.model.CuentaUsuario;
import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.model.ObjetivoDepartamento;
import org.example.projectofinal.model.Seguimiento;
import org.example.projectofinal.service.AdminPanelService;
import org.example.projectofinal.service.ObjetivoDepartamentoService;
import org.example.projectofinal.service.SeguimientoService;
import org.example.projectofinal.util.SesionUsuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SeguimientosController {

    @FXML private ComboBox<ObjetivoDepartamento> cmbAsignacion;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private ComboBox<Empleado> cmbEmpleado;
    @FXML private TextField txtAvance;
    @FXML private DatePicker dpFecha;
    @FXML private TextArea txtObservaciones;

    @FXML private TableView<Seguimiento> tabla;
    @FXML private TableColumn<Seguimiento, String> colFecha;
    @FXML private TableColumn<Seguimiento, String> colObjetivo;
    @FXML private TableColumn<Seguimiento, String> colPeriodo;
    @FXML private TableColumn<Seguimiento, String> colTipo;
    @FXML private TableColumn<Seguimiento, String> colResponsable;
    @FXML private TableColumn<Seguimiento, String> colAvance;
    @FXML private TableColumn<Seguimiento, String> colRegistradoPor;
    @FXML private TableColumn<Seguimiento, String> colObservaciones;
    @FXML private Label lblEstado;

    private final SeguimientoService service = new SeguimientoService();
    private final ObjetivoDepartamentoService asignacionService = new ObjetivoDepartamentoService();
    private final AdminPanelService empleadoService = new AdminPanelService();

    private List<Empleado> empleadosActivos = List.of();

    @FXML
    public void initialize() {
        cmbAsignacion.setConverter(UiUtil.conversor(a -> a.getIdObjetivo().getNombre()
                + " · " + a.getIdDepartamento().getNombre()
                + " · " + a.getIdPeriodo().getNombre()));
        cmbEmpleado.setConverter(UiUtil.conversor(Empleado::getNombreCompleto));

        cmbTipo.setItems(FXCollections.observableArrayList(
                SeguimientoService.DEPARTAMENTAL, SeguimientoService.INDIVIDUAL));
        cmbTipo.setValue(SeguimientoService.DEPARTAMENTAL);
        cmbEmpleado.disableProperty().bind(cmbTipo.valueProperty().isNotEqualTo(SeguimientoService.INDIVIDUAL));
        cmbTipo.valueProperty().addListener((obs, ant, nuevo) -> {
            if (!SeguimientoService.INDIVIDUAL.equals(nuevo)) {
                cmbEmpleado.setValue(null);
            }
        });
        cmbAsignacion.valueProperty().addListener((obs, ant, nueva) -> filtrarEmpleados(nueva));

        dpFecha.setValue(LocalDate.now());

        UiUtil.columna(colFecha, s -> UiUtil.fecha(s.getFechaSeguimiento()));
        UiUtil.columna(colObjetivo, s -> s.getIdAsignacion().getIdObjetivo().getNombre());
        UiUtil.columna(colPeriodo, s -> s.getIdAsignacion().getIdPeriodo().getNombre());
        UiUtil.columna(colTipo, Seguimiento::getTipo);
        UiUtil.columna(colResponsable, s -> s.getIdEmpleado() != null
                ? s.getIdEmpleado().getNombreCompleto()
                : s.getIdDepartamento() != null ? s.getIdDepartamento().getNombre() : "");
        UiUtil.columna(colAvance, s -> s.getPorcentajeAvance() == null ? ""
                : s.getPorcentajeAvance().stripTrailingZeros().toPlainString() + " %");
        UiUtil.columna(colRegistradoPor, s -> s.getRegistradoPor().getNombreCompleto());
        UiUtil.columna(colObservaciones, Seguimiento::getObservaciones);

        actualizarTodo();
    }

    @FXML
    private void actualizarTodo() {
        empleadosActivos = empleadoService.listarEmpleados().stream()
                .filter(e -> Boolean.TRUE.equals(e.getActivo()))
                .toList();
        cmbAsignacion.setItems(FXCollections.observableArrayList(asignacionService.listarConDetalle()));
        filtrarEmpleados(cmbAsignacion.getValue());

        tabla.setItems(FXCollections.observableArrayList(service.listarConDetalle()));
        lblEstado.setText(tabla.getItems().size() + " seguimiento(s) registrados."
                + (cmbAsignacion.getItems().isEmpty()
                    ? " Primero asigna objetivos a departamentos en la pantalla Objetivos." : ""));
    }

    /** Muestra solo los empleados del departamento de la asignación elegida. */
    private void filtrarEmpleados(ObjetivoDepartamento asignacion) {
        List<Empleado> lista = asignacion == null ? empleadosActivos
                : empleadosActivos.stream()
                    .filter(e -> e.getIdDepartamento() != null
                            && e.getIdDepartamento().getId().equals(asignacion.getIdDepartamento().getId()))
                    .toList();
        cmbEmpleado.setItems(FXCollections.observableArrayList(lista));
    }

    @FXML
    private void limpiarFormulario() {
        cmbAsignacion.setValue(null);
        cmbTipo.setValue(SeguimientoService.DEPARTAMENTAL);
        cmbEmpleado.setValue(null);
        txtAvance.clear();
        dpFecha.setValue(LocalDate.now());
        txtObservaciones.clear();
    }

    @FXML
    private void registrar() {
        CuentaUsuario cuenta = SesionUsuario.getCuentaUsuario();

        BigDecimal avance;
        try {
            avance = UiUtil.numero(txtAvance);
        } catch (NumberFormatException e) {
            UiUtil.error("El porcentaje de avance debe ser un número.");
            return;
        }

        ObjetivoDepartamento asignacion = cmbAsignacion.getValue();

        Seguimiento s = new Seguimiento();
        s.setIdAsignacion(asignacion);
        s.setTipo(cmbTipo.getValue());
        s.setPorcentajeAvance(avance);
        s.setFechaSeguimiento(dpFecha.getValue() == null ? LocalDate.now() : dpFecha.getValue());
        s.setObservaciones(UiUtil.texto(txtObservaciones));
        s.setRegistradoPor(cuenta == null ? null : cuenta.getIdEmpleado());

        if (SeguimientoService.INDIVIDUAL.equals(cmbTipo.getValue())) {
            s.setIdEmpleado(cmbEmpleado.getValue());
        } else if (asignacion != null) {
            s.setIdDepartamento(asignacion.getIdDepartamento());
        }

        String error = service.validar(s);
        if (error != null) {
            UiUtil.error(error);
            return;
        }

        if (service.registrar(s)) {
            limpiarFormulario();
            actualizarTodo();
            lblEstado.setText("Seguimiento registrado.");
        } else {
            UiUtil.error("No se pudo registrar el seguimiento.");
        }
    }
}
