package org.example.projectofinal.service;

import org.example.projectofinal.dao.CuentaUsuarioDao;
import org.example.projectofinal.model.CuentaUsuario;
import org.example.projectofinal.util.SesionUsuario;

public class LoginService {

    private final CuentaUsuarioDao cuentaUsuarioDao = new CuentaUsuarioDao();
    private final AdminPanelService adminPanelService = new AdminPanelService();

    /**
     * Valida correo y contraseña contra la base de datos.
     * Si son correctos inicia la sesión y devuelve la cuenta; si no, devuelve null.
     */
    public CuentaUsuario iniciarSesion(String correo, String contrasena) {

        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            return null;
        }

        // Solo devuelve cuentas activas de empleados activos
        CuentaUsuario cuenta = cuentaUsuarioDao.buscarPorCorreoInstitucional(correo.trim());

        if (cuenta == null) {
            return null;
        }

        if (!adminPanelService.verificarContrasena(contrasena, cuenta.getContrasenaHash())) {
            return null;
        }

        SesionUsuario.iniciarSesion(cuenta);
        return cuenta;
    }

    public void cerrarSesion() {
        SesionUsuario.cerrarSesion();
    }

}
