package org.example.projectofinal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.projectofinal.dao.EntityManagerAdmin;
import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.model.ObjetivoDepartamento;
import org.example.projectofinal.model.ResponsableObjetivo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ResponsableObjetivoService extends ServiceGeneric<ResponsableObjetivo> {

    private static final String SELECT_BASE =
            "SELECT r FROM ResponsableObjetivo r " +
                    "JOIN FETCH r.idEmpleado " +
                    "JOIN FETCH r.idAsignacion a " +
                    "JOIN FETCH a.idObjetivo ";

    public ResponsableObjetivoService() {
        super(new ImplDao<>(ResponsableObjetivo.class));
    }

    public List<ResponsableObjetivo> listarPorAsignacion(UUID idAsignacion) {
        return consultar(SELECT_BASE + "WHERE a.id = :id",
                ResponsableObjetivo.class, "id", idAsignacion);
    }

    public List<ResponsableObjetivo> listarPorEmpleado(UUID idEmpleado) {
        return consultar(SELECT_BASE + "WHERE r.idEmpleado.id = :id",
                ResponsableObjetivo.class, "id", idEmpleado);
    }

    public boolean asignar(ResponsableObjetivo responsable) {

        if (responsable == null
                || responsable.getIdAsignacion() == null
                || responsable.getIdEmpleado() == null) {
            return false;
        }

        boolean yaAsignado = consultarUno(
                "SELECT r FROM ResponsableObjetivo r " +
                        "WHERE r.idAsignacion.id = :asig AND r.idEmpleado.id = :emp",
                ResponsableObjetivo.class,
                "asig", responsable.getIdAsignacion().getId(),
                "emp", responsable.getIdEmpleado().getId()) != null;

        if (yaAsignado) {
            return false;
        }

        try {
            if (responsable.getFechaAsignacion() == null) {
                responsable.setFechaAsignacion(LocalDate.now());
            }
            guardar(responsable);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Agrega como responsables a todos los empleados activos del departamento
     * de la asignación que todavía no lo sean. Devuelve cuántos se agregaron,
     * o -1 si hubo un error.
     */
    public int sincronizarConDepartamento(UUID idAsignacion) {

        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            ObjetivoDepartamento asignacion = em.find(ObjetivoDepartamento.class, idAsignacion);
            if (asignacion == null) {
                tx.rollback();
                return -1;
            }

            List<Empleado> faltantes = em.createQuery(
                            "SELECT e FROM Empleado e " +
                                    "WHERE e.idDepartamento.id = :dep AND e.activo = true " +
                                    "AND NOT EXISTS (SELECT r FROM ResponsableObjetivo r " +
                                    "  WHERE r.idAsignacion.id = :asig AND r.idEmpleado.id = e.id)",
                            Empleado.class)
                    .setParameter("dep", asignacion.getIdDepartamento().getId())
                    .setParameter("asig", idAsignacion)
                    .getResultList();

            for (Empleado empleado : faltantes) {
                ResponsableObjetivo r = new ResponsableObjetivo();
                r.setIdAsignacion(asignacion);
                r.setIdEmpleado(empleado);
                r.setFechaAsignacion(LocalDate.now());
                em.persist(r);
            }

            tx.commit();
            return faltantes.size();

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return -1;

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    /** Cantidad de responsables por asignación (id de asignación -> total). */
    public Map<UUID, Long> contarPorAsignacion() {
        Map<UUID, Long> totales = new HashMap<>();
        for (Object[] fila : consultar(
                "SELECT r.idAsignacion.id, COUNT(r) FROM ResponsableObjetivo r GROUP BY r.idAsignacion.id",
                Object[].class)) {
            totales.put((UUID) fila[0], (Long) fila[1]);
        }
        return totales;
    }

    public boolean quitar(UUID idResponsable) {
        try {
            eliminar(idResponsable);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
