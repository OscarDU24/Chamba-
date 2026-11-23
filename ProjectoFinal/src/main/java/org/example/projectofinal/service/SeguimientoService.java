package org.example.projectofinal.service;

import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.ObjetivoDepartamento;
import org.example.projectofinal.model.PeriodoEvaluacion;
import org.example.projectofinal.model.Seguimiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    public static final String INDIVIDUAL = "INDIVIDUAL";
    public static final String DEPARTAMENTAL = "DEPARTAMENTAL";

    /**
     * Reglas del seguimiento:
     *  - INDIVIDUAL lleva empleado (que participe en el objetivo) y no departamento;
     *  - DEPARTAMENTAL lleva el departamento de la asignación y no empleado;
     *  - avance entre 0 y 100;
     *  - fecha dentro del período de la asignación.
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

        if (seguimiento.getFechaSeguimiento() == null) {
            seguimiento.setFechaSeguimiento(LocalDate.now());
        }

        // La asignación se lee de la base con su período y departamento
        ObjetivoDepartamento asignacion = consultarUno(
                "SELECT a FROM ObjetivoDepartamento a " +
                        "JOIN FETCH a.idPeriodo JOIN FETCH a.idDepartamento WHERE a.id = :id",
                ObjetivoDepartamento.class, "id", seguimiento.getIdAsignacion().getId());

        if (asignacion == null) {
            return "La asignación seleccionada ya no existe.";
        }

        // La fecha del avance tiene que caer dentro del período de la asignación
        PeriodoEvaluacion periodo = asignacion.getIdPeriodo();
        LocalDate fecha = seguimiento.getFechaSeguimiento();
        if (fecha.isBefore(periodo.getFechaInicio()) || fecha.isAfter(periodo.getFechaFin())) {
            return "La fecha del seguimiento debe estar dentro del período " + periodo.getNombre()
                    + " (del " + periodo.getFechaInicio().format(FECHA)
                    + " al " + periodo.getFechaFin().format(FECHA) + ").";
        }

        if (DEPARTAMENTAL.equals(seguimiento.getTipo())
                && !seguimiento.getIdDepartamento().getId().equals(asignacion.getIdDepartamento().getId())) {
            return "El seguimiento departamental debe ser del departamento "
                    + asignacion.getIdDepartamento().getNombre() + ", al que está asignado el objetivo.";
        }

        if (INDIVIDUAL.equals(seguimiento.getTipo())) {
            Long participa = consultarUno(
                    "SELECT COUNT(r) FROM ResponsableObjetivo r " +
                            "WHERE r.idAsignacion.id = :asig AND r.idEmpleado.id = :emp",
                    Long.class, "asig", asignacion.getId(), "emp", seguimiento.getIdEmpleado().getId());
            if (participa == null || participa == 0) {
                return "Ese empleado no participa en este objetivo. Si entró después al departamento, "
                        + "usa \"Actualizar participantes\" en la pantalla Objetivos.";
            }
        }
        return null;
    }

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public boolean registrar(Seguimiento seguimiento) {

        if (validar(seguimiento) != null) {
            return false;
        }

        try {
            guardar(seguimiento);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
