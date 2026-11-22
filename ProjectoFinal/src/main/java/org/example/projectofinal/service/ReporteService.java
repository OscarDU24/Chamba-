package org.example.projectofinal.service;

import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.model.Evaluacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Datos para la pantalla de reportes. Solo lectura. */
public class ReporteService {

    /** Una fila del reporte por departamento. */
    public record FilaDepartamento(String departamento, long empleados, long objetivos,
                                   BigDecimal avance, BigDecimal evaluacionFinal, String estadoEvaluacion) {
    }

    /** Una fila del reporte por empleado. */
    public record FilaEmpleado(String empleado, String departamento, long objetivos,
                               BigDecimal avance, BigDecimal evaluacionFinal, String estadoEvaluacion) {
    }

    private final EvaluacionService evaluacionService = new EvaluacionService();
    private final DepartamentoService departamentoService = new DepartamentoService();
    private final AdminPanelService empleadoService = new AdminPanelService();
    private final ObjetivoDepartamentoService asignacionService = new ObjetivoDepartamentoService();
    private final ResponsableObjetivoService responsableService = new ResponsableObjetivoService();

    public List<FilaDepartamento> porDepartamento(UUID idPeriodo) {

        List<Empleado> empleados = empleadoService.listarEmpleados();
        Map<UUID, Evaluacion> evaluaciones = new HashMap<>();
        for (Evaluacion e : evaluacionService.listarPorPeriodo(idPeriodo)) {
            if (EvaluacionService.DEPARTAMENTAL.equals(e.getTipo())) {
                evaluaciones.put(e.getIdDepartamento().getId(), e);
            }
        }

        Map<UUID, Long> objetivosPorDep = new HashMap<>();
        asignacionService.listarPorPeriodo(idPeriodo)
                .forEach(a -> objetivosPorDep.merge(a.getIdDepartamento().getId(), 1L, Long::sum));

        List<FilaDepartamento> filas = new ArrayList<>();
        for (Departamento d : departamentoService.listarActivos()) {
            long totalEmpleados = empleados.stream()
                    .filter(e -> Boolean.TRUE.equals(e.getActivo()))
                    .filter(e -> e.getIdDepartamento() != null && e.getIdDepartamento().getId().equals(d.getId()))
                    .count();
            Evaluacion ev = evaluaciones.get(d.getId());

            filas.add(new FilaDepartamento(
                    d.getNombre(),
                    totalEmpleados,
                    objetivosPorDep.getOrDefault(d.getId(), 0L),
                    evaluacionService.calcularDepartamental(idPeriodo, d.getId()),
                    ev == null ? null : ev.getPuntuacionFinal(),
                    ev == null ? "Sin evaluar" : ev.getEstado()));
        }
        return filas;
    }

    public List<FilaEmpleado> porEmpleado(UUID idPeriodo) {

        Map<UUID, Evaluacion> evaluaciones = new HashMap<>();
        for (Evaluacion e : evaluacionService.listarPorPeriodo(idPeriodo)) {
            if (EvaluacionService.INDIVIDUAL.equals(e.getTipo()) && e.getIdEmpleado() != null) {
                evaluaciones.put(e.getIdEmpleado().getId(), e);
            }
        }

        Map<UUID, Long> objetivosPorEmpleado = new HashMap<>();
        asignacionService.listarPorPeriodo(idPeriodo).forEach(a ->
                responsableService.listarPorAsignacion(a.getId()).forEach(r ->
                        objetivosPorEmpleado.merge(r.getIdEmpleado().getId(), 1L, Long::sum)));

        List<FilaEmpleado> filas = new ArrayList<>();
        for (Empleado e : empleadoService.listarEmpleados()) {
            if (!Boolean.TRUE.equals(e.getActivo())) {
                continue;
            }
            Evaluacion ev = evaluaciones.get(e.getId());

            filas.add(new FilaEmpleado(
                    e.getNombreCompleto(),
                    e.getIdDepartamento() == null ? "Sin departamento" : e.getIdDepartamento().getNombre(),
                    objetivosPorEmpleado.getOrDefault(e.getId(), 0L),
                    evaluacionService.calcularIndividual(idPeriodo, e.getId()),
                    ev == null ? null : ev.getPuntuacionFinal(),
                    ev == null ? "Sin evaluar" : ev.getEstado()));
        }
        return filas;
    }

    /** Promedio de los valores no nulos, o null si no hay ninguno. */
    public static BigDecimal promedio(List<BigDecimal> valores) {
        List<BigDecimal> presentes = valores.stream().filter(Objects::nonNull).toList();
        if (presentes.isEmpty()) {
            return null;
        }
        return presentes.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(presentes.size()), 2, RoundingMode.HALF_UP);
    }
}
