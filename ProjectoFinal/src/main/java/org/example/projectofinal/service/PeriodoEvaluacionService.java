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

        if (validar(periodo) != null) {
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

        if (periodo == null || periodo.getId() == null || validar(periodo) != null) {
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

    public static final java.util.List<String> ESTADOS = java.util.List.of("PLANIFICADO", "EN_CURSO", "CERRADO");

    /**
     * Reglas del formulario de períodos.
     * Devuelve el mensaje de error para mostrar, o null si es válido.
     */
    public String validar(PeriodoEvaluacion p) {
        if (p == null || p.getNombre() == null || p.getNombre().isBlank()) {
            return "Escribe el nombre del período.";
        }
        p.setNombre(p.getNombre().trim());
        if (p.getNombre().length() > 100) {
            return "El nombre no puede tener más de 100 caracteres.";
        }
        if (p.getFechaInicio() == null || p.getFechaFin() == null) {
            return "Selecciona la fecha de inicio y la fecha de fin.";
        }
        if (p.getFechaFin().isBefore(p.getFechaInicio())) {
            return "La fecha de fin no puede ser anterior a la fecha de inicio.";
        }
        if (!ESTADOS.contains(p.getEstado())) {
            return "Selecciona el estado del período.";
        }
        return null;
    }
}
