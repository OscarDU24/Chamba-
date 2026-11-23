package org.example.projectofinal.service;

import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.ObjetivoDepartamento;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ObjetivoDepartamentoService extends ServiceGeneric<ObjetivoDepartamento> {

    private static final String SELECT_BASE =
            "SELECT a FROM ObjetivoDepartamento a " +
                    "JOIN FETCH a.idObjetivo " +
                    "JOIN FETCH a.idDepartamento " +
                    "JOIN FETCH a.idPeriodo ";

    public ObjetivoDepartamentoService() {
        super(new ImplDao<>(ObjetivoDepartamento.class));
    }

    public List<ObjetivoDepartamento> listarConDetalle() {
        return consultar(SELECT_BASE + "ORDER BY a.fechaAsignacion DESC",
                ObjetivoDepartamento.class);
    }

    public List<ObjetivoDepartamento> listarPorDepartamento(UUID idDepartamento) {
        return consultar(SELECT_BASE + "WHERE a.idDepartamento.id = :id",
                ObjetivoDepartamento.class, "id", idDepartamento);
    }

    public List<ObjetivoDepartamento> listarPorPeriodo(UUID idPeriodo) {
        return consultar(SELECT_BASE + "WHERE a.idPeriodo.id = :id",
                ObjetivoDepartamento.class, "id", idPeriodo);
    }

    /**
     * Reglas para asignar un objetivo a un departamento.
     * Devuelve el mensaje de error para mostrar, o null si es válido.
     */
    public String validar(ObjetivoDepartamento a) {
        if (a == null || a.getIdObjetivo() == null) {
            return "Selecciona en la tabla el objetivo que quieres asignar.";
        }
        if (!Boolean.TRUE.equals(a.getIdObjetivo().getActivo())) {
            return "No se puede asignar un objetivo inactivo.";
        }
        if (!ObjetivoService.ESPECIFICO.equals(a.getIdObjetivo().getNivel())) {
            return "A los departamentos se asignan objetivos ESPECÍFICOS. Crea un objetivo específico dentro de \""
                    + a.getIdObjetivo().getNombre() + "\" y asigna ese.";
        }
        if (a.getIdDepartamento() == null || a.getIdPeriodo() == null) {
            return "Selecciona el departamento y el período.";
        }
        if (existeAsignacion(a)) {
            return "Ese objetivo ya está asignado a ese departamento en ese período.";
        }
        return null;
    }

    public boolean asignar(ObjetivoDepartamento asignacion) {

        if (validar(asignacion) != null) {
            return false;
        }

        try {
            if (asignacion.getFechaAsignacion() == null) {
                asignacion.setFechaAsignacion(LocalDate.now());
            }
            guardar(asignacion);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Evita asignar el mismo objetivo al mismo departamento en el mismo período. */
    public boolean existeAsignacion(ObjetivoDepartamento asignacion) {
        return consultarUno(
                "SELECT a FROM ObjetivoDepartamento a " +
                        "WHERE a.idObjetivo.id = :obj " +
                        "AND a.idDepartamento.id = :dep " +
                        "AND a.idPeriodo.id = :per",
                ObjetivoDepartamento.class,
                "obj", asignacion.getIdObjetivo().getId(),
                "dep", asignacion.getIdDepartamento().getId(),
                "per", asignacion.getIdPeriodo().getId()) != null;
    }
}
