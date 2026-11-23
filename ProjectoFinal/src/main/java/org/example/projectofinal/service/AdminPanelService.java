package org.example.projectofinal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import org.example.projectofinal.dao.EntityManagerAdmin;
import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.CategoriaPersonal;
import org.example.projectofinal.model.CuentaUsuario;
import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.model.Empleado;
import org.example.projectofinal.util.Validaciones;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AdminPanelService extends ServiceGeneric<Empleado> {

    private final ImplDao<CategoriaPersonal> categoriaDao;
    private final ImplDao<Departamento> departamentoDao;
    private final CategoriaPersonalService categoriaService = new CategoriaPersonalService();

    private static final int ITERACIONES = 210000;
    private static final int LONGITUD_HASH = 256;
    private static final int LONGITUD_SALT = 16;

    public AdminPanelService() {
        super(new ImplDao<>(Empleado.class));

        this.categoriaDao = new ImplDao<>(CategoriaPersonal.class);
        this.departamentoDao = new ImplDao<>(Departamento.class);
    }

    // =========================================================
    // REGISTRAR EMPLEADO Y CREAR SU CUENTA
    // =========================================================

    // =========================================================
    // VALIDAR EMPLEADO
    // Todas las reglas del formulario de empleados en un solo lugar.
    // Devuelve el mensaje de error para mostrar, o null si es válido.
    //   contrasena: obligatoria si el empleado es nuevo (id == null);
    //   al editar, solo se valida si viene escrita.
    // =========================================================

    public String validarEmpleado(Empleado empleado, String contrasena) {

        if (empleado == null) {
            return "Faltan los datos del empleado.";
        }

        normalizar(empleado);

        String error = Validaciones.validarNombre(empleado.getNombreCompleto());
        if (error != null) {
            return error;
        }

        if (empleado.getCif() == null || empleado.getCif().isEmpty()) {
            return "Escribe el CIF del empleado.";
        }
        if (empleado.getCif().length() > 120) {
            return "El CIF no puede tener más de 120 caracteres.";
        }

        // El dominio @uamv.edu.ni se exige a correos nuevos o cambiados,
        // para poder seguir editando empleados registrados antes de la regla.
        if (correoCambio(empleado)) {
            error = Validaciones.validarCorreo(empleado.getCorreoInstitucional());
            if (error != null) {
                return error;
            }
        }

        if (empleado.getIdCategoria() == null) {
            return "Selecciona la categoría.";
        }

        error = validarCategoriaDepartamento(empleado);
        if (error != null) {
            return error;
        }

        boolean esNuevo = empleado.getId() == null;
        if (esNuevo || (contrasena != null && !contrasena.isEmpty())) {
            error = Validaciones.validarContrasena(contrasena);
            if (error != null) {
                return error;
            }
        }

        if (existeOtro("SELECT e FROM Empleado e WHERE LOWER(e.cif) = LOWER(:valor)",
                empleado.getCif(), empleado.getId())) {
            return "Ya existe otro empleado con el CIF " + empleado.getCif() + ".";
        }
        if (existeOtro("SELECT e FROM Empleado e WHERE LOWER(e.correoInstitucional) = LOWER(:valor)",
                empleado.getCorreoInstitucional(), empleado.getId())) {
            return "Ya existe otro empleado con el correo " + empleado.getCorreoInstitucional() + ".";
        }

        return null;
    }

    /** Quita espacios de más del nombre, CIF y correo antes de validar o guardar. */
    private void normalizar(Empleado empleado) {
        empleado.setNombreCompleto(Validaciones.normalizarNombre(empleado.getNombreCompleto()));
        if (empleado.getCif() != null) {
            empleado.setCif(empleado.getCif().trim());
        }
        if (empleado.getCorreoInstitucional() != null) {
            empleado.setCorreoInstitucional(empleado.getCorreoInstitucional().trim());
        }
    }

    private boolean existeOtro(String jpql, String valor, UUID idExcluido) {
        Empleado encontrado = consultarUno(jpql, Empleado.class, "valor", valor);
        return encontrado != null && !encontrado.getId().equals(idExcluido);
    }

    public boolean registrarEmpleado(Empleado empleado, String contrasena) {

        if (empleado == null || empleado.getId() != null
                || validarEmpleado(empleado, contrasena) != null) {
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
    // VALIDAR CATEGORÍA / DEPARTAMENTO
    // =========================================================

    /** Mensaje de error si la combinación no está permitida, o null si es válida. */
    public String validarCategoriaDepartamento(Empleado empleado) {
        return categoriaService.validarCombinacion(
                empleado.getIdCategoria(), empleado.getIdDepartamento());
    }

    /** Ids de departamentos permitidos para la categoría (vacío = cualquiera). */
    public Set<UUID> departamentosPermitidos(UUID idCategoria) {
        return categoriaService.departamentosPermitidos(idCategoria);
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

        if (empleado == null || empleado.getId() == null
                || validarEmpleado(empleado, null) != null) {
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

    /** true si el correo es distinto al guardado (o el empleado es nuevo). */
    public boolean correoCambio(Empleado empleado) {
        if (empleado.getId() == null) {
            return true;
        }
        Empleado guardado = buscarPorId(empleado.getId());
        return guardado == null || guardado.getCorreoInstitucional() == null
                || !guardado.getCorreoInstitucional().equalsIgnoreCase(
                        empleado.getCorreoInstitucional() == null ? "" : empleado.getCorreoInstitucional().trim());
    }

    // =========================================================
    // ELIMINAR EMPLEADO (definitivo)
    // Solo si no tiene historial; si lo tiene, se debe desactivar.
    // Devuelve el mensaje de error, o null si se eliminó.
    // =========================================================

    public String eliminarEmpleado(UUID idEmpleado) {

        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            Empleado empleado = em.find(Empleado.class, idEmpleado);

            if (empleado == null) {
                return "El empleado ya no existe.";
            }

            long seguimientos = contar(em,
                    "SELECT COUNT(s) FROM Seguimiento s " +
                            "WHERE s.idEmpleado.id = :id OR s.registradoPor.id = :id", idEmpleado);
            long evaluaciones = contar(em,
                    "SELECT COUNT(e) FROM Evaluacion e " +
                            "WHERE e.idEmpleado.id = :id OR e.evaluadoPor.id = :id", idEmpleado);
            long aCargo = contar(em,
                    "SELECT COUNT(s) FROM Supervision s WHERE s.idJefe.id = :id", idEmpleado);

            if (seguimientos + evaluaciones + aCargo > 0) {
                List<String> motivos = new ArrayList<>();
                if (seguimientos > 0) motivos.add(seguimientos + " seguimiento(s)");
                if (evaluaciones > 0) motivos.add(evaluaciones + " evaluación(es)");
                if (aCargo > 0) motivos.add("es o fue jefe en " + aCargo + " supervisión(es)");

                return empleado.getNombreCompleto() + " tiene historial ("
                        + String.join(", ", motivos) + "). Eliminarlo borraría esos registros; "
                        + "usa \"Desactivar empleado\" en su lugar.";
            }

            tx.begin();

            // Lo que depende solo de este empleado se borra con él
            em.createNativeQuery("DELETE FROM responsables_objetivos WHERE id_empleado = ?1")
                    .setParameter(1, idEmpleado).executeUpdate();
            em.createNativeQuery("DELETE FROM supervisiones WHERE id_empleado = ?1")
                    .setParameter(1, idEmpleado).executeUpdate();
            em.createNativeQuery("DELETE FROM administradores_sistema WHERE id_usuario IN "
                            + "(SELECT id_usuario FROM cuentas_usuario WHERE id_empleado = ?1)")
                    .setParameter(1, idEmpleado).executeUpdate();
            em.createNativeQuery("DELETE FROM cuentas_usuario WHERE id_empleado = ?1")
                    .setParameter(1, idEmpleado).executeUpdate();
            em.createNativeQuery("DELETE FROM empleados WHERE id_empleado = ?1")
                    .setParameter(1, idEmpleado).executeUpdate();

            tx.commit();
            return null;

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            e.printStackTrace();
            return "No se pudo eliminar el empleado.";

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    private long contar(EntityManager em, String jpql, UUID id) {
        return em.createQuery(jpql, Long.class).setParameter("id", id).getSingleResult();
    }

    // =========================================================
    // CREAR CUENTA PARA UN EMPLEADO QUE NO TIENE
    // (por ejemplo, después de eliminar su cuenta en Usuarios)
    // =========================================================

    public boolean crearCuenta(UUID idEmpleado, String contrasena) {

        if (Validaciones.validarContrasena(contrasena) != null) {
            return false;
        }

        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Empleado empleado = em.find(Empleado.class, idEmpleado);
            boolean yaTiene = !em.createQuery(
                            "SELECT c.id FROM CuentaUsuario c WHERE c.idEmpleado.id = :id", UUID.class)
                    .setParameter("id", idEmpleado).getResultList().isEmpty();

            if (empleado == null || yaTiene) {
                tx.rollback();
                return false;
            }

            CuentaUsuario cuenta = new CuentaUsuario();
            cuenta.setIdEmpleado(empleado);
            cuenta.setContrasenaHash(generarHash(contrasena));
            cuenta.setRol("EMPLEADO");
            cuenta.setActivo(Boolean.TRUE.equals(empleado.getActivo()));
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

        if (Validaciones.validarContrasena(nuevaContrasena) != null) {
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