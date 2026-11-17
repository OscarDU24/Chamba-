package org.example.projectofinal.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.projectofinal.model.Objetivo;
import org.example.projectofinal.util.JpaUtil;

import java.util.List;
import java.util.UUID;

public class ObjetivoDao {

    public boolean guardar(Objetivo objetivo) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(objetivo);
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

    public List<Objetivo> listar() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT o FROM Objetivo o",
                    Objetivo.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Objetivo> listarActivos() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT o FROM Objetivo o WHERE o.activo = true",
                    Objetivo.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public Objetivo buscarPorId(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Objetivo.class, id);
        } finally {
            em.close();
        }
    }

    public boolean actualizar(Objetivo objetivo) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(objetivo);
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
            Objetivo objetivo = em.find(Objetivo.class, id);

            if (objetivo == null) {
                tx.rollback();
                return false;
            }

            objetivo.setActivo(false);
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
