package org.example.projectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.projectofinal.model.CuentaUsuario;
import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.model.Evaluacion;
import org.example.projectofinal.model.PeriodoEvaluacion;
import org.example.projectofinal.service.AdminPanelService;
import org.example.projectofinal.service.DepartamentoService;
import org.example.projectofinal.service.EvaluacionService;
import org.example.projectofinal.service.PeriodoEvaluacionService;
import org.example.projectofinal.util.SesionUsuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Evaluación final mixta: combina el desempeño individual y el departamental.
 * ADMINISTRADOR calcula y registra; EMPLEADO solo consulta sus resultados.
 */
public class EvaluacionFinalController {

    /** Valores permitidos por la base (CHECK en evaluaciones.estado). */
    private static final String[] ESTADOS = {"PENDIENTE", "EN_PROCESO", "FINALIZADA"};

    @FXML private VBox panelFormulario;
    @FXML private ComboBox<PeriodoEvaluacion> cmbPeriodo;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private ComboBox<Departamento> cmbDepartamento;
    @FXML private ComboBox<Empleado> cmbEmpleado;
    @FXML private TextField txtPeso;
    @FXML private TextField txtIndividual;
    @FXML private TextField txtDepartamental;
    @FXML private TextField txtFinal;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TextArea txtObservaciones;
    @FXML private Label lblDetalleCalculo;

    @FXML private TableView<Evaluacion> tabla;
    @FXML private TableColumn<Evaluacion, String> colTipo;
    @FXML private TableColumn<Evaluacion, String> colEvaluado;
    @FXML private TableColumn<Evaluacion, String> colDepartamento;
    @FXML private TableColumn<Evaluacion, String> colIndividual;
    @FXML private TableColumn<Evaluacion, String> colDepartamental;
    @FXML private TableColumn<Evaluacion, String> colFinal;
    @FXML private TableColumn<Evaluacion, String> colEstado;
    @FXML private TableColumn<Evaluacion, String> colFecha;
    @FXML private TableColumn<Evaluacion, String> colEvaluador;
    @FXML private Label lblEstado;

    private final EvaluacionService service = new EvaluacionService();
    private final PeriodoEvaluacionService periodoService = new PeriodoEvaluacionService();
    private final DepartamentoService departamentoService = new DepartamentoService();
    private final AdminPanelService empleadoService = new AdminPanelService();

    private List<Empleado> empleadosActivos = List.of();
    private boolean soloLectura;

    @FXML
    public void initialize() {
        CuentaUsuario cuenta = SesionUsuario.getCuentaUsuario();
        soloLectura = cuenta == null || !"ADMINISTRADOR".equals(cuenta.getRol());

        cmbPeriodo.setConverter(UiUtil.conversor(p -> p.getNombre() + " (" + p.getEstado() + ")"));
        cmbDepartamento.setConverter(UiUtil.conversor(Departamento::getNombre));
        cmbEmpleado.setConverter(UiUtil.conversor(Empleado::getNombreCompleto));

        cmbTipo.setItems(FXCollections.observableArrayList(
                EvaluacionService.INDIVIDUAL, EvaluacionService.DEPARTAMENTAL));
        cmbTipo.setValue(EvaluacionService.INDIVIDUAL);
        cmbEstado.setItems(FXCollections.observableArrayList(ESTADOS));
        cmbEstado.setValue(ESTADOS[0]);
        txtPeso.setText(EvaluacionService.PESO_INDIVIDUAL_POR_DEFECTO.toPlainString());

        cmbEmpleado.disableProperty().bind(cmbTipo.valueProperty().isNotEqualTo(EvaluacionService.INDIVIDUAL));
        txtIndividual.disableProperty().bind(cmbTipo.valueProperty().isNotEqualTo(EvaluacionService.INDIVIDUAL));
        txtPeso.disableProperty().bind(cmbTipo.valueProperty().isNotEqualTo(EvaluacionService.INDIVIDUAL));

        cmbTipo.valueProperty().addListener((obs, ant, nuevo) -> limpiarPuntuaciones());
        cmbDepartamento.valueProperty().addListener((obs, ant, nuevo) -> {
            filtrarEmpleados();
            limpiarPuntuaciones();
        });
        cmbEmpleado.valueProperty().addListener((obs, ant, nuevo) -> limpiarPuntuaciones());
        cmbPeriodo.valueProperty().addListener((obs, ant, nuevo) -> {
            limpiarPuntuaciones();
            actualizarTabla();
        });

        UiUtil.columna(colTipo, Evaluacion::getTipo);
        UiUtil.columna(colEvaluado, e -> e.getIdEmpleado() != null
                ? e.getIdEmpleado().getNombreCompleto() : "(todo el departamento)");
        UiUtil.columna(colDepartamento, e -> e.getIdDepartamento().getNombre());
        UiUtil.columna(colIndividual, e -> numero(e.getPuntuacionIndividual()));
        UiUtil.columna(colDepartamental, e -> numero(e.getPuntuacionDepartamental()));
        UiUtil.columna(colFinal, e -> numero(e.getPuntuacionFinal()));
        UiUtil.columna(colEstado, Evaluacion::getEstado);
        UiUtil.columna(colFecha, e -> UiUtil.fecha(e.getFechaEvaluacion()));
        UiUtil.columna(colEvaluador, e -> e.getEvaluadoPor().getNombreCompleto());

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, ant, nueva) -> {
            if (nueva != null && !soloLectura) {
                cargarFormulario(nueva);
            }
        });

        if (soloLectura) {
            panelFormulario.setVisible(false);
            panelFormulario.setManaged(false);
        }

        actualizarTodo();
    }

    private static String numero(BigDecimal valor) {
        return valor == null ? "—" : valor.stripTrailingZeros().toPlainString();
    }

    @FXML
    private void actualizarTodo() {
        List<PeriodoEvaluacion> periodos = periodoService.listarOrdenados();
        PeriodoEvaluacion elegido = cmbPeriodo.getValue();
        cmbPeriodo.setItems(FXCollections.observableArrayList(periodos));

        if (elegido != null) {
            cmbPeriodo.setValue(periodos.stream()
                    .filter(p -> p.getId().equals(elegido.getId())).findFirst().orElse(null));
        } else {
            PeriodoEvaluacion actual = periodoService.buscarPeriodoActual();
            cmbPeriodo.setValue(actual != null ? periodos.stream()
                    .filter(p -> p.getId().equals(actual.getId())).findFirst().orElse(null)
                    : periodos.isEmpty() ? null : periodos.get(0));
        }

        if (!soloLectura) {
            empleadosActivos = empleadoService.listarEmpleados().stream()
                    .filter(e -> Boolean.TRUE.equals(e.getActivo()))
                    .toList();
            cmbDepartamento.setItems(FXCollections.observableArrayList(departamentoService.listarActivos()));
            filtrarEmpleados();
        }

        actualizarTabla();
    }

    private void actualizarTabla() {
        PeriodoEvaluacion periodo = cmbPeriodo.getValue();

        if (periodo == null) {
            tabla.getItems().clear();
            lblEstado.setText("No hay períodos de evaluación. Créalos en la pantalla Períodos.");
            return;
        }

        List<Evaluacion> evaluaciones = service.listarPorPeriodo(periodo.getId());

        if (soloLectura) {
            Empleado yo = SesionUsuario.getCuentaUsuario() == null ? null
                    : SesionUsuario.getCuentaUsuario().getIdEmpleado();
            evaluaciones = evaluaciones.stream()
                    .filter(e -> yo != null && e.getIdEmpleado() != null
                            && e.getIdEmpleado().getId().equals(yo.getId()))
                    .toList();
        }

        tabla.setItems(FXCollections.observableArrayList(evaluaciones));
        lblEstado.setText(evaluaciones.size() + " evaluación(es) en " + periodo.getNombre() + "."
                + (soloLectura ? " Solo se muestran tus resultados." : ""));
    }

    private void filtrarEmpleados() {
        Departamento dep = cmbDepartamento.getValue();
        Empleado elegido = cmbEmpleado.getValue();

        List<Empleado> lista = dep == null ? List.of() : empleadosActivos.stream()
                .filter(e -> e.getIdDepartamento() != null
                        && e.getIdDepartamento().getId().equals(dep.getId()))
                .toList();
        cmbEmpleado.setItems(FXCollections.observableArrayList(lista));

        if (elegido != null && lista.stream().anyMatch(e -> e.getId().equals(elegido.getId()))) {
            cmbEmpleado.setValue(lista.stream().filter(e -> e.getId().equals(elegido.getId())).findFirst().get());
        }
    }

    private void limpiarPuntuaciones() {
        txtIndividual.clear();
        txtDepartamental.clear();
        txtFinal.clear();
        lblDetalleCalculo.setText("");
    }

    private void cargarFormulario(Evaluacion e) {
        cmbTipo.setValue(e.getTipo());
        cmbDepartamento.setValue(cmbDepartamento.getItems().stream()
                .filter(d -> d.getId().equals(e.getIdDepartamento().getId())).findFirst().orElse(null));
        if (e.getIdEmpleado() != null) {
            cmbEmpleado.setValue(cmbEmpleado.getItems().stream()
                    .filter(x -> x.getId().equals(e.getIdEmpleado().getId())).findFirst().orElse(null));
        }
        txtIndividual.setText(e.getPuntuacionIndividual() == null ? "" : numero(e.getPuntuacionIndividual()));
        txtDepartamental.setText(e.getPuntuacionDepartamental() == null ? "" : numero(e.getPuntuacionDepartamental()));
        txtFinal.setText(e.getPuntuacionFinal() == null ? "" : numero(e.getPuntuacionFinal()));
        cmbEstado.setValue(e.getEstado());
        txtObservaciones.setText(UiUtil.valor(e.getObservaciones()));
        lblDetalleCalculo.setText("Evaluación existente cargada. Puedes recalcular o ajustar y guardar.");
    }

    @FXML
    private void limpiarFormulario() {
        cmbTipo.setValue(EvaluacionService.INDIVIDUAL);
        cmbDepartamento.setValue(null);
        cmbEmpleado.setValue(null);
        txtPeso.setText(EvaluacionService.PESO_INDIVIDUAL_POR_DEFECTO.toPlainString());
        cmbEstado.setValue(ESTADOS[0]);
        txtObservaciones.clear();
        limpiarPuntuaciones();
        tabla.getSelectionModel().clearSelection();
    }

    /** Calcula las puntuaciones a partir de los seguimientos registrados. */
    @FXML
    private void calcular() {
        PeriodoEvaluacion periodo = cmbPeriodo.getValue();
        Departamento dep = cmbDepartamento.getValue();
        boolean individual = EvaluacionService.INDIVIDUAL.equals(cmbTipo.getValue());

        if (periodo == null || dep == null) {
            UiUtil.error("Selecciona el período y el departamento.");
            return;
        }
        if (individual && cmbEmpleado.getValue() == null) {
            UiUtil.error("Selecciona el empleado a evaluar.");
            return;
        }

        BigDecimal peso = leerPeso();
        if (peso == null) {
            return;
        }

        BigDecimal dept = service.calcularDepartamental(periodo.getId(), dep.getId());
        BigDecimal indiv = individual
                ? service.calcularIndividual(periodo.getId(), cmbEmpleado.getValue().getId())
                : null;

        txtDepartamental.setText(dept == null ? "" : numero(dept));
        txtIndividual.setText(indiv == null ? "" : numero(indiv));
        recalcularFinal(peso);

        StringBuilder detalle = new StringBuilder();
        if (individual) {
            detalle.append(indiv == null
                    ? "El empleado no participa en objetivos de este período. "
                    : "Individual: promedio del último avance en sus objetivos. ");
        }
        detalle.append(dept == null
                ? "El departamento no tiene objetivos asignados en este período."
                : "Departamental: promedio del último avance de los objetivos del departamento.");
        detalle.append(" Los objetivos sin avance cuentan como 0.");
        lblDetalleCalculo.setText(detalle.toString());
    }

    /** Recalcula la final con lo escrito en los campos (permite ajustes manuales). */
    @FXML
    private void recalcularFinalManual() {
        BigDecimal peso = leerPeso();
        if (peso != null) {
            recalcularFinal(peso);
        }
    }

    private void recalcularFinal(BigDecimal peso) {
        try {
            BigDecimal indiv = UiUtil.numero(txtIndividual);
            BigDecimal dept = UiUtil.numero(txtDepartamental);
            boolean individual = EvaluacionService.INDIVIDUAL.equals(cmbTipo.getValue());

            BigDecimal fin = individual
                    ? service.calcularPuntuacionFinal(indiv, dept, peso)
                    : dept;
            txtFinal.setText(fin == null ? "" : numero(fin));
        } catch (NumberFormatException e) {
            UiUtil.error("Las puntuaciones deben ser números.");
        }
    }

    private BigDecimal leerPeso() {
        try {
            BigDecimal peso = UiUtil.numero(txtPeso);
            if (peso == null || peso.signum() < 0 || peso.compareTo(new BigDecimal("100")) > 0) {
                UiUtil.error("El peso individual debe estar entre 0 y 100.");
                return null;
            }
            return peso;
        } catch (NumberFormatException e) {
            UiUtil.error("El peso individual debe ser un número.");
            return null;
        }
    }

    @FXML
    private void guardar() {
        CuentaUsuario cuenta = SesionUsuario.getCuentaUsuario();
        PeriodoEvaluacion periodo = cmbPeriodo.getValue();
        Departamento dep = cmbDepartamento.getValue();
        String tipo = cmbTipo.getValue();
        boolean individual = EvaluacionService.INDIVIDUAL.equals(tipo);
        Empleado empleado = individual ? cmbEmpleado.getValue() : null;

        BigDecimal indiv;
        BigDecimal dept;
        BigDecimal fin;
        try {
            indiv = individual ? UiUtil.numero(txtIndividual) : null;
            dept = UiUtil.numero(txtDepartamental);
            fin = UiUtil.numero(txtFinal);
        } catch (NumberFormatException e) {
            UiUtil.error("Las puntuaciones deben ser números.");
            return;
        }

        if (fin == null) {
            UiUtil.error("Presiona \"Calcular\" o escribe la puntuación final antes de guardar.");
            return;
        }

        Evaluacion existente = (periodo == null || dep == null || (individual && empleado == null)) ? null
                : service.buscarExistente(periodo.getId(), tipo,
                    empleado == null ? null : empleado.getId(), dep.getId());

        Evaluacion e = existente != null ? existente : new Evaluacion();
        e.setIdPeriodo(periodo);
        e.setIdDepartamento(dep);
        e.setIdEmpleado(empleado);
        e.setTipo(tipo);
        e.setPuntuacionIndividual(indiv);
        e.setPuntuacionDepartamental(dept);
        e.setPuntuacionFinal(fin);
        e.setEstado(cmbEstado.getValue());
        e.setObservaciones(UiUtil.texto(txtObservaciones));
        e.setFechaEvaluacion(LocalDate.now());
        e.setEvaluadoPor(cuenta == null ? null : cuenta.getIdEmpleado());

        String error = service.validar(e);
        if (error != null) {
            UiUtil.error(error);
            return;
        }

        if (existente != null && !UiUtil.confirmar("Ya existe una evaluación "
                + tipo + " para " + (empleado != null ? empleado.getNombreCompleto() : dep.getNombre())
                + " en este período. ¿Deseas actualizarla?")) {
            return;
        }

        boolean ok = existente != null ? service.modificar(e) : service.registrar(e);

        if (ok) {
            limpiarFormulario();
            actualizarTabla();
            lblEstado.setText(existente == null ? "Evaluación registrada." : "Evaluación actualizada.");
        } else {
            UiUtil.error("No se pudo guardar la evaluación.");
        }
    }
}
