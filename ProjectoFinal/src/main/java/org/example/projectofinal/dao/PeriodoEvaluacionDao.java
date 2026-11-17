package org.example.projectofinal.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.projectofinal.model.PeriodoEvaluacion;
import org.example.projectofinal.util.JpaUtil;

import java.util.List;
import java.util.UUID;

public class PeriodoEvaluacionDao {

    public boolean guardar(PeriodoEvaluacion periodo) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(periodo);
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

    public List<PeriodoEvaluacion> listar() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM PeriodoEvaluacion p ORDER BY p.fechaInicio DESC",
                    PeriodoEvaluacion.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public PeriodoEvaluacion buscarPorId(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(PeriodoEvaluacion.class, id);
        } finally {
            em.close();
        }
    }

    public boolean actualizar(PeriodoEvaluacion periodo) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(periodo);
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
            PeriodoEvaluacion periodo =
                    em.find(PeriodoEvaluacion.class, id);

            if (periodo == null) {
                tx.rollback();
                return false;
            }

            em.remove(periodo);
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
