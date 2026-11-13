package org.example.projectofinal.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "objetivos_departamento")
public class ObjetivosDepartamento {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_asignacion", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_objetivo", nullable = false)
    private Objetivo idObjetivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departamento", nullable = false)
    private Departamento idDepartamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_periodo", nullable = false)
    private PeriodosEvaluacion idPeriodo;

    @ColumnDefault("CURRENT_DATE")
    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDate fechaAsignacion;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Objetivo getIdObjetivo() {
        return idObjetivo;
    }

    public void setIdObjetivo(Objetivo idObjetivo) {
        this.idObjetivo = idObjetivo;
    }

    public Departamento getIdDepartamento() {
        return idDepartamento;
    }

    public void setIdDepartamento(Departamento idDepartamento) {
        this.idDepartamento = idDepartamento;
    }

    public PeriodosEvaluacion getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(PeriodosEvaluacion idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDate fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

}