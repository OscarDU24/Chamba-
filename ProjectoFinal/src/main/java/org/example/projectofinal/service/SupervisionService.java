package org.example.projectofinal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.projectofinal.dao.EntityManagerAdmin;
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

        // Se conserva el historial: la supervisión anterior se cierra
        // (activo = false, fecha_fin = inicio de la nueva) y se crea una nueva.
        // Ambos pasos van en la misma transacción.
        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Supervision anterior = em.createQuery(
                            "SELECT s FROM Supervision s " +
                                    "WHERE s.idEmpleado.id = :id AND s.activo = true",
                            Supervision.class)
                    .setParameter("id", supervision.getIdEmpleado().getId())
                    .getResultStream().findFirst().orElse(null);

            if (anterior != null) {
                if (supervision.getFechaInicio().isBefore(anterior.getFechaInicio())) {
                    tx.rollback();
                    return false;
                }
                anterior.setActivo(false);
                anterior.setFechaFin(supervision.getFechaInicio());
                em.flush();
            }

            supervision.setActivo(true);
            supervision.setFechaFin(null);
            em.persist(supervision);

            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    /** Todas las supervisiones (actuales y pasadas) de un empleado. */
    public List<Supervision> listarHistorialPorEmpleado(UUID idEmpleado) {
        return consultar(SELECT_BASE + "WHERE s.idEmpleado.id = :id ORDER BY s.fechaInicio DESC",
                Supervision.class, "id", idEmpleado);
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
