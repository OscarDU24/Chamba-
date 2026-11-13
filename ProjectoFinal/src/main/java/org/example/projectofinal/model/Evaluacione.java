package org.example.projectofinal.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "evaluaciones")
public class Evaluacione {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_evaluacion", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_periodo", nullable = false)
    private PeriodosEvaluacion idPeriodo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departamento", nullable = false)
    private Departamento idDepartamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado")
    private Empleado idEmpleado;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "puntuacion_individual", precision = 5, scale = 2)
    private BigDecimal puntuacionIndividual;

    @Column(name = "puntuacion_departamental", precision = 5, scale = 2)
    private BigDecimal puntuacionDepartamental;

    @Column(name = "puntuacion_final", precision = 5, scale = 2)
    private BigDecimal puntuacionFinal;

    @Column(name = "observaciones", length = Integer.MAX_VALUE)
    private String observaciones;

    @ColumnDefault("CURRENT_DATE")
    @Column(name = "fecha_evaluacion", nullable = false)
    private LocalDate fechaEvaluacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluado_por", nullable = false)
    private Empleado evaluadoPor;

    @ColumnDefault("'PENDIENTE'")
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PeriodosEvaluacion getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(PeriodosEvaluacion idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public Departamento getIdDepartamento() {
        return idDepartamento;
    }

    public void setIdDepartamento(Departamento idDepartamento) {
        this.idDepartamento = idDepartamento;
    }

    public Empleado getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Empleado idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getPuntuacionIndividual() {
        return puntuacionIndividual;
    }

    public void setPuntuacionIndividual(BigDecimal puntuacionIndividual) {
        this.puntuacionIndividual = puntuacionIndividual;
    }

    public BigDecimal getPuntuacionDepartamental() {
        return puntuacionDepartamental;
    }

    public void setPuntuacionDepartamental(BigDecimal puntuacionDepartamental) {
        this.puntuacionDepartamental = puntuacionDepartamental;
    }

    public BigDecimal getPuntuacionFinal() {
        return puntuacionFinal;
    }

    public void setPuntuacionFinal(BigDecimal puntuacionFinal) {
        this.puntuacionFinal = puntuacionFinal;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDate getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(LocalDate fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }

    public Empleado getEvaluadoPor() {
        return evaluadoPor;
    }

    public void setEvaluadoPor(Empleado evaluadoPor) {
        this.evaluadoPor = evaluadoPor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}