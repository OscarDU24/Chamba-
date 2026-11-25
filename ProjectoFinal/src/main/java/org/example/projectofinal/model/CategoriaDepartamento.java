package org.example.projectofinal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria_departamento", schema = "public")
public class CategoriaDepartamento {
    @EmbeddedId
    private CategoriaDepartamentoId id;

    @MapsId("idCategoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriaPersonal idCategoria;

    @MapsId("idDepartamento")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departamento", nullable = false)
    private Departamento idDepartamento;

    public CategoriaDepartamentoId getId() {
        return id;
    }

    public void setId(CategoriaDepartamentoId id) {
        this.id = id;
    }

    public CategoriaPersonal getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(CategoriaPersonal idCategoria) {
        this.idCategoria = idCategoria;
    }

    public Departamento getIdDepartamento() {
        return idDepartamento;
    }

    public void setIdDepartamento(Departamento idDepartamento) {
        this.idDepartamento = idDepartamento;
    }

}