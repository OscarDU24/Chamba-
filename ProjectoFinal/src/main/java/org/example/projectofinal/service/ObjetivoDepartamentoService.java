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

    public boolean asignar(ObjetivoDepartamento asignacion) {

        if (asignacion == null
                || asignacion.getIdObjetivo() == null
                || asignacion.getIdDepartamento() == null
                || asignacion.getIdPeriodo() == null) {
            return false;
        }

        if (existeAsignacion(asignacion)) {
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
