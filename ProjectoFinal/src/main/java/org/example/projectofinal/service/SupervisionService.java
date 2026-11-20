package org.example.projectofinal.service;

import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.Supervision;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class SupervisionService extends ServiceGeneric<Supervision> {

    private static final String SELECT_BASE =
            "SELECT s FROM Supervision s " +
                    "JOIN FETCH s.idEmpleado " +
                    "JOIN FETCH s.idJefe ";

    public SupervisionService() {
        super(new ImplDao<>(Supervision.class));
    }

    public List<Supervision> listarConDetalle() {
        return consultar(SELECT_BASE + "ORDER BY s.fechaInicio DESC", Supervision.class);
    }

    public List<Supervision> listarActivas() {
        return consultar(SELECT_BASE + "WHERE s.activo = true ORDER BY s.fechaInicio DESC",
                Supervision.class);
    }

    /** Empleados que tiene a cargo un jefe (supervisiones activas). */
    public List<Supervision> listarPorJefe(UUID idJefe) {
        return consultar(SELECT_BASE + "WHERE s.idJefe.id = :id AND s.activo = true",
                Supervision.class, "id", idJefe);
    }

    /** Supervisión activa de un empleado (quién es su jefe). */
    public Supervision buscarActivaPorEmpleado(UUID idEmpleado) {
        return consultarUno(SELECT_BASE + "WHERE s.idEmpleado.id = :id AND s.activo = true",
                Supervision.class, "id", idEmpleado);
    }

    public boolean registrar(Supervision supervision) {

        if (supervision == null
                || supervision.getIdEmpleado() == null
                || supervision.getIdJefe() == null
                || supervision.getFechaInicio() == null) {
            return false;
        }

        // Un empleado no puede supervisarse a sí mismo
        if (supervision.getIdEmpleado().getId().equals(supervision.getIdJefe().getId())) {
            return false;
        }

        try {
            // En la base cada empleado tiene una sola fila de supervisión
            // (UNIQUE id_empleado): si ya existe se reutiliza con el nuevo jefe.
            Supervision existente = consultarUno(
                    "SELECT s FROM Supervision s WHERE s.idEmpleado.id = :id",
                    Supervision.class, "id", supervision.getIdEmpleado().getId());

            if (existente != null) {
                existente.setIdJefe(supervision.getIdJefe());
                existente.setFechaInicio(supervision.getFechaInicio());
                existente.setFechaFin(null);
                existente.setActivo(true);
                actualizar(existente);
            } else {
                supervision.setActivo(true);
                guardar(supervision);
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean finalizar(UUID idSupervision) {

        Supervision supervision = buscarPorId(idSupervision);

        if (supervision == null) {
            return false;
        }

        try {
            supervision.setActivo(false);
            supervision.setFechaFin(LocalDate.now());
            actualizar(supervision);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
