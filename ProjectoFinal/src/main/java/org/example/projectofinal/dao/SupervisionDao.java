package org.example.projectofinal.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.projectofinal.model.Supervision;
import org.example.projectofinal.util.JpaUtil;

import java.util.List;
import java.util.UUID;

public class SupervisionDao {

    public boolean guardar(Supervision supervision) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(supervision);
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

    public List<Supervision> listar() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT s FROM Supervision s",
                    Supervision.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Supervision> listarActivas() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT s FROM Supervision s WHERE s.activo = true",
                    Supervision.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public Supervision buscarPorId(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Supervision.class, id);
        } finally {
            em.close();
        }
    }

    public boolean actualizar(Supervision supervision) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(supervision);
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

    public boolean desactivar(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Supervision supervision =
                    em.find(Supervision.class, id);

            if (supervision == null) {
                tx.rollback();
                return false;
            }

            supervision.setActivo(false);
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
