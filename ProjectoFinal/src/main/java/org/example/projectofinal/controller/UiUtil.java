package org.example.projectofinal.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextInputControl;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/** Métodos de apoyo compartidos por las pantallas de administración. */
final class UiUtil {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private UiUtil() {
    }

    static void info(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Información", mensaje);
    }

    static void error(String mensaje) {
        mostrar(Alert.AlertType.WARNING, "Revisa los datos", mensaje);
    }

    static boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje,
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar");
        alerta.setHeaderText(null);
        return alerta.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    /** Convierte un objeto a texto para mostrarlo en un ComboBox. */
    static <T> StringConverter<T> conversor(Function<T, String> texto) {
        return new StringConverter<>() {
            @Override
            public String toString(T valor) {
                return valor == null ? "" : texto.apply(valor);
            }

            @Override
            public T fromString(String s) {
                return null;
            }
        };
    }

    /** Llena una columna de tabla con el texto que devuelve la función. */
    static <T> void columna(TableColumn<T, String> columna, Function<T, String> texto) {
        columna.setCellValueFactory(data -> {
            String valor = texto.apply(data.getValue());
            return new SimpleStringProperty(valor == null ? "" : valor);
        });
    }

    /** Texto del campo sin espacios extremos, o null si está vacío. */
    static String texto(TextInputControl campo) {
        String valor = campo.getText();
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    /** Número del campo, o null si está vacío. Lanza NumberFormatException si no es número. */
    static BigDecimal numero(TextInputControl campo) {
        String valor = texto(campo);
        return valor == null ? null : new BigDecimal(valor.replace(',', '.'));
    }

    static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    static String estado(Boolean activo) {
        return Boolean.TRUE.equals(activo) ? "Activo" : "Inactivo";
    }

    static String valor(Object valor) {
        return valor == null ? "" : valor.toString();
    }
}
