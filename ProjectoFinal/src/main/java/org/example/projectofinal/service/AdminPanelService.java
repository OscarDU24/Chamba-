package org.example.projectofinal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import org.example.projectofinal.dao.CuentaUsuarioDao;
import org.example.projectofinal.dao.EntityManagerAdmin;
import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.CategoriaPersonal;
import org.example.projectofinal.model.CuentaUsuario;
import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.model.Empleado;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminPanelService extends ServiceGeneric<Empleado> {

    private final ImplDao<CategoriaPersonal> categoriaDao;
    private final ImplDao<Departamento> departamentoDao;
    private final CuentaUsuarioDao cuentaUsuarioDao;

    private static final int ITERACIONES = 210000;
    private static final int LONGITUD_HASH = 256;
    private static final int LONGITUD_SALT = 16;

    public AdminPanelService() {
        super(new ImplDao<>(Empleado.class));

        this.categoriaDao = new ImplDao<>(CategoriaPersonal.class);
        this.departamentoDao = new ImplDao<>(Departamento.class);
        this.cuentaUsuarioDao = new CuentaUsuarioDao();
    }

    // =========================================================
    // REGISTRAR EMPLEADO Y CREAR SU CUENTA
    // =========================================================

    public boolean registrarEmpleado(Empleado empleado, String contrasena) {

        if (empleado == null || contrasena == null || contrasena.isBlank()) {
            return false;
        }

        EntityManager em = EntityManagerAdmin.getEntityManager();
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
            EntityManagerAdmin.cerrar(em);
        }
    }

    // =========================================================
    // LISTAR EMPLEADOS
    // =========================================================

    public List<Empleado> listarEmpleados() {

        EntityManager em = EntityManagerAdmin.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT e FROM Empleado e " +
                            "JOIN FETCH e.idCategoria " +
                            "LEFT JOIN FETCH e.idDepartamento " +
                            "ORDER BY e.nombreCompleto",
                    Empleado.class
            ).getResultList();

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    // =========================================================
    // LISTAR CATEGORIAS ACTIVAS
    // =========================================================

    public List<CategoriaPersonal> listarCategorias() {

        return categoriaDao.listar()
                .stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .toList();
    }

    // =========================================================
    // LISTAR DEPARTAMENTOS ACTIVOS
    // =========================================================

    public List<Departamento> listarDepartamentos() {

        return departamentoDao.listar()
                .stream()
                .filter(d -> Boolean.TRUE.equals(d.getActivo()))
                .toList();
    }

    // =========================================================
    // LISTAR ESTADOS DE CUENTAS
    // =========================================================

    public Map<UUID, Boolean> listarEstadosCuentas() {

        EntityManager em = EntityManagerAdmin.getEntityManager();

        try {
            List<Object[]> resultados = em.createQuery(
                    "SELECT c.idEmpleado.id, c.activo FROM CuentaUsuario c",
                    Object[].class
            ).getResultList();

            Map<UUID, Boolean> estados = new HashMap<>();

            for (Object[] resultado : resultados) {
                estados.put(
                        (UUID) resultado[0],
                        (Boolean) resultado[1]
                );
            }

            return estados;

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    // =========================================================
    // ACTUALIZAR EMPLEADO
    // =========================================================

    public boolean actualizarEmpleado(Empleado empleado) {

        if (empleado == null || empleado.getId() == null) {
            return false;
        }

        try {
            super.actualizar(empleado);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // DESACTIVAR EMPLEADO Y SU CUENTA
    // =========================================================

    public boolean desactivarEmpleado(UUID idEmpleado) {

        EntityManager em = EntityManagerAdmin.getEntityManager();
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
            EntityManagerAdmin.cerrar(em);
        }
    }

    // =========================================================
    // REACTIVAR EMPLEADO Y SU CUENTA
    // =========================================================

    public boolean reactivarEmpleado(UUID idEmpleado) {

        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Empleado empleado = em.find(Empleado.class, idEmpleado);

            if (empleado == null) {
                tx.rollback();
                return false;
            }

            empleado.setActivo(true);

            CuentaUsuario cuenta = em.createQuery(
                            "SELECT c FROM CuentaUsuario c " +
                                    "WHERE c.idEmpleado.id = :id",
                            CuentaUsuario.class
                    ).setParameter("id", idEmpleado)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (cuenta != null) {
                cuenta.setActivo(true);
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
            EntityManagerAdmin.cerrar(em);
        }
    }

    // =========================================================
    // CAMBIAR ESTADO DE CUENTA
    // =========================================================

    public boolean cambiarEstadoCuenta(UUID idEmpleado) {

        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            CuentaUsuario cuenta = em.createQuery(
                            "SELECT c FROM CuentaUsuario c " +
                                    "JOIN FETCH c.idEmpleado " +
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
            EntityManagerAdmin.cerrar(em);
        }
    }

    // =========================================================
    // CAMBIAR CONTRASEÑA
    // =========================================================

    public boolean cambiarContrasena(UUID idEmpleado, String nuevaContrasena) {

        if (nuevaContrasena == null || nuevaContrasena.isBlank()) {
            return false;
        }

        EntityManager em = EntityManagerAdmin.getEntityManager();
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

            cuenta.setContrasenaHash(generarHash(nuevaContrasena));

            tx.commit();
            return true;

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            e.printStackTrace();
            return false;

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    // =========================================================
    // BUSCAR CUENTA POR CORREO
    // =========================================================

    public CuentaUsuario buscarCuentaPorCorreo(String correo) {

        if (correo == null || correo.isBlank()) {
            return null;
        }

        return cuentaUsuarioDao.buscarPorCorreoInstitucional(correo);
    }

    // =========================================================
    // GENERAR HASH DE CONTRASEÑA
    // =========================================================

    private String generarHash(String contrasena) throws Exception {

        SecureRandom random = new SecureRandom();

        byte[] salt = new byte[LONGITUD_SALT];
        random.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
                contrasena.toCharArray(),
                salt,
                ITERACIONES,
                LONGITUD_HASH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return ITERACIONES + ":" +
                    Base64.getEncoder().encodeToString(salt) + ":" +
                    Base64.getEncoder().encodeToString(hash);

        } finally {
            spec.clearPassword();
        }
    }

    // =========================================================
    // VERIFICAR CONTRASEÑA PARA EL LOGIN
    // =========================================================

    public boolean verificarContrasena(String contrasena, String hashGuardado) {

        if (contrasena == null || hashGuardado == null) {
            return false;
        }

        try {
            String[] partes = hashGuardado.split(":");

            if (partes.length != 3) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[0]);

            if (iteraciones <= 0 || iteraciones > 1000000) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(partes[1]);
            byte[] hashOriginal = Base64.getDecoder().decode(partes[2]);

            if (salt.length != LONGITUD_SALT ||
                    hashOriginal.length != LONGITUD_HASH / 8) {
                return false;
            }

            PBEKeySpec spec = new PBEKeySpec(
                    contrasena.toCharArray(),
                    salt,
                    iteraciones,
                    LONGITUD_HASH
            );

            try {
                SecretKeyFactory factory =
                        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

                byte[] hashCalculado = factory.generateSecret(spec).getEncoded();

                return MessageDigest.isEqual(hashOriginal, hashCalculado);

            } finally {
                spec.clearPassword();
            }

        } catch (Exception e) {
            return false;
        }
    }
}