package org.example.projectofinal.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

/** Criterio editable en memoria para la demostración de evaluaciones. */
public class CriterioPrueba {
    public enum Tipo { OPCION_MULTIPLE, SATISFACCION, TEXTO_LIBRE }

    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty tipoTexto = new SimpleStringProperty();
    private final StringProperty opcionesTexto = new SimpleStringProperty();
    private final StringProperty ponderacionTexto = new SimpleStringProperty();
    private Tipo tipo;
    private List<String> opciones;
    private double ponderacion;
    private boolean multiplesRespuestas;

    public CriterioPrueba(String nombre, Tipo tipo, List<String> opciones) {
        this(nombre, tipo, opciones, 1, false);
    }

    public CriterioPrueba(String nombre, Tipo tipo, List<String> opciones, double ponderacion, boolean multiplesRespuestas) {
        this.nombre.set(nombre);
        this.tipo = tipo;
        this.tipoTexto.set(etiqueta(tipo));
        this.opciones = new ArrayList<>(opciones);
        this.opcionesTexto.set(String.join(", ", opciones));
        setPonderacion(ponderacion);
        this.multiplesRespuestas = multiplesRespuestas;
    }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String value) { nombre.set(value); }
    public StringProperty nombreProperty() { return nombre; }
    public String getTipoTexto() { return tipoTexto.get(); }
    public StringProperty tipoTextoProperty() { return tipoTexto; }
    public Tipo getTipo() { return tipo; }
    public List<String> getOpciones() { return List.copyOf(opciones); }
    public double getPonderacion() { return ponderacion; }
    public String getPonderacionTexto() { return ponderacionTexto.get(); }
    public StringProperty ponderacionTextoProperty() { return ponderacionTexto; }
    public void setPonderacion(double value) {
        if (!Double.isFinite(value) || value < 1.0 || value > 100.0) {
            throw new IllegalArgumentException("La ponderación debe estar entre 1 y 100.");
        }
        ponderacion = value;
        ponderacionTexto.set(String.format(java.util.Locale.ROOT, "%s", value));
    }
    public boolean isMultiplesRespuestas() { return multiplesRespuestas; }

    public void setTipo(Tipo tipo, List<String> opciones) {
        this.tipo = tipo;
        this.tipoTexto.set(etiqueta(tipo));
        this.opciones = new ArrayList<>(opciones);
        this.opcionesTexto.set(String.join(", ", opciones));
    }

    public void setTipo(Tipo tipo, List<String> opciones, boolean multiplesRespuestas) {
        setTipo(tipo, opciones);
        this.multiplesRespuestas = multiplesRespuestas;
    }

    public String getOpcionesTexto() { return opcionesTexto.get(); }
    public StringProperty opcionesTextoProperty() { return opcionesTexto; }

    public static String etiqueta(Tipo tipo) {
        return switch (tipo) {
            case OPCION_MULTIPLE -> "Opción múltiple";
            case SATISFACCION -> "Satisfacción (1 a 5)";
            case TEXTO_LIBRE -> "Texto libre";
        };
    }
}
