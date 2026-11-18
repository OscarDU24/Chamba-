package org.example.projectofinal.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.projectofinal.model.CriterioPrueba;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;

public class PruebaEvaluacionController {
    @FXML private Label titulo;
    @FXML private VBox preguntas;
    private final List<Node> respuestas = new ArrayList<>();
    private final List<CriterioPrueba> criterios = new ArrayList<>();

    public void configurar(String rol, List<CriterioPrueba> puntos) {
        titulo.setText("Evaluación de " + rol);
        criterios.clear();
        criterios.addAll(puntos);
        preguntas.getChildren().clear(); respuestas.clear();
        for (int i = 0; i < puntos.size(); i++) {
            CriterioPrueba punto = puntos.get(i);
            Label pregunta = new Label((i + 1) + ". " + punto.getNombre());
            pregunta.getStyleClass().add("question-title");
            preguntas.getChildren().add(pregunta);
            Node respuesta;
            switch (punto.getTipo()) {
                case OPCION_MULTIPLE -> {
                    if (punto.isMultiplesRespuestas()) {
                        VBox lista = new VBox(6);
                        for (String opcion : punto.getOpciones()) lista.getChildren().add(new CheckBox(opcion));
                        respuesta = lista;
                    } else {
                        ComboBox<String> combo = new ComboBox<>();
                        combo.getItems().setAll(punto.getOpciones());
                        combo.setPromptText("Selecciona una respuesta");
                        combo.setMaxWidth(Double.MAX_VALUE);
                        respuesta = combo;
                    }
                }
                case SATISFACCION -> {
                    ComboBox<String> combo = new ComboBox<>();
                    combo.getItems().setAll("1 · Muy insatisfecho", "2 · Insatisfecho", "3 · Neutral", "4 · Satisfecho", "5 · Muy satisfecho");
                    combo.setPromptText("Selecciona un nivel");
                    combo.setMaxWidth(Double.MAX_VALUE);
                    respuesta = combo;
                }
                case TEXTO_LIBRE -> {
                    TextArea area = new TextArea();
                    area.setPromptText("Escribe tu respuesta"); area.setPrefRowCount(3); area.setWrapText(true);
                    respuesta = area;
                }
                default -> throw new IllegalStateException("Tipo de respuesta no soportado");
            }
            respuestas.add(respuesta);
            preguntas.getChildren().addAll(respuesta, new javafx.scene.layout.Region());
        }
    }

    @FXML private void guardarRespuestas() {
        for (int i = 0; i < respuestas.size(); i++) {
            Node respuesta = respuestas.get(i);

            boolean vacia = respuesta instanceof ComboBox<?> combo ? combo.getValue() == null
                    : respuesta instanceof TextArea area ? area.getText().isBlank()
                    : respuesta instanceof VBox lista && lista.getChildren().stream()
                            .filter(CheckBox.class::isInstance)
                            .map(CheckBox.class::cast)
                            .noneMatch(CheckBox::isSelected);
            if (vacia) {
                new Alert(Alert.AlertType.WARNING,
                        "Completa la respuesta: " + criterios.get(i).getNombre(),
                        ButtonType.OK).showAndWait();
                respuesta.requestFocus();
                return;
            }
        }
        new Alert(Alert.AlertType.INFORMATION, "Respuestas guardadas correctamente.", ButtonType.OK).showAndWait();
        ((javafx.stage.Stage) preguntas.getScene().getWindow()).close();
    }
}
