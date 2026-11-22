package org.example.projectofinal.service;

import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.Evaluacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EvaluacionService extends ServiceGeneric<Evaluacion> {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private static final String SELECT_BASE =
            "SELECT e FROM Evaluacion e " +
                    "JOIN FETCH e.idPeriodo " +
                    "JOIN FETCH e.idDepartamento " +
                    "JOIN FETCH e.evaluadoPor " +
                    "LEFT JOIN FETCH e.idEmpleado ";

    public EvaluacionService() {
        super(new ImplDao<>(Evaluacion.class));
    }

    public List<Evaluacion> listarConDetalle() {
        return consultar(SELECT_BASE + "ORDER BY e.fechaEvaluacion DESC", Evaluacion.class);
    }

    public List<Evaluacion> listarPorPeriodo(UUID idPeriodo) {
        return consultar(SELECT_BASE + "WHERE e.idPeriodo.id = :id ORDER BY e.fechaEvaluacion DESC",
                Evaluacion.class, "id", idPeriodo);
    }

    public List<Evaluacion> listarPorEmpleado(UUID idEmpleado) {
        return consultar(SELECT_BASE + "WHERE e.idEmpleado.id = :id ORDER BY e.fechaEvaluacion DESC",
                Evaluacion.class, "id", idEmpleado);
    }

    public List<Evaluacion> listarPorDepartamento(UUID idDepartamento) {
        return consultar(SELECT_BASE + "WHERE e.idDepartamento.id = :id ORDER BY e.fechaEvaluacion DESC",
                Evaluacion.class, "id", idDepartamento);
    }

    public boolean registrar(Evaluacion evaluacion) {

        if (!esValida(evaluacion)) {
            return false;
        }

        try {
            if (evaluacion.getFechaEvaluacion() == null) {
                evaluacion.setFechaEvaluacion(LocalDate.now());
            }
            guardar(evaluacion);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean modificar(Evaluacion evaluacion) {

        if (evaluacion == null || evaluacion.getId() == null || !esValida(evaluacion)) {
            return false;
        }

        try {
            actualizar(evaluacion);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Calcula la puntuación final como promedio ponderado.
     * pesoIndividual va de 0 a 100; el resto corresponde a la parte departamental.
     */
    public BigDecimal calcularPuntuacionFinal(BigDecimal individual,
                                              BigDecimal departamental,
                                              BigDecimal pesoIndividual) {

        if (individual == null && departamental == null) {
            return null;
        }
        if (individual == null) {
            return departamental;
        }
        if (departamental == null) {
            return individual;
        }

        BigDecimal peso = pesoIndividual.divide(CIEN, 4, RoundingMode.HALF_UP);

        return individual.multiply(peso)
                .add(departamental.multiply(BigDecimal.ONE.subtract(peso)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // =========================================================
    // CÁLCULO AUTOMÁTICO A PARTIR DE LOS SEGUIMIENTOS
    // =========================================================

    public static final String INDIVIDUAL = "INDIVIDUAL";
    public static final String DEPARTAMENTAL = "DEPARTAMENTAL";

    /** Peso por defecto de la parte individual (%). El documento lo deja pendiente. */
    public static final BigDecimal PESO_INDIVIDUAL_POR_DEFECTO = new BigDecimal("50");

    /**
     * Puntuación individual: promedio del último avance INDIVIDUAL registrado
     * en cada objetivo del período en el que el empleado es responsable.
     * Un objetivo sin avance cuenta como 0. Devuelve null si no participa en ninguno.
     */
    public BigDecimal calcularIndividual(UUID idPeriodo, UUID idEmpleado) {

        List<UUID> asignaciones = consultar(
                "SELECT r.idAsignacion.id FROM ResponsableObjetivo r " +
                        "WHERE r.idEmpleado.id = :emp AND r.idAsignacion.idPeriodo.id = :per",
                UUID.class, "emp", idEmpleado, "per", idPeriodo);

        List<Object[]> avances = consultar(
                "SELECT s.idAsignacion.id, s.fechaSeguimiento, s.porcentajeAvance FROM Seguimiento s " +
                        "WHERE s.tipo = 'INDIVIDUAL' AND s.idEmpleado.id = :emp " +
                        "AND s.idAsignacion.idPeriodo.id = :per",
                Object[].class, "emp", idEmpleado, "per", idPeriodo);

        return promedioUltimosAvances(asignaciones, avances);
    }

    /**
     * Puntuación departamental: promedio del último avance DEPARTAMENTAL registrado
     * en cada objetivo asignado al departamento en el período.
     * Un objetivo sin avance cuenta como 0. Devuelve null si no tiene objetivos.
     */
    public BigDecimal calcularDepartamental(UUID idPeriodo, UUID idDepartamento) {

        List<UUID> asignaciones = consultar(
                "SELECT a.id FROM ObjetivoDepartamento a " +
                        "WHERE a.idDepartamento.id = :dep AND a.idPeriodo.id = :per",
                UUID.class, "dep", idDepartamento, "per", idPeriodo);

        List<Object[]> avances = consultar(
                "SELECT s.idAsignacion.id, s.fechaSeguimiento, s.porcentajeAvance FROM Seguimiento s " +
                        "WHERE s.tipo = 'DEPARTAMENTAL' AND s.idDepartamento.id = :dep " +
                        "AND s.idAsignacion.idPeriodo.id = :per",
                Object[].class, "dep", idDepartamento, "per", idPeriodo);

        return promedioUltimosAvances(asignaciones, avances);
    }

    /** avances: filas [idAsignacion, fecha, porcentaje]. */
    private BigDecimal promedioUltimosAvances(List<UUID> asignaciones, List<Object[]> avances) {

        if (asignaciones.isEmpty()) {
            return null;
        }

        Map<UUID, Object[]> ultimo = new HashMap<>();
        for (Object[] fila : avances) {
            Object[] actual = ultimo.get((UUID) fila[0]);
            if (actual == null || ((LocalDate) fila[1]).isAfter((LocalDate) actual[1])) {
                ultimo.put((UUID) fila[0], fila);
            }
        }

        BigDecimal suma = BigDecimal.ZERO;
        for (UUID id : asignaciones) {
            Object[] fila = ultimo.get(id);
            if (fila != null) {
                suma = suma.add((BigDecimal) fila[2]);
            }
        }

        return suma.divide(BigDecimal.valueOf(asignaciones.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Evaluación ya registrada para el mismo período y el mismo evaluado
     * (empleado si es INDIVIDUAL, departamento si es DEPARTAMENTAL).
     */
    public Evaluacion buscarExistente(UUID idPeriodo, String tipo, UUID idEmpleado, UUID idDepartamento) {
        if (INDIVIDUAL.equals(tipo)) {
            return consultarUno(SELECT_BASE +
                            "WHERE e.idPeriodo.id = :per AND e.tipo = 'INDIVIDUAL' AND e.idEmpleado.id = :emp",
                    Evaluacion.class, "per", idPeriodo, "emp", idEmpleado);
        }
        return consultarUno(SELECT_BASE +
                        "WHERE e.idPeriodo.id = :per AND e.tipo = 'DEPARTAMENTAL' AND e.idDepartamento.id = :dep",
                Evaluacion.class, "per", idPeriodo, "dep", idDepartamento);
    }

    /** Reglas de la base: INDIVIDUAL lleva empleado; DEPARTAMENTAL no. */
    public String validar(Evaluacion evaluacion) {
        if (evaluacion.getIdPeriodo() == null) {
            return "Selecciona el período.";
        }
        if (evaluacion.getIdDepartamento() == null) {
            return "Selecciona el departamento.";
        }
        if (evaluacion.getEvaluadoPor() == null) {
            return "No hay un usuario con sesión iniciada.";
        }
        if (INDIVIDUAL.equals(evaluacion.getTipo()) && evaluacion.getIdEmpleado() == null) {
            return "Una evaluación INDIVIDUAL necesita un empleado.";
        }
        if (DEPARTAMENTAL.equals(evaluacion.getTipo()) && evaluacion.getIdEmpleado() != null) {
            return "Una evaluación DEPARTAMENTAL no lleva empleado.";
        }
        if (!INDIVIDUAL.equals(evaluacion.getTipo()) && !DEPARTAMENTAL.equals(evaluacion.getTipo())) {
            return "Selecciona el tipo de evaluación.";
        }
        if (!enRango(evaluacion.getPuntuacionIndividual())
                || !enRango(evaluacion.getPuntuacionDepartamental())
                || !enRango(evaluacion.getPuntuacionFinal())) {
            return "Las puntuaciones deben estar entre 0 y 100.";
        }
        return null;
    }

    private boolean esValida(Evaluacion evaluacion) {
        return evaluacion != null
                && evaluacion.getIdPeriodo() != null
                && evaluacion.getIdDepartamento() != null
                && evaluacion.getEvaluadoPor() != null
                && enRango(evaluacion.getPuntuacionIndividual())
                && enRango(evaluacion.getPuntuacionDepartamental())
                && enRango(evaluacion.getPuntuacionFinal());
    }

    private boolean enRango(BigDecimal valor) {
        return valor == null
                || (valor.compareTo(BigDecimal.ZERO) >= 0 && valor.compareTo(CIEN) <= 0);
    }
}
