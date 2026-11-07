package org.example.projectofinal.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "citas")
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_cita", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private Solicitude idSolicitud;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private Instant fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)
    private Instant fechaHoraFin;

    @Column(name = "modalidad", nullable = false, length = 20)
    private String modalidad;

    @Column(name = "ubicacion", length = 150)
    private String ubicacion;

    @Column(name = "observaciones", length = Integer.MAX_VALUE)
    private String observaciones;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Solicitude getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Solicitude idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Instant getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(Instant fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public Instant getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(Instant fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

}