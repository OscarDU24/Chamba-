package org.example.projectofinal.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "seguimientos")
public class Seguimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_seguimiento", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asignacion", nullable = false)
    private ObjetivosDepartamento idAsignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado")
    private Empleado idEmpleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento")
    private Departamento idDepartamento;

    @ColumnDefault("CURRENT_DATE")
    @Column(name = "fecha_seguimiento", nullable = false)
    private LocalDate fechaSeguimiento;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "porcentaje_avance", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeAvance;

    @Column(name = "observaciones", length = Integer.MAX_VALUE)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por", nullable = false)
    private Empleado registradoPor;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ObjetivosDepartamento getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(ObjetivosDepartamento idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public Empleado getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Empleado idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Departamento getIdDepartamento() {
        return idDepartamento;
    }

    public void setIdDepartamento(Departamento idDepartamento) {
        this.idDepartamento = idDepartamento;
    }

    public LocalDate getFechaSeguimiento() {
        return fechaSeguimiento;
    }

    public void setFechaSeguimiento(LocalDate fechaSeguimiento) {
        this.fechaSeguimiento = fechaSeguimiento;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getPorcentajeAvance() {
        return porcentajeAvance;
    }

    public void setPorcentajeAvance(BigDecimal porcentajeAvance) {
        this.porcentajeAvance = porcentajeAvance;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Empleado getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Empleado registradoPor) {
        this.registradoPor = registradoPor;
    }

}