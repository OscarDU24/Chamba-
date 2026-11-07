package org.example.projectofinal.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_rol", nullable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 30)
    private String nombre;

    @Column(name = "descripcion", length = 150)
    private String descripcion;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

}