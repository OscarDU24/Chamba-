package org.example.projectofinal.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "objetivos")
public class Objetivo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_objetivo", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_objetivo_padre")
    private Objetivo idObjetivoPadre;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", length = Integer.MAX_VALUE)
    private String descripcion;

    @Column(name = "nivel", nullable = false, length = 20)
    private String nivel;

    @Column(name = "indicador", length = 150)
    private String indicador;

    @Column(name = "meta", precision = 12, scale = 2)
    private BigDecimal meta;

    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida;

    @ColumnDefault("true")
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Objetivo getIdObjetivoPadre() {
        return idObjetivoPadre;
    }

    public void setIdObjetivoPadre(Objetivo idObjetivoPadre) {
        this.idObjetivoPadre = idObjetivoPadre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getIndicador() {
        return indicador;
    }

    public void setIndicador(String indicador) {
        this.indicador = indicador;
    }

    public BigDecimal getMeta() {
        return meta;
    }

    public void setMeta(BigDecimal meta) {
        this.meta = meta;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

}