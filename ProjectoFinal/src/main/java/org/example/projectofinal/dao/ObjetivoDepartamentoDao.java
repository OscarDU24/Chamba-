package org.example.projectofinal.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.projectofinal.model.ObjetivoDepartamento;
import org.example.projectofinal.util.JpaUtil;

import java.util.List;
import java.util.UUID;

public class ObjetivoDepartamentoDao {

    public boolean guardar(ObjetivoDepartamento asignacion) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(asignacion);
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

    public List<ObjetivoDepartamento> listar() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM ObjetivoDepartamento a",
                    ObjetivoDepartamento.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public ObjetivoDepartamento buscarPorId(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(ObjetivoDepartamento.class, id);
        } finally {
            em.close();
        }
    }

    public boolean actualizar(ObjetivoDepartamento asignacion) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(asignacion);
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
            ObjetivoDepartamento asignacion =
                    em.find(ObjetivoDepartamento.class, id);

            if (asignacion == null) {
                tx.rollback();
                return false;
            }

            em.remove(asignacion);
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
