package org.example.projectofinal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.projectofinal.dao.EntityManagerAdmin;
import org.example.projectofinal.dao.IDao;

import java.util.List;

public class ServiceGeneric<T> {

    protected final IDao<T> dao;

    public ServiceGeneric(IDao<T> dao) {
        this.dao = dao;
    }

    public T guardar(T entidad) {
        return dao.guardar(entidad);
    }

    public List<T> listar() {
        return dao.listar();
    }

    public T buscarPorId(Object id) {
        return dao.buscarPorId(id);
    }

    public T actualizar(T entidad) {
        return dao.actualizar(entidad);
    }

    public void eliminar(Object id) {
        dao.eliminar(id);
    }

    // =========================================================
    // CONSULTAS JPQL PARA LOS SERVICES HIJOS
    // parametros: nombre1, valor1, nombre2, valor2, ...
    // =========================================================

    protected <R> List<R> consultar(String jpql, Class<R> tipo, Object... parametros) {

        EntityManager em = EntityManagerAdmin.getEntityManager();

        try {
            return crearConsulta(em, jpql, tipo, parametros).getResultList();

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    protected <R> R consultarUno(String jpql, Class<R> tipo, Object... parametros) {

        EntityManager em = EntityManagerAdmin.getEntityManager();

        try {
            return crearConsulta(em, jpql, tipo, parametros)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    private <R> TypedQuery<R> crearConsulta(EntityManager em, String jpql,
                                            Class<R> tipo, Object... parametros) {

        if (parametros.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Los parámetros deben ir en pares nombre/valor");
        }

        TypedQuery<R> consulta = em.createQuery(jpql, tipo);

        for (int i = 0; i < parametros.length; i += 2) {
            consulta.setParameter((String) parametros[i], parametros[i + 1]);
        }

        return consulta;
    }
}
