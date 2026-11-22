package org.example.projectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.model.Supervision;
import org.example.projectofinal.service.AdminPanelService;
import org.example.projectofinal.service.SupervisionService;

import java.time.LocalDate;
import java.util.List;

public class SupervisionesController {

    @FXML private ComboBox<Empleado> cmbEmpleado;
    @FXML private ComboBox<Empleado> cmbJefe;
    @FXML private DatePicker dpInicio;

    @FXML private TableView<Supervision> tabla;
    @FXML private TableColumn<Supervision, String> colEmpleado;
    @FXML private TableColumn<Supervision, String> colJefe;
    @FXML private TableColumn<Supervision, String> colInicio;
    @FXML private TableColumn<Supervision, String> colFin;
    @FXML private TableColumn<Supervision, String> colEstado;
    @FXML private Label lblEstado;

    private final SupervisionService service = new SupervisionService();
    private final AdminPanelService empleadoService = new AdminPanelService();

    @FXML
    public void initialize() {
        cmbEmpleado.setConverter(UiUtil.conversor(SupervisionesController::nombreEmpleado));
        cmbJefe.setConverter(UiUtil.conversor(SupervisionesController::nombreEmpleado));
        dpInicio.setValue(LocalDate.now());

        UiUtil.columna(colEmpleado, s -> s.getIdEmpleado().getNombreCompleto());
        UiUtil.columna(colJefe, s -> s.getIdJefe().getNombreCompleto());
        UiUtil.columna(colInicio, s -> UiUtil.fecha(s.getFechaInicio()));
        UiUtil.columna(colFin, s -> UiUtil.fecha(s.getFechaFin()));
        UiUtil.columna(colEstado, s -> Boolean.TRUE.equals(s.getActivo()) ? "Vigente" : "Finalizada");

        actualizarTodo();
    }

    static String nombreEmpleado(Empleado e) {
        String depto = e.getIdDepartamento() == null ? "Sin departamento" : e.getIdDepartamento().getNombre();
        return e.getNombreCompleto() + " · " + depto;
    }

    @FXML
    private void actualizarTodo() {
        List<Empleado> activos = empleadoService.listarEmpleados().stream()
                .filter(e -> Boolean.TRUE.equals(e.getActivo()))
                .toList();
        cmbEmpleado.setItems(FXCollections.observableArrayList(activos));
        cmbJefe.setItems(FXCollections.observableArrayList(activos));

        tabla.setItems(FXCollections.observableArrayList(service.listarConDetalle()));
        lblEstado.setText(tabla.getItems().size() + " supervisión(es) registradas.");
    }

    @FXML
    private void asignar() {
        Empleado empleado = cmbEmpleado.getValue();
        Empleado jefe = cmbJefe.getValue();

        if (empleado == null || jefe == null) {
            UiUtil.error("Selecciona el empleado y su jefe.");
            return;
        }
        if (empleado.getId().equals(jefe.getId())) {
            UiUtil.error("Un empleado no puede ser su propio jefe.");
            return;
        }
        if (dpInicio.getValue() == null) {
            UiUtil.error("Selecciona la fecha de inicio.");
            return;
        }

        Supervision actual = service.buscarActivaPorEmpleado(empleado.getId());
        if (actual != null) {
            if (actual.getIdJefe().getId().equals(jefe.getId())) {
                UiUtil.error(jefe.getNombreCompleto() + " ya es el jefe actual de "
                        + empleado.getNombreCompleto() + ".");
                return;
            }
            if (dpInicio.getValue().isBefore(actual.getFechaInicio())) {
                UiUtil.error("La fecha debe ser igual o posterior al inicio de la supervisión actual ("
                        + UiUtil.fecha(actual.getFechaInicio()) + ").");
                return;
            }
            if (!UiUtil.confirmar(empleado.getNombreCompleto()
                    + " tiene como jefe a " + actual.getIdJefe().getNombreCompleto()
                    + ". Esa supervisión se finalizará y quedará en el historial. ¿Asignar a "
                    + jefe.getNombreCompleto() + "?")) {
                return;
            }
        }

        Supervision s = new Supervision();
        s.setIdEmpleado(empleado);
        s.setIdJefe(jefe);
        s.setFechaInicio(dpInicio.getValue());

        if (service.registrar(s)) {
            cmbEmpleado.setValue(null);
            cmbJefe.setValue(null);
            actualizarTodo();
            lblEstado.setText("Supervisión asignada.");
        } else {
            UiUtil.error("No se pudo asignar la supervisión.");
        }
    }

    @FXML
    private void finalizar() {
        Supervision s = tabla.getSelectionModel().getSelectedItem();

        if (s == null) {
            UiUtil.error("Selecciona una supervisión de la tabla.");
            return;
        }
        if (!Boolean.TRUE.equals(s.getActivo())) {
            UiUtil.error("Esa supervisión ya está finalizada.");
            return;
        }
        if (s.getFechaInicio() != null && LocalDate.now().isBefore(s.getFechaInicio())) {
            UiUtil.error("No se puede finalizar antes de su fecha de inicio.");
            return;
        }
        if (!UiUtil.confirmar("¿Finalizar la supervisión de " + s.getIdEmpleado().getNombreCompleto() + "?")) {
            return;
        }

        if (service.finalizar(s.getId())) {
            actualizarTodo();
            lblEstado.setText("Supervisión finalizada.");
        } else {
            UiUtil.error("No se pudo finalizar la supervisión.");
        }
    }
}
