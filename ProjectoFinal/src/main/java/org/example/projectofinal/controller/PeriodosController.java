package org.example.projectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.projectofinal.model.PeriodoEvaluacion;
import org.example.projectofinal.service.PeriodoEvaluacionService;

public class PeriodosController {

    /** Valores permitidos por la base (CHECK en periodos_evaluacion.estado). */
    private static final String[] ESTADOS = {"PLANIFICADO", "EN_CURSO", "CERRADO"};

    @FXML private TextField txtNombre;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TextArea txtDescripcion;
    @FXML private Button btnGuardar;

    @FXML private TableView<PeriodoEvaluacion> tabla;
    @FXML private TableColumn<PeriodoEvaluacion, String> colNombre;
    @FXML private TableColumn<PeriodoEvaluacion, String> colInicio;
    @FXML private TableColumn<PeriodoEvaluacion, String> colFin;
    @FXML private TableColumn<PeriodoEvaluacion, String> colEstado;
    @FXML private TableColumn<PeriodoEvaluacion, String> colDescripcion;
    @FXML private Label lblEstado;

    private final PeriodoEvaluacionService service = new PeriodoEvaluacionService();
    private PeriodoEvaluacion seleccionado;

    @FXML
    public void initialize() {
        cmbEstado.setItems(FXCollections.observableArrayList(ESTADOS));
        cmbEstado.setValue(ESTADOS[0]);

        UiUtil.columna(colNombre, PeriodoEvaluacion::getNombre);
        UiUtil.columna(colInicio, p -> UiUtil.fecha(p.getFechaInicio()));
        UiUtil.columna(colFin, p -> UiUtil.fecha(p.getFechaFin()));
        UiUtil.columna(colEstado, PeriodoEvaluacion::getEstado);
        UiUtil.columna(colDescripcion, PeriodoEvaluacion::getDescripcion);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, ant, nuevo) -> {
            if (nuevo != null) {
                seleccionado = nuevo;
                txtNombre.setText(UiUtil.valor(nuevo.getNombre()));
                dpInicio.setValue(nuevo.getFechaInicio());
                dpFin.setValue(nuevo.getFechaFin());
                cmbEstado.setValue(nuevo.getEstado());
                txtDescripcion.setText(UiUtil.valor(nuevo.getDescripcion()));
                btnGuardar.setText("Guardar cambios");
            }
        });

        actualizarTabla();
    }

    @FXML
    private void actualizarTabla() {
        tabla.setItems(FXCollections.observableArrayList(service.listarOrdenados()));
        lblEstado.setText(tabla.getItems().size() + " período(s) registrados.");
    }

    @FXML
    private void limpiarFormulario() {
        seleccionado = null;
        txtNombre.clear();
        dpInicio.setValue(null);
        dpFin.setValue(null);
        cmbEstado.setValue(ESTADOS[0]);
        txtDescripcion.clear();
        btnGuardar.setText("Registrar período");
        tabla.getSelectionModel().clearSelection();
    }

    @FXML
    private void guardar() {
        PeriodoEvaluacion p = new PeriodoEvaluacion();
        if (seleccionado != null) {
            p.setId(seleccionado.getId());
        }
        p.setNombre(UiUtil.texto(txtNombre));
        p.setFechaInicio(dpInicio.getValue());
        p.setFechaFin(dpFin.getValue());
        p.setEstado(cmbEstado.getValue());
        p.setDescripcion(UiUtil.texto(txtDescripcion));

        String error = service.validar(p);
        if (error != null) {
            UiUtil.error(error);
            return;
        }

        boolean nuevo = seleccionado == null;
        boolean ok = nuevo ? service.registrar(p) : service.modificar(p);

        if (!ok) {
            UiUtil.error("No se pudo guardar el período.");
            actualizarTabla();
            return;
        }

        limpiarFormulario();
        actualizarTabla();
        lblEstado.setText(nuevo ? "Período registrado." : "Período actualizado.");
    }
}
