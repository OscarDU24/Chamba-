package org.example.projectofinal.service;

import org.example.projectofinal.dao.CuentaUsuarioDao;
import org.example.projectofinal.model.CuentaUsuario;

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

    public boolean restablecerContrasena(UUID idEmpleado, String nuevaContrasena) {
        if (nuevaContrasena == null || nuevaContrasena.length() < 8 || nuevaContrasena.length() > 128) {
            return false;
        }
        return adminPanelService.cambiarContrasena(idEmpleado, nuevaContrasena);
    }
}
