package org.example.projectofinal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.projectofinal.dao.CuentaUsuarioDao;
import org.example.projectofinal.dao.EntityManagerAdmin;
import org.example.projectofinal.model.CuentaUsuario;
import org.example.projectofinal.util.Validaciones;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CuentaUsuarioService {

    public static final String ADMINISTRADOR = "ADMINISTRADOR";
    public static final String EMPLEADO = "EMPLEADO";

    private final CuentaUsuarioDao dao = new CuentaUsuarioDao();
    private final AdminPanelService adminPanelService = new AdminPanelService();

    /** Cuentas con su empleado, ordenadas por nombre. */
    public List<CuentaUsuario> listar() {
        return dao.listar().stream()
                .sorted(Comparator.comparing(c -> c.getIdEmpleado().getNombreCompleto(),
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** Alterna el rol entre ADMINISTRADOR y EMPLEADO (valores permitidos por la base). */
    public boolean cambiarRol(UUID idCuenta) {
        CuentaUsuario cuenta = dao.buscarPorId(idCuenta);

        if (cuenta == null) {
            return false;
        }

        cuenta.setRol(ADMINISTRADOR.equals(cuenta.getRol()) ? EMPLEADO : ADMINISTRADOR);
        return dao.actualizar(cuenta);
    }

    public boolean cambiarEstado(UUID idEmpleado) {
        return adminPanelService.cambiarEstadoCuenta(idEmpleado);
    }

    /**
     * Cambia la contraseña de la cuenta del empleado.
     * Devuelve el mensaje de error para mostrar, o null si se cambió.
     */
    public String restablecerContrasena(UUID idEmpleado, String nuevaContrasena, String confirmacion) {
        String error = Validaciones.validarContrasena(nuevaContrasena);
        if (error != null) {
            return error;
        }
        if (!nuevaContrasena.equals(confirmacion)) {
            return "Las contraseñas no coinciden.";
        }
        return adminPanelService.cambiarContrasena(idEmpleado, nuevaContrasena)
                ? null
                : "No se pudo restablecer la contraseña.";
    }

    /**
     * Elimina la cuenta (el empleado se conserva, sin acceso al sistema).
     * Se le puede crear una nueva desde Gestión de empleados.
     */
    public boolean eliminarCuenta(UUID idCuenta) {

        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.createNativeQuery("DELETE FROM administradores_sistema WHERE id_usuario = ?1")
                    .setParameter(1, idCuenta).executeUpdate();
            int borradas = em.createNativeQuery("DELETE FROM cuentas_usuario WHERE id_usuario = ?1")
                    .setParameter(1, idCuenta).executeUpdate();

            tx.commit();
            return borradas == 1;

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
}
