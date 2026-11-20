package org.example.projectofinal.service;

import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.Evaluacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
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
