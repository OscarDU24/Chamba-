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
import org.example.projectofinal.service.ResponsableObjetivoService;
import org.example.projectofinal.service.SeguimientoService;
import org.example.projectofinal.util.SesionUsuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private final ResponsableObjetivoService responsableService = new ResponsableObjetivoService();

    private List<Empleado> empleadosActivos = List.of();

    /** Si el usuario es EMPLEADO solo registra y ve su propio avance individual. */
    private boolean esEmpleado;
    private Empleado yo;

    @FXML
    public void initialize() {
        CuentaUsuario cuentaSesion = SesionUsuario.getCuentaUsuario();
        yo = cuentaSesion == null ? null : cuentaSesion.getIdEmpleado();
        esEmpleado = cuentaSesion == null || !"ADMINISTRADOR".equals(cuentaSesion.getRol());

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
        cmbAsignacion.valueProperty().addListener((obs, ant, nueva) -> {
            filtrarEmpleados(nueva);
            ajustarFechaAlPeriodo(nueva);
        });

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

        if (esEmpleado) {
            cmbTipo.setValue(SeguimientoService.INDIVIDUAL);
            cmbTipo.setDisable(true);
        }

        actualizarTodo();
    }

    @FXML
    private void actualizarTodo() {
        List<ObjetivoDepartamento> asignaciones = asignacionService.listarConDetalle();
        List<Seguimiento> seguimientos = service.listarConDetalle();

        if (esEmpleado) {
            // Solo objetivos en los que participa y solo sus propios avances
            Set<UUID> misAsignaciones = yo == null ? Set.of()
                    : responsableService.listarPorEmpleado(yo.getId()).stream()
                        .map(r -> r.getIdAsignacion().getId())
                        .collect(Collectors.toSet());
            asignaciones = asignaciones.stream()
                    .filter(a -> misAsignaciones.contains(a.getId()))
                    .toList();
            seguimientos = seguimientos.stream()
                    .filter(s -> yo != null && s.getIdEmpleado() != null
                            && s.getIdEmpleado().getId().equals(yo.getId()))
                    .toList();
            empleadosActivos = yo == null ? List.of() : List.of(yo);
        } else {
            empleadosActivos = empleadoService.listarEmpleados().stream()
                    .filter(e -> Boolean.TRUE.equals(e.getActivo()))
                    .toList();
        }

        cmbAsignacion.setItems(FXCollections.observableArrayList(asignaciones));
        filtrarEmpleados(cmbAsignacion.getValue());

        tabla.setItems(FXCollections.observableArrayList(seguimientos));
        lblEstado.setText(tabla.getItems().size() + " seguimiento(s) registrados."
                + (cmbAsignacion.getItems().isEmpty()
                    ? (esEmpleado ? " Todavía no participas en objetivos asignados."
                                  : " Primero asigna objetivos a departamentos en la pantalla Objetivos.")
                    : ""));
    }

    /** Muestra solo los empleados del departamento de la asignación elegida. */
    private void filtrarEmpleados(ObjetivoDepartamento asignacion) {
        if (esEmpleado) {
            cmbEmpleado.setItems(FXCollections.observableArrayList(empleadosActivos));
            cmbEmpleado.setValue(yo);
            return;
        }
        // Solo los empleados que participan en el objetivo asignado
        Set<UUID> participantes = asignacion == null ? Set.of()
                : responsableService.listarPorAsignacion(asignacion.getId()).stream()
                    .map(r -> r.getIdEmpleado().getId())
                    .collect(Collectors.toSet());
        List<Empleado> lista = asignacion == null ? empleadosActivos
                : empleadosActivos.stream()
                    .filter(e -> participantes.contains(e.getId()))
                    .toList();
        cmbEmpleado.setItems(FXCollections.observableArrayList(lista));
    }

    /** Si la fecha elegida queda fuera del período de la asignación, la acerca al período. */
    private void ajustarFechaAlPeriodo(ObjetivoDepartamento asignacion) {
        if (asignacion == null || asignacion.getIdPeriodo() == null) {
            return;
        }
        LocalDate inicio = asignacion.getIdPeriodo().getFechaInicio();
        LocalDate fin = asignacion.getIdPeriodo().getFechaFin();
        LocalDate fecha = dpFecha.getValue() == null ? LocalDate.now() : dpFecha.getValue();

        if (fecha.isBefore(inicio)) {
            dpFecha.setValue(inicio);
        } else if (fecha.isAfter(fin)) {
            dpFecha.setValue(fin);
        }
    }

    @FXML
    private void limpiarFormulario() {
        cmbAsignacion.setValue(null);
        cmbTipo.setValue(esEmpleado ? SeguimientoService.INDIVIDUAL : SeguimientoService.DEPARTAMENTAL);
        cmbEmpleado.setValue(esEmpleado ? yo : null);
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
