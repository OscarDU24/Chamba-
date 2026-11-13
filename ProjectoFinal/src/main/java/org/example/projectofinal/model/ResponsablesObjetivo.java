package org.example.projectofinal.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "responsables_objetivos")
public class ResponsablesObjetivo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_responsabilidad", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asignacion", nullable = false)
    private ObjetivosDepartamento idAsignacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado idEmpleado;

    @ColumnDefault("CURRENT_DATE")
    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDate fechaAsignacion;

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

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDate fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

}