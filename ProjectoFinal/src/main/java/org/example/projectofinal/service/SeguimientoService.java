package org.example.projectofinal.service;

import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.Seguimiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class SeguimientoService extends ServiceGeneric<Seguimiento> {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private static final String SELECT_BASE =
            "SELECT s FROM Seguimiento s " +
                    "JOIN FETCH s.idAsignacion a " +
                    "JOIN FETCH a.idObjetivo " +
                    "JOIN FETCH a.idDepartamento " +
                    "JOIN FETCH a.idPeriodo " +
                    "JOIN FETCH s.registradoPor " +
                    "LEFT JOIN FETCH s.idEmpleado " +
                    "LEFT JOIN FETCH s.idDepartamento ";

    public SeguimientoService() {
        super(new ImplDao<>(Seguimiento.class));
    }

    public List<Seguimiento> listarConDetalle() {
        return consultar(SELECT_BASE + "ORDER BY s.fechaSeguimiento DESC", Seguimiento.class);
    }

    public List<Seguimiento> listarPorAsignacion(UUID idAsignacion) {
        return consultar(SELECT_BASE + "WHERE a.id = :id ORDER BY s.fechaSeguimiento DESC",
                Seguimiento.class, "id", idAsignacion);
    }

    public List<Seguimiento> listarPorEmpleado(UUID idEmpleado) {
        return consultar(SELECT_BASE + "WHERE s.idEmpleado.id = :id ORDER BY s.fechaSeguimiento DESC",
                Seguimiento.class, "id", idEmpleado);
    }

    /** Último avance registrado para una asignación (null si no hay). */
    public Seguimiento buscarUltimo(UUID idAsignacion) {
        return consultarUno(SELECT_BASE + "WHERE a.id = :id ORDER BY s.fechaSeguimiento DESC",
                Seguimiento.class, "id", idAsignacion);
    }

    public static final String INDIVIDUAL = "INDIVIDUAL";
    public static final String DEPARTAMENTAL = "DEPARTAMENTAL";

    /**
     * Reglas de la base: INDIVIDUAL lleva empleado y no departamento;
     * DEPARTAMENTAL lleva departamento y no empleado; avance entre 0 y 100.
     * Devuelve el mensaje de error o null si es válido.
     */
    public String validar(Seguimiento seguimiento) {

        if (seguimiento == null || seguimiento.getIdAsignacion() == null) {
            return "Selecciona la asignación (objetivo · departamento · período).";
        }
        if (seguimiento.getRegistradoPor() == null) {
            return "No hay un usuario con sesión iniciada para registrar el seguimiento.";
        }
        if (INDIVIDUAL.equals(seguimiento.getTipo())) {
            if (seguimiento.getIdEmpleado() == null || seguimiento.getIdDepartamento() != null) {
                return "Un seguimiento INDIVIDUAL necesita un empleado.";
            }
        } else if (DEPARTAMENTAL.equals(seguimiento.getTipo())) {
            if (seguimiento.getIdDepartamento() == null || seguimiento.getIdEmpleado() != null) {
                return "Un seguimiento DEPARTAMENTAL necesita un departamento.";
            }
        } else {
            return "Selecciona el tipo de seguimiento.";
        }

        BigDecimal avance = seguimiento.getPorcentajeAvance();
        if (avance == null || avance.compareTo(BigDecimal.ZERO) < 0 || avance.compareTo(CIEN) > 0) {
            return "El porcentaje de avance debe estar entre 0 y 100.";
        }
        return null;
    }

    public boolean registrar(Seguimiento seguimiento) {

        if (validar(seguimiento) != null) {
            return false;
        }

        try {
            if (seguimiento.getFechaSeguimiento() == null) {
                seguimiento.setFechaSeguimiento(LocalDate.now());
            }
            guardar(seguimiento);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
