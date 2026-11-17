package org.example.projectofinal.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.projectofinal.model.Evaluacion;
import org.example.projectofinal.util.JpaUtil;

import java.util.List;
import java.util.UUID;

public class EvaluacionDao {

    public boolean guardar(Evaluacion evaluacion) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(evaluacion);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public List<Evaluacion> listar() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Evaluacion e ORDER BY e.fechaEvaluacion DESC",
                    Evaluacion.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public Evaluacion buscarPorId(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Evaluacion.class, id);
        } finally {
            em.close();
        }
    }

    public List<Evaluacion> buscarPorEmpleado(UUID idEmpleado) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Evaluacion e WHERE e.empleado.idEmpleado = :id",
                    Evaluacion.class
            ).setParameter("id", idEmpleado).getResultList();
        } finally {
            em.close();
        }
    }

    public boolean actualizar(Evaluacion evaluacion) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(evaluacion);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public boolean eliminar(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Evaluacion evaluacion = em.find(Evaluacion.class, id);

            if (evaluacion == null) {
                tx.rollback();
                return false;
            }

            em.remove(evaluacion);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}
