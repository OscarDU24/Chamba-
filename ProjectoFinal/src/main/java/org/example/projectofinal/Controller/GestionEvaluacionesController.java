package org.example.projectofinal.Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.projectofinal.model.CriterioPrueba;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GestionEvaluacionesController {
    private final javafx.collections.ObservableList<CriterioPrueba> criterios = FXCollections.observableArrayList();
    private final java.util.Map<String, List<CriterioPrueba>> configuraciones = new java.util.LinkedHashMap<>();

    @FXML private TableView<CriterioPrueba> tablaCriterios;
    @FXML private ComboBox<String> rolCombo;
    @FXML private Label mensaje;

    @FXML
    private void initialize() {
        rolCombo.setItems(FXCollections.observableArrayList("Docentes", "Mantenimiento", "Seguridad", "Servicios_Estudiantes"));
        rolCombo.getSelectionModel().selectFirst();

        TableColumn<CriterioPrueba, String> criterio = new TableColumn<>("Punto de evaluación");
        criterio.setCellValueFactory(cell -> cell.getValue().nombreProperty());
        criterio.setPrefWidth(360);

        TableColumn<CriterioPrueba, String> tipo = new TableColumn<>("Tipo de respuesta");
        tipo.setCellValueFactory(cell -> cell.getValue().tipoTextoProperty());
        tipo.setPrefWidth(210);

        TableColumn<CriterioPrueba, String> opciones = new TableColumn<>("Opciones configuradas");
        opciones.setCellValueFactory(cell -> cell.getValue().opcionesTextoProperty());
        opciones.setPrefWidth(360);

        TableColumn<CriterioPrueba, String> ponderacion = new TableColumn<>("Ponderación");
        ponderacion.setCellValueFactory(cell -> cell.getValue().ponderacionTextoProperty());
        ponderacion.setPrefWidth(120);

        tablaCriterios.getColumns().setAll(criterio, tipo, opciones, ponderacion);
        tablaCriterios.setItems(criterios);
        cargarEjemplo();
        actualizarDetallePonderacion();

        rolCombo.valueProperty().addListener((observable, anterior, nuevo) -> {
            if (anterior != null) configuraciones.put(anterior, new ArrayList<>(criterios));
            criterios.setAll(configuraciones.getOrDefault(nuevo, List.of()));
            actualizarDetallePonderacion();
        });
    }

    private void cargarEjemplo() {
        for (String rol : rolCombo.getItems()) {
            configuraciones.put(rol, new ArrayList<>(List.of(
                    new CriterioPrueba("Cumple con sus responsabilidades", CriterioPrueba.Tipo.SATISFACCION, List.of()),
                    new CriterioPrueba("¿Cómo califica su comunicación?", CriterioPrueba.Tipo.OPCION_MULTIPLE,
                            List.of("Excelente", "Buena", "Regular", "Necesita mejorar")),
                    new CriterioPrueba("Comentarios adicionales", CriterioPrueba.Tipo.TEXTO_LIBRE, List.of())
            )));
        }
        criterios.setAll(configuraciones.get(rolCombo.getValue()));
    }

    @FXML private void nuevoCriterio() {
        if (100.0 - sumaPonderaciones() < 1.0) {
            aviso("La ponderación de esta evaluación ya llegó al 100%.");
            return;
        }
        editar(null);
    }

    @FXML private void editarCriterio() {
        CriterioPrueba seleccionado = tablaCriterios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            aviso("Selecciona un punto para editar.");
            return;
        }
        editar(seleccionado);
    }

    @FXML private void eliminarCriterio() {
        CriterioPrueba seleccionado = tablaCriterios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            aviso("Selecciona un punto para eliminar.");
            return;
        }
        criterios.remove(seleccionado);
        guardarConfiguracionActual();
        actualizarDetallePonderacion();
        mensaje.setText("Punto eliminado. " + detallePonderacion());
    }

    private void editar(CriterioPrueba existente) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existente == null ? "Nuevo punto" : "Editar punto");
        dialog.setHeaderText("Configura el criterio y su mecanismo de respuesta");

        ButtonType aceptar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(aceptar, ButtonType.CANCEL);

        TextField nombre = new TextField(existente == null ? "" : existente.getNombre());
        nombre.setPromptText("Nombre o pregunta");

        ComboBox<CriterioPrueba.Tipo> tipo = new ComboBox<>(FXCollections.observableArrayList(CriterioPrueba.Tipo.values()));
        tipo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(CriterioPrueba.Tipo value) {
                return value == null ? "" : CriterioPrueba.etiqueta(value);
            }
            @Override public CriterioPrueba.Tipo fromString(String value) { return null; }
        });
        tipo.setValue(existente == null ? CriterioPrueba.Tipo.SATISFACCION : existente.getTipo());

        Spinner<Integer> cantidad = new Spinner<>(2, 10,
                existente == null || existente.getOpciones().isEmpty() ? 2 : existente.getOpciones().size());
        cantidad.setEditable(true);

        CheckBox multiples = new CheckBox("Permitir seleccionar varias respuestas");
        multiples.setSelected(existente != null && existente.isMultiplesRespuestas());

        double ponderacionDisponibleCalculada = existente == null
                ? 100.0 - sumaPonderaciones()
                : 100.0 - sumaPonderacionesExcluyendo(existente);
        final double ponderacionDisponible = Math.max(1.0, ponderacionDisponibleCalculada);

        double valorPonderacion = existente == null ? 1.0
                : Math.max(1.0, Math.min(ponderacionDisponible, existente.getPonderacion()));
        Spinner<Double> ponderacion = new Spinner<>(1.0, ponderacionDisponible, valorPonderacion, 1.0);
        ponderacion.setEditable(true);
        ponderacion.setPrefWidth(120);

        Label detallePonderacion = new Label(
                String.format(java.util.Locale.ROOT,
                        "Disponible para esta evaluación: %.1f%%", ponderacionDisponible));

        ponderacion.getEditor().textProperty().addListener((observado, anterior, nuevo) -> {
            try {
                double valor = Double.parseDouble(nuevo.trim());
                if (valor >= 1.0 && valor <= ponderacionDisponible) {
                    ponderacion.getValueFactory().setValue(valor);
                }
            } catch (NumberFormatException ignored) { }
        });

        VBox camposOpciones = new VBox(6);
        List<TextField> opciones = new ArrayList<>();
        Runnable actualizarOpciones = () -> {
            int n;
            try {
                n = Math.max(2, Math.min(10, cantidad.getValue()));
            } catch (Exception ex) {
                n = 2;
            }

            while (opciones.size() < n) {
                int index = opciones.size();
                TextField campo = new TextField(existente != null && index < existente.getOpciones().size()
                        ? existente.getOpciones().get(index) : "");
                campo.setPromptText("Respuesta " + (index + 1));
                opciones.add(campo);
                camposOpciones.getChildren().add(campo);
            }
            while (opciones.size() > n) {
                TextField removido = opciones.remove(opciones.size() - 1);
                camposOpciones.getChildren().remove(removido);
            }
        };
        actualizarOpciones.run();

        cantidad.valueProperty().addListener((observado, anterior, nuevo) -> actualizarOpciones.run());
        cantidad.getEditor().textProperty().addListener((observado, anterior, nuevo) -> {
            try {
                cantidad.getValueFactory().setValue(Math.max(2, Math.min(10, Integer.parseInt(nuevo))));
            } catch (NumberFormatException ignored) { }
        });

        VBox bloqueOpciones = new VBox(8,
                new Label("Cantidad de opciones (2 a 10)"), cantidad, camposOpciones, multiples);
        bloqueOpciones.visibleProperty().bind(tipo.valueProperty().isEqualTo(CriterioPrueba.Tipo.OPCION_MULTIPLE));
        bloqueOpciones.managedProperty().bind(bloqueOpciones.visibleProperty());

        Label ayuda = new Label("La escala de satisfacción usa cinco niveles; texto libre permite escribir una respuesta.");
        VBox contenido = new VBox(10,
                new Label("Pregunta / criterio"), nombre,
                new Label("Tipo de respuesta"), tipo,
                new Label("Ponderación (1 a 100)"), ponderacion,
                detallePonderacion,
                bloqueOpciones, ayuda);
        contenido.setPrefWidth(460);

        ScrollPane desplazamiento = new ScrollPane(contenido);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        desplazamiento.setPrefViewportHeight(430);
        dialog.getDialogPane().setContent(desplazamiento);

        dialog.getDialogPane().lookupButton(aceptar).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (nombre.getText().isBlank()) {
                aviso("Escribe el nombre del punto.");
                event.consume();
                return;
            }

            if (ponderacion.getValue() < 1.0 || ponderacion.getValue() > 100.0) {
                aviso("La ponderación debe estar entre 1 y 100.");
                event.consume();
                return;
            }

            double base = sumaPonderacionesExcluyendo(existente);
            if (base + ponderacion.getValue() > 100.000001) {
                double disponible = Math.max(0.0, 100.0 - base);
                aviso(String.format(java.util.Locale.ROOT,
                        "La evaluación no puede superar 100%%. Disponible: %.1f%%", disponible));
                event.consume();
                return;
            }

            if (tipo.getValue() == CriterioPrueba.Tipo.OPCION_MULTIPLE) {
                List<String> valores = opciones.stream()
                        .map(TextField::getText)
                        .map(String::trim)
                        .filter(texto -> !texto.isBlank())
                        .distinct()
                        .toList();
                boolean hayCamposVacios = opciones.stream()
                        .map(TextField::getText)
                        .anyMatch(texto -> texto.isBlank());

                if (valores.size() < 2 || hayCamposVacios) {
                    aviso("Escribe al menos dos opciones distintas y completa todos los campos.");
                    event.consume();
                }
            }
        });

        Optional<ButtonType> resultado = dialog.showAndWait();
        if (resultado.isEmpty() || resultado.get() != aceptar) return;

        List<String> lista = tipo.getValue() == CriterioPrueba.Tipo.OPCION_MULTIPLE
                ? opciones.stream().map(campo -> campo.getText().trim()).filter(texto -> !texto.isBlank()).toList()
                : List.of();
        double peso = ponderacion.getValue();

        if (existente == null) {
            criterios.add(new CriterioPrueba(nombre.getText().trim(), tipo.getValue(), lista, peso, multiples.isSelected()));
        } else {
            existente.setNombre(nombre.getText().trim());
            existente.setTipo(tipo.getValue(), lista, multiples.isSelected());
            existente.setPonderacion(peso);
            tablaCriterios.refresh();
        }

        guardarConfiguracionActual();
        actualizarDetallePonderacion();
        mensaje.setText("Punto guardado. " + detallePonderacion());
    }

    private void guardarConfiguracionActual() {
        if (rolCombo.getValue() != null) {
            configuraciones.put(rolCombo.getValue(), new ArrayList<>(criterios));
        }
    }

    @FXML private void iniciarPrueba() {
        if (criterios.isEmpty()) {
            aviso("Agrega al menos un punto antes de iniciar la prueba.");
            return;
        }

        double total = sumaPonderaciones();
        if (Math.abs(total - 100.0) > 0.000001) {
            aviso(String.format(java.util.Locale.ROOT,
                    "La prueba no puede iniciarse: la ponderación total es %.1f%%. Debe ser exactamente 100%%.",
                    total));
            actualizarDetallePonderacion();
            return;
        }

        URL prueba = getClass().getResource("/org/example/projectofinal/view/evaluaciones/PruebaEvaluacion.fxml");
        URL css = getClass().getResource("/org/example/projectofinal/view/evaluaciones/evaluaciones.css");
        if (prueba == null) {
            aviso("No se encontró PruebaEvaluacion.fxml.");
            return;
        }
        if (css == null) {
            aviso("No se encontró evaluaciones.css.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(prueba);
            Parent root = loader.load();
            PruebaEvaluacionController controller = loader.getController();
            controller.configurar(rolCombo.getValue(), new ArrayList<>(criterios));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Prueba de evaluación · " + rolCombo.getValue());

            Scene scene = new Scene(root, 850, 650);
            scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            aviso("No se pudo abrir la prueba: " + e.getMessage());
        }
    }

    private double sumaPonderaciones() {
        return criterios.stream().mapToDouble(CriterioPrueba::getPonderacion).sum();
    }

    private double sumaPonderacionesExcluyendo(CriterioPrueba excluido) {
        return criterios.stream()
                .filter(criterio -> criterio != excluido)
                .mapToDouble(CriterioPrueba::getPonderacion)
                .sum();
    }

    private String detallePonderacion() {
        double usada = sumaPonderaciones();
        double disponible = Math.max(0.0, 100.0 - usada);
        return String.format(java.util.Locale.ROOT,
                "Ponderación total: %.1f%% · Disponible: %.1f%%", usada, disponible);
    }

    private void actualizarDetallePonderacion() {
        mensaje.setText(detallePonderacion());
    }

    private void aviso(String texto) {
        new Alert(Alert.AlertType.WARNING, texto, ButtonType.OK).showAndWait();
    }
}
