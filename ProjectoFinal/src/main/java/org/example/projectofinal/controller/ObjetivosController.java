package org.example.projectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.model.Objetivo;
import org.example.projectofinal.model.ObjetivoDepartamento;
import org.example.projectofinal.model.PeriodoEvaluacion;
import org.example.projectofinal.service.DepartamentoService;
import org.example.projectofinal.service.ObjetivoDepartamentoService;
import org.example.projectofinal.service.ObjetivoService;
import org.example.projectofinal.service.PeriodoEvaluacionService;
import org.example.projectofinal.service.ResponsableObjetivoService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ObjetivosController {

    // ---- Formulario del objetivo ----
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbNivel;
    @FXML private ComboBox<Objetivo> cmbPadre;
    @FXML private TextField txtIndicador;
    @FXML private TextField txtMeta;
    @FXML private TextField txtUnidad;
    @FXML private TextArea txtDescripcion;
    @FXML private Button btnGuardar;

    @FXML private TableView<Objetivo> tablaObjetivos;
    @FXML private TableColumn<Objetivo, String> colNombre;
    @FXML private TableColumn<Objetivo, String> colNivel;
    @FXML private TableColumn<Objetivo, String> colPadre;
    @FXML private TableColumn<Objetivo, String> colIndicador;
    @FXML private TableColumn<Objetivo, String> colMeta;
    @FXML private TableColumn<Objetivo, String> colEstado;

    // ---- Asignación a departamentos ----
    @FXML private ComboBox<Departamento> cmbDepartamento;
    @FXML private ComboBox<PeriodoEvaluacion> cmbPeriodo;

    @FXML private TableView<ObjetivoDepartamento> tablaAsignaciones;
    @FXML private TableColumn<ObjetivoDepartamento, String> colAsigObjetivo;
    @FXML private TableColumn<ObjetivoDepartamento, String> colAsigDepartamento;
    @FXML private TableColumn<ObjetivoDepartamento, String> colAsigPeriodo;
    @FXML private TableColumn<ObjetivoDepartamento, String> colAsigFecha;
    @FXML private TableColumn<ObjetivoDepartamento, String> colAsigParticipantes;

    @FXML private Label lblEstado;

    private final ObjetivoService objetivoService = new ObjetivoService();
    private final ObjetivoDepartamentoService asignacionService = new ObjetivoDepartamentoService();
    private final DepartamentoService departamentoService = new DepartamentoService();
    private final PeriodoEvaluacionService periodoService = new PeriodoEvaluacionService();
    private final ResponsableObjetivoService responsableService = new ResponsableObjetivoService();

    private Objetivo seleccionado;
    private Map<UUID, Long> participantes = Map.of();

    @FXML
    public void initialize() {
        cmbNivel.setItems(FXCollections.observableArrayList(
                ObjetivoService.GENERAL, ObjetivoService.ESPECIFICO));
        cmbNivel.setValue(ObjetivoService.GENERAL);
        cmbPadre.setConverter(UiUtil.conversor(Objetivo::getNombre));
        cmbPadre.disableProperty().bind(cmbNivel.valueProperty().isNotEqualTo(ObjetivoService.ESPECIFICO));
        cmbNivel.valueProperty().addListener((obs, ant, nuevo) -> {
            if (ObjetivoService.GENERAL.equals(nuevo)) {
                cmbPadre.setValue(null);
            }
        });

        cmbDepartamento.setConverter(UiUtil.conversor(Departamento::getNombre));
        cmbPeriodo.setConverter(UiUtil.conversor(p -> p.getNombre() + " (" + p.getEstado() + ")"));

        UiUtil.columna(colNombre, Objetivo::getNombre);
        UiUtil.columna(colNivel, Objetivo::getNivel);
        UiUtil.columna(colPadre, o -> o.getIdObjetivoPadre() == null ? "" : o.getIdObjetivoPadre().getNombre());
        UiUtil.columna(colIndicador, Objetivo::getIndicador);
        UiUtil.columna(colMeta, o -> o.getMeta() == null ? ""
                : o.getMeta().stripTrailingZeros().toPlainString() + " " + UiUtil.valor(o.getUnidadMedida()));
        UiUtil.columna(colEstado, o -> UiUtil.estado(o.getActivo()));

        UiUtil.columna(colAsigObjetivo, a -> a.getIdObjetivo().getNombre());
        UiUtil.columna(colAsigDepartamento, a -> a.getIdDepartamento().getNombre());
        UiUtil.columna(colAsigPeriodo, a -> a.getIdPeriodo().getNombre());
        UiUtil.columna(colAsigFecha, a -> UiUtil.fecha(a.getFechaAsignacion()));
        UiUtil.columna(colAsigParticipantes, a -> String.valueOf(participantes.getOrDefault(a.getId(), 0L)));

        tablaObjetivos.getSelectionModel().selectedItemProperty().addListener((obs, ant, nuevo) -> {
            if (nuevo != null) {
                cargarFormulario(nuevo);
            }
        });

        actualizarTodo();
    }

    @FXML
    private void actualizarTodo() {
        List<Objetivo> objetivos = objetivoService.listarConDetalle();
        tablaObjetivos.setItems(FXCollections.observableArrayList(objetivos));

        cmbPadre.setItems(FXCollections.observableArrayList(objetivos.stream()
                .filter(o -> ObjetivoService.GENERAL.equals(o.getNivel()))
                .filter(o -> Boolean.TRUE.equals(o.getActivo()))
                .toList()));

        cmbDepartamento.setItems(FXCollections.observableArrayList(departamentoService.listarActivos()));
        cmbPeriodo.setItems(FXCollections.observableArrayList(periodoService.listarOrdenados()));
        participantes = responsableService.contarPorAsignacion();
        tablaAsignaciones.setItems(FXCollections.observableArrayList(asignacionService.listarConDetalle()));

        lblEstado.setText(objetivos.size() + " objetivo(s) y "
                + tablaAsignaciones.getItems().size() + " asignación(es).");
    }

    private void cargarFormulario(Objetivo o) {
        seleccionado = o;
        txtNombre.setText(UiUtil.valor(o.getNombre()));
        cmbNivel.setValue(o.getNivel());
        cmbPadre.setValue(o.getIdObjetivoPadre() == null ? null
                : cmbPadre.getItems().stream()
                    .filter(p -> p.getId().equals(o.getIdObjetivoPadre().getId()))
                    .findFirst().orElse(null));
        txtIndicador.setText(UiUtil.valor(o.getIndicador()));
        txtMeta.setText(o.getMeta() == null ? "" : o.getMeta().stripTrailingZeros().toPlainString());
        txtUnidad.setText(UiUtil.valor(o.getUnidadMedida()));
        txtDescripcion.setText(UiUtil.valor(o.getDescripcion()));
        btnGuardar.setText("Guardar cambios");
    }

    @FXML
    private void limpiarFormulario() {
        seleccionado = null;
        txtNombre.clear();
        cmbNivel.setValue(ObjetivoService.GENERAL);
        cmbPadre.setValue(null);
        txtIndicador.clear();
        txtMeta.clear();
        txtUnidad.clear();
        txtDescripcion.clear();
        btnGuardar.setText("Registrar objetivo");
        tablaObjetivos.getSelectionModel().clearSelection();
    }

    @FXML
    private void guardar() {
        BigDecimal meta;
        try {
            meta = UiUtil.numero(txtMeta);
        } catch (NumberFormatException e) {
            UiUtil.error("La meta debe ser un número.");
            return;
        }

        String nombre = UiUtil.texto(txtNombre);

        Objetivo o = seleccionado == null ? new Objetivo() : seleccionado;
        o.setNombre(nombre);
        o.setNivel(cmbNivel.getValue());
        o.setIdObjetivoPadre(ObjetivoService.ESPECIFICO.equals(cmbNivel.getValue()) ? cmbPadre.getValue() : null);
        o.setIndicador(UiUtil.texto(txtIndicador));
        o.setMeta(meta);
        o.setUnidadMedida(UiUtil.texto(txtUnidad));
        o.setDescripcion(UiUtil.texto(txtDescripcion));

        String error = objetivoService.validar(o);
        if (error != null) {
            UiUtil.error(error);
            actualizarTodo();
            return;
        }

        boolean nuevo = seleccionado == null;
        boolean ok = nuevo ? objetivoService.registrar(o) : objetivoService.modificar(o);

        if (!ok) {
            UiUtil.error("No se pudo guardar el objetivo.");
            actualizarTodo();
            return;
        }

        limpiarFormulario();
        actualizarTodo();
        lblEstado.setText(nuevo ? "Objetivo registrado." : "Objetivo actualizado.");
    }

    @FXML
    private void cambiarEstado() {
        Objetivo o = tablaObjetivos.getSelectionModel().getSelectedItem();
        if (o == null) {
            UiUtil.error("Selecciona un objetivo de la tabla.");
            return;
        }

        String accion = Boolean.TRUE.equals(o.getActivo()) ? "desactivar" : "activar";
        if (!UiUtil.confirmar("¿Deseas " + accion + " el objetivo " + o.getNombre() + "?")) {
            return;
        }

        if (objetivoService.cambiarEstado(o.getId())) {
            limpiarFormulario();
            actualizarTodo();
        } else {
            UiUtil.error("No se pudo cambiar el estado.");
        }
    }

    @FXML
    private void asignar() {
        ObjetivoDepartamento a = new ObjetivoDepartamento();
        a.setIdObjetivo(tablaObjetivos.getSelectionModel().getSelectedItem());
        a.setIdDepartamento(cmbDepartamento.getValue());
        a.setIdPeriodo(cmbPeriodo.getValue());
        a.setFechaAsignacion(LocalDate.now());

        String error = asignacionService.validar(a);
        if (error != null) {
            UiUtil.error(error);
            return;
        }

        if (!asignacionService.asignar(a)) {
            UiUtil.error("No se pudo asignar el objetivo.");
            return;
        }

        // Todos los integrantes del departamento participan en el objetivo
        int agregados = responsableService.sincronizarConDepartamento(a.getId());
        actualizarTodo();

        if (agregados < 0) {
            UiUtil.error("El objetivo se asignó, pero no se pudieron agregar los participantes. "
                    + "Usa \"Actualizar participantes\".");
        } else {
            lblEstado.setText("Objetivo asignado a " + a.getIdDepartamento().getNombre()
                    + " con " + agregados + " participante(s).");
        }
    }

    /** Agrega a la asignación los empleados que entraron después al departamento. */
    @FXML
    private void actualizarParticipantes() {
        ObjetivoDepartamento a = tablaAsignaciones.getSelectionModel().getSelectedItem();

        if (a == null) {
            UiUtil.error("Selecciona una asignación en la tabla de abajo.");
            return;
        }

        int agregados = responsableService.sincronizarConDepartamento(a.getId());
        actualizarTodo();

        if (agregados < 0) {
            UiUtil.error("No se pudieron actualizar los participantes.");
        } else {
            lblEstado.setText(agregados == 0
                    ? "Los participantes ya estaban al día."
                    : "Se agregaron " + agregados + " participante(s).");
        }
    }
}
