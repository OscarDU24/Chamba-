package org.example.projectofinal.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.projectofinal.model.CuentaUsuario;
import org.example.projectofinal.util.JpaUtil;

import java.util.List;
import java.util.UUID;

public class CuentaUsuarioDao {

    public boolean guardar(CuentaUsuario cuenta) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(cuenta);
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

    public List<CuentaUsuario> listar() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM CuentaUsuario c",
                    CuentaUsuario.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public CuentaUsuario buscarPorId(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(CuentaUsuario.class, id);
        } finally {
            em.close();
        }
    }

    public CuentaUsuario buscarPorEmpleado(UUID idEmpleado) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<CuentaUsuario> lista = em.createQuery(
                    "SELECT c FROM CuentaUsuario c WHERE c.empleado.idEmpleado = :id",
                    CuentaUsuario.class
            ).setParameter("id", idEmpleado).getResultList();

            return lista.isEmpty() ? null : lista.get(0);
        } finally {
            em.close();
        }
    }

    public boolean actualizar(CuentaUsuario cuenta) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(cuenta);
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
            CuentaUsuario cuenta = em.find(CuentaUsuario.class, id);

            if (cuenta == null) {
                tx.rollback();
                return false;
            }

            cuenta.setActivo(false);
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
