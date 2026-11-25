package org.example.projectofinal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CategoriaDepartamentoId implements Serializable {
    @Serial
    private static final long serialVersionUID = 5638264097793169194L;
    @Column(name = "id_categoria", nullable = false)
    private UUID idCategoria;

    @Column(name = "id_departamento", nullable = false)
    private UUID idDepartamento;

    public UUID getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(UUID idCategoria) {
        this.idCategoria = idCategoria;
    }

    public UUID getIdDepartamento() {
        return idDepartamento;
    }

    public void setIdDepartamento(UUID idDepartamento) {
        this.idDepartamento = idDepartamento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoriaDepartamentoId entity = (CategoriaDepartamentoId) o;
        return Objects.equals(this.idCategoria, entity.idCategoria) &&
                Objects.equals(this.idDepartamento, entity.idDepartamento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCategoria, idDepartamento);
    }
}