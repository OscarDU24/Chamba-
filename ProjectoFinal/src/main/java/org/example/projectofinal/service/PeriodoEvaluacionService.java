package org.example.projectofinal.service;

import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.PeriodoEvaluacion;

import java.time.LocalDate;
import java.util.List;

public class PeriodoEvaluacionService extends ServiceGeneric<PeriodoEvaluacion> {

    public PeriodoEvaluacionService() {
        super(new ImplDao<>(PeriodoEvaluacion.class));
    }

    public List<PeriodoEvaluacion> listarOrdenados() {
        return consultar(
                "SELECT p FROM PeriodoEvaluacion p ORDER BY p.fechaInicio DESC",
                PeriodoEvaluacion.class);
    }

    public List<PeriodoEvaluacion> listarPorEstado(String estado) {
        return consultar(
                "SELECT p FROM PeriodoEvaluacion p WHERE p.estado = :estado " +
                        "ORDER BY p.fechaInicio DESC",
                PeriodoEvaluacion.class,
                "estado", estado);
    }

    /** Período cuyo rango de fechas incluye el día de hoy. */
    public PeriodoEvaluacion buscarPeriodoActual() {
        LocalDate hoy = LocalDate.now();
        return consultarUno(
                "SELECT p FROM PeriodoEvaluacion p " +
                        "WHERE p.fechaInicio <= :hoy AND p.fechaFin >= :hoy " +
                        "ORDER BY p.fechaInicio DESC",
                PeriodoEvaluacion.class,
                "hoy", hoy);
    }

    public boolean registrar(PeriodoEvaluacion periodo) {

        if (!esValido(periodo)) {
            return false;
        }

        try {
            guardar(periodo);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean modificar(PeriodoEvaluacion periodo) {

        if (periodo == null || periodo.getId() == null || !esValido(periodo)) {
            return false;
        }

        try {
            actualizar(periodo);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean esValido(PeriodoEvaluacion periodo) {
        return periodo != null
                && periodo.getNombre() != null && !periodo.getNombre().isBlank()
                && periodo.getFechaInicio() != null
                && periodo.getFechaFin() != null
                && !periodo.getFechaFin().isBefore(periodo.getFechaInicio());
    }
}
