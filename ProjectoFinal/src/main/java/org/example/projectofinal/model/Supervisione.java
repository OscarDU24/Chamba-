package org.example.projectofinal.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "supervisiones")
public class Supervisione {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_supervision", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado idEmpleado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_jefe", nullable = false)
    private Empleado idJefe;

    @ColumnDefault("CURRENT_DATE")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @ColumnDefault("true")
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Empleado getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Empleado idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Empleado getIdJefe() {
        return idJefe;
    }

    public void setIdJefe(Empleado idJefe) {
        this.idJefe = idJefe;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

}