package org.example.projectofinal.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.projectofinal.model.CategoriaPersonal;
import org.example.projectofinal.util.JpaUtil;

import java.util.List;
import java.util.UUID;

public class CategoriaPersonalDao {

    public boolean guardar(CategoriaPersonal categoria) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(categoria);
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

    public List<CategoriaPersonal> listar() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM CategoriaPersonal c",
                    CategoriaPersonal.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public CategoriaPersonal buscarPorId(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(CategoriaPersonal.class, id);
        } finally {
            em.close();
        }
    }

    public boolean actualizar(CategoriaPersonal categoria) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.merge(categoria);
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
            CategoriaPersonal categoria =
                    em.find(CategoriaPersonal.class, id);

            if (categoria == null) {
                tx.rollback();
                return false;
            }

            categoria.setActivo(false);
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
