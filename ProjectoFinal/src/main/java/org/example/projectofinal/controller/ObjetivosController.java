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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    @FXML private Label lblEstado;

    private final ObjetivoService objetivoService = new ObjetivoService();
    private final ObjetivoDepartamentoService asignacionService = new ObjetivoDepartamentoService();
    private final DepartamentoService departamentoService = new DepartamentoService();
    private final PeriodoEvaluacionService periodoService = new PeriodoEvaluacionService();

    private Objetivo seleccionado;

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
        if (nombre != null && nombre.length() > 150) {
            UiUtil.error("El nombre no puede tener más de 150 caracteres.");
            return;
        }

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
        Objetivo o = tablaObjetivos.getSelectionModel().getSelectedItem();

        if (o == null) {
            UiUtil.error("Selecciona en la tabla el objetivo que quieres asignar.");
            return;
        }
        if (!Boolean.TRUE.equals(o.getActivo())) {
            UiUtil.error("No se puede asignar un objetivo inactivo.");
            return;
        }
        if (cmbDepartamento.getValue() == null || cmbPeriodo.getValue() == null) {
            UiUtil.error("Selecciona el departamento y el período.");
            return;
        }

        ObjetivoDepartamento a = new ObjetivoDepartamento();
        a.setIdObjetivo(o);
        a.setIdDepartamento(cmbDepartamento.getValue());
        a.setIdPeriodo(cmbPeriodo.getValue());
        a.setFechaAsignacion(LocalDate.now());

        if (asignacionService.existeAsignacion(a)) {
            UiUtil.error("Ese objetivo ya está asignado a ese departamento en ese período.");
            return;
        }

        if (asignacionService.asignar(a)) {
            actualizarTodo();
            lblEstado.setText("Objetivo asignado a " + cmbDepartamento.getValue().getNombre() + ".");
        } else {
            UiUtil.error("No se pudo asignar el objetivo.");
        }
    }
}
