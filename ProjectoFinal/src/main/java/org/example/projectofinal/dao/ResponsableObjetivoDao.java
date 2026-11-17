package org.example.projectofinal.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.projectofinal.model.ResponsableObjetivo;
import org.example.projectofinal.util.JpaUtil;

import java.util.List;
import java.util.UUID;

public class ResponsableObjetivoDao {

    public boolean guardar(ResponsableObjetivo responsable) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(responsable);
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

    public List<ResponsableObjetivo> listar() {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT r FROM ResponsableObjetivo r",
                    ResponsableObjetivo.class
            ).getResultList();

        } finally {
            em.close();
        }
    }
    public ResponsableObjetivo buscarPorId(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(ResponsableObjetivo.class, id);
        } finally {
            em.close();
        }
    }

    public boolean actualizar(ResponsableObjetivo responsable) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(responsable);
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
            ResponsableObjetivo responsable =
                    em.find(ResponsableObjetivo.class, id);

            if (responsable == null) {
                tx.rollback();
                return false;
            }

            em.remove(responsable);
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
