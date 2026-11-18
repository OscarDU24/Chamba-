package org.example.projectofinal.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "administradores_sistema")
public class AdministradoresSistema {
    @Id
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id_administrador", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private CuentaUsuario idUsuario;

    @ColumnDefault("true")
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CuentaUsuario getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(CuentaUsuario idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

}