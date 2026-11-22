package org.example.projectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import org.example.projectofinal.model.Evaluacion;
import org.example.projectofinal.model.ObjetivoDepartamento;
import org.example.projectofinal.model.PeriodoEvaluacion;
import org.example.projectofinal.model.Seguimiento;
import org.example.projectofinal.service.AdminPanelService;
import org.example.projectofinal.service.EvaluacionService;
import org.example.projectofinal.service.ObjetivoDepartamentoService;
import org.example.projectofinal.service.PeriodoEvaluacionService;
import org.example.projectofinal.service.SeguimientoService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DashboardController {

    /** Una fila de la tabla de actividad reciente. */
    public record Actividad(LocalDate fecha, String actividad, String responsable, String estado) {
    }

    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final int MAX_ACTIVIDADES = 10;

    @FXML
    private Label lblTotalEmpleados;

    @FXML
    private Label lblTotalDepartamentos;

    @FXML
    private Label lblTotalObjetivos;

    @FXML
    private Label lblTotalEvaluaciones;

    @FXML
    private Label lblPeriodoActual;

    @FXML
    private Label lblFechasPeriodo;

    @FXML
    private Label lblEstadoPeriodo;

    @FXML
    private Label lblObjetivosAsignados;

    @FXML
    private Label lblObjetivosSeguimiento;

    @FXML
    private Label lblObjetivosCumplidos;

    @FXML
    private TableView<Actividad> tablaActividad;

    @FXML
    private TableColumn<Actividad, String> colFecha;

    @FXML
    private TableColumn<Actividad, String> colActividad;

    @FXML
    private TableColumn<Actividad, String> colResponsable;

    @FXML
    private TableColumn<Actividad, String> colEstado;

    private final AdminPanelService service = new AdminPanelService();
    private final PeriodoEvaluacionService periodoService = new PeriodoEvaluacionService();
    private final ObjetivoDepartamentoService asignacionService = new ObjetivoDepartamentoService();
    private final SeguimientoService seguimientoService = new SeguimientoService();
    private final EvaluacionService evaluacionService = new EvaluacionService();

    private PeriodoEvaluacion periodo;
    private List<Seguimiento> seguimientos = List.of();

    @FXML
    public void initialize() {

        try {
            periodo = buscarPeriodo();
            seguimientos = seguimientoService.listarConDetalle();

            cargarIndicadores();
            cargarPeriodo();
            cargarResumenObjetivos();
            configurarActividad();

        } catch (Exception e) {
            e.printStackTrace();
            lblTotalEmpleados.setText("Error");
            lblTotalDepartamentos.setText("Error");
            lblPeriodoActual.setText("No se pudieron cargar los datos");
        }
    }

    /** Período cuyas fechas incluyen hoy; si no hay, el último EN_CURSO; si no, el más reciente. */
    private PeriodoEvaluacion buscarPeriodo() {
        PeriodoEvaluacion actual = periodoService.buscarPeriodoActual();
        if (actual != null) {
            return actual;
        }
        List<PeriodoEvaluacion> todos = periodoService.listarOrdenados();
        return todos.stream()
                .filter(p -> "EN_CURSO".equals(p.getEstado()))
                .findFirst()
                .orElse(todos.isEmpty() ? null : todos.get(0));
    }

    private void cargarIndicadores() {

        long totalActivos = service.listarEmpleados().stream()
                .filter(e -> Boolean.TRUE.equals(e.getActivo()))
                .count();

        lblTotalEmpleados.setText(String.valueOf(totalActivos));
        lblTotalDepartamentos.setText(String.valueOf(service.listarDepartamentos().size()));
        lblTotalObjetivos.setText(String.valueOf(asignacionesDelPeriodo().size()));
        lblTotalEvaluaciones.setText(String.valueOf(
                periodo == null ? 0 : evaluacionService.listarPorPeriodo(periodo.getId()).size()));
    }

    private void cargarPeriodo() {

        if (periodo == null) {
            lblPeriodoActual.setText("No hay un período configurado");
            lblFechasPeriodo.setText("Créalo en la pantalla Períodos");
            lblEstadoPeriodo.setText("Sin período");
            return;
        }

        lblPeriodoActual.setText(periodo.getNombre());
        lblFechasPeriodo.setText("Del " + UiUtil.fecha(periodo.getFechaInicio())
                + " al " + UiUtil.fecha(periodo.getFechaFin()));
        lblEstadoPeriodo.setText(switch (periodo.getEstado()) {
            case "PLANIFICADO" -> "Planificado";
            case "EN_CURSO" -> "En curso";
            case "CERRADO" -> "Cerrado";
            default -> periodo.getEstado();
        });
    }

    private List<ObjetivoDepartamento> asignacionesDelPeriodo() {
        return periodo == null ? List.of() : asignacionService.listarPorPeriodo(periodo.getId());
    }

    /**
     * Asignados: objetivos asignados en el período.
     * En seguimiento: tienen al menos un avance y el último es menor a 100 %.
     * Cumplidos: el último avance registrado llegó a 100 %.
     */
    private void cargarResumenObjetivos() {

        List<ObjetivoDepartamento> asignaciones = asignacionesDelPeriodo();

        Map<UUID, Seguimiento> ultimo = new HashMap<>();
        for (Seguimiento s : seguimientos) {
            UUID id = s.getIdAsignacion().getId();
            Seguimiento actual = ultimo.get(id);
            if (actual == null || s.getFechaSeguimiento().isAfter(actual.getFechaSeguimiento())) {
                ultimo.put(id, s);
            }
        }

        long cumplidos = 0;
        long enSeguimiento = 0;
        for (ObjetivoDepartamento a : asignaciones) {
            Seguimiento s = ultimo.get(a.getId());
            if (s == null) {
                continue;
            }
            if (s.getPorcentajeAvance().compareTo(CIEN) >= 0) {
                cumplidos++;
            } else {
                enSeguimiento++;
            }
        }

        lblObjetivosAsignados.setText(String.valueOf(asignaciones.size()));
        lblObjetivosSeguimiento.setText(String.valueOf(enSeguimiento));
        lblObjetivosCumplidos.setText(String.valueOf(cumplidos));
    }

    /** Últimos seguimientos y evaluaciones registrados, del más reciente al más antiguo. */
    private void configurarActividad() {

        UiUtil.columna(colFecha, a -> UiUtil.fecha(a.fecha()));
        UiUtil.columna(colActividad, Actividad::actividad);
        UiUtil.columna(colResponsable, Actividad::responsable);
        UiUtil.columna(colEstado, Actividad::estado);

        List<Actividad> actividades = new ArrayList<>();

        for (Seguimiento s : seguimientos) {
            actividades.add(new Actividad(
                    s.getFechaSeguimiento(),
                    "Avance de \"" + s.getIdAsignacion().getIdObjetivo().getNombre() + "\"",
                    s.getRegistradoPor().getNombreCompleto(),
                    s.getPorcentajeAvance().stripTrailingZeros().toPlainString() + " %"));
        }

        for (Evaluacion e : evaluacionService.listarConDetalle()) {
            String evaluado = e.getIdEmpleado() != null
                    ? e.getIdEmpleado().getNombreCompleto()
                    : e.getIdDepartamento().getNombre();
            actividades.add(new Actividad(
                    e.getFechaEvaluacion(),
                    "Evaluación " + e.getTipo().toLowerCase() + " de " + evaluado,
                    e.getEvaluadoPor().getNombreCompleto(),
                    e.getEstado()));
        }

        actividades.sort(Comparator.comparing(Actividad::fecha,
                Comparator.nullsLast(Comparator.reverseOrder())));

        tablaActividad.setItems(FXCollections.observableArrayList(
                actividades.subList(0, Math.min(MAX_ACTIVIDADES, actividades.size()))));
        tablaActividad.setPlaceholder(new Label("No hay actividades registradas."));
    }
}
