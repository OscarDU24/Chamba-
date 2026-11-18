package org.example.projectofinal.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import java.util.List;

public class ImplDao<T> implements IDao<T> {

    private final Class<T> entidad;

    public ImplDao(Class<T> entidad) {
        this.entidad = entidad;
    }

    @Override
    public T guardar(T objeto) {
        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(objeto);
            tx.commit();

            return objeto;

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    @Override
    public List<T> listar() {
        EntityManager em = EntityManagerAdmin.getEntityManager();

        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> consulta = cb.createQuery(entidad);

            consulta.select(consulta.from(entidad));

            return em.createQuery(consulta).getResultList();

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    @Override
    public T buscarPorId(Object id) {
        EntityManager em = EntityManagerAdmin.getEntityManager();

        try {
            return em.find(entidad, id);

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    @Override
    public T actualizar(T objeto) {
        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            T actualizado = em.merge(objeto);

            tx.commit();

            return actualizado;

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    @Override
    public void eliminar(Object id) {
        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            T objeto = em.find(entidad, id);

            if (objeto != null) {
                em.remove(objeto);
            }

            tx.commit();

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }
}
