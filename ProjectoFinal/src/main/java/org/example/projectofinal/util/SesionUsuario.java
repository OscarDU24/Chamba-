
package org.example.projectofinal.util;

import org.example.projectofinal.model.CuentaUsuario;

public class SesionUsuario {

    private static CuentaUsuario cuentaUsuario;

    public static void iniciarSesion(CuentaUsuario cuenta) {
        cuentaUsuario = cuenta;
    }

    public static CuentaUsuario getCuentaUsuario() {
        return cuentaUsuario;
    }

    public static void cerrarSesion() {
        cuentaUsuario = null;
    }

}
