
package org.example.projectofinal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.projectofinal.dao.CategoriaPersonalDao;
import org.example.projectofinal.dao.DepartamentoDao;
import org.example.projectofinal.dao.EmpleadoDao;
import org.example.projectofinal.model.CuentaUsuario;
import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.model.CategoriaPersonal;
import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.util.JpaUtil;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminPanelService {

    private static final int ITERACIONES = 210000;
    private static final int LONGITUD_HASH = 256;
    private static final int LONGITUD_SALT = 16;

    private final EmpleadoDao empleadoDao;
    private final DepartamentoDao departamentoDao;
    private final CategoriaPersonalDao categoriaPersonalDao;

    public AdminPanelService() {
        this.empleadoDao = new EmpleadoDao();
        this.departamentoDao = new DepartamentoDao();
        this.categoriaPersonalDao = new CategoriaPersonalDao();
    }

    public boolean registrarEmpleado(Empleado empleado, String contrasena) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            empleado.setActivo(true);
            em.persist(empleado);
            em.flush();

            CuentaUsuario cuenta = new CuentaUsuario();
            cuenta.setIdEmpleado(empleado);
            cuenta.setContrasenaHash(generarHash(contrasena));
            cuenta.setRol("EMPLEADO");
            cuenta.setActivo(true);

            em.persist(cuenta);
            tx.commit();

            return true;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public List<Empleado> listarEmpleados() {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT e FROM Empleado e " +
                            "JOIN FETCH e.idCategoria " +
                            "LEFT JOIN FETCH e.idDepartamento " +
                            "ORDER BY e.nombreCompleto",
                    Empleado.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<CategoriaPersonal> listarCategorias() {
        return categoriaPersonalDao.listar().stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .toList();
    }

    public List<Departamento> listarDepartamentos() {
        return departamentoDao.listar().stream()
                .filter(d -> Boolean.TRUE.equals(d.getActivo()))
                .toList();
    }

    public Map<UUID, Boolean> listarEstadosCuentas() {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            List<Object[]> resultados = em.createQuery(
                    "SELECT c.idEmpleado.id, c.activo FROM CuentaUsuario c",
                    Object[].class
            ).getResultList();

            Map<UUID, Boolean> estados = new HashMap<>();

            for (Object[] resultado : resultados) {
                estados.put((UUID) resultado[0], (Boolean) resultado[1]);
            }

            return estados;
        } finally {
            em.close();
        }
    }

    public boolean actualizarEmpleado(Empleado empleado) {
        return empleadoDao.actualizar(empleado);
    }

    public boolean desactivarEmpleado(UUID idEmpleado) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Empleado empleado = em.find(Empleado.class, idEmpleado);

            if (empleado == null) {
                tx.rollback();
                return false;
            }

            empleado.setActivo(false);

            CuentaUsuario cuenta = em.createQuery(
                            "SELECT c FROM CuentaUsuario c " +
                                    "WHERE c.idEmpleado.id = :id",
                            CuentaUsuario.class
                    ).setParameter("id", idEmpleado)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (cuenta != null) {
                cuenta.setActivo(false);
            }

            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public boolean cambiarEstadoCuenta(UUID idEmpleado) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            CuentaUsuario cuenta = em.createQuery(
                            "SELECT c FROM CuentaUsuario c " +
                                    "WHERE c.idEmpleado.id = :id",
                            CuentaUsuario.class
                    ).setParameter("id", idEmpleado)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (cuenta == null) {
                tx.rollback();
                return false;
            }

            if (!Boolean.TRUE.equals(cuenta.getIdEmpleado().getActivo())) {
                tx.rollback();
                return false;
            }

            cuenta.setActivo(!Boolean.TRUE.equals(cuenta.getActivo()));

            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public boolean cambiarContrasena(UUID idEmpleado, String contrasena) {
        if (contrasena == null || contrasena.isBlank()) {
            return false;
        }

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            CuentaUsuario cuenta = em.createQuery(
                            "SELECT c FROM CuentaUsuario c " +
                                    "WHERE c.idEmpleado.id = :id",
                            CuentaUsuario.class
                    ).setParameter("id", idEmpleado)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (cuenta == null) {
                tx.rollback();
                return false;
            }

            cuenta.setContrasenaHash(generarHash(contrasena));

            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    private String generarHash(String contrasena) throws Exception {
        byte[] salt = new byte[LONGITUD_SALT];
        new SecureRandom().nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
                contrasena.toCharArray(),
                salt,
                ITERACIONES,
                LONGITUD_HASH
        );

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(
                    "PBKDF2WithHmacSHA256"
            );

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return ITERACIONES + ":" +
                    Base64.getEncoder().encodeToString(salt) + ":" +
                    Base64.getEncoder().encodeToString(hash);

        } finally {
            spec.clearPassword();
        }
    }
}
