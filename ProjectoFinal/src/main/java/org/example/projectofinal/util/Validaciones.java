package org.example.projectofinal.util;

import java.util.regex.Pattern;

/**
 * Reglas de formato compartidas por las pantallas y los services.
 * Cada método devuelve el mensaje de error, o null si el valor es válido.
 */
public final class Validaciones {

    /** Dominio institucional obligatorio para los correos de empleados. */
    public static final String DOMINIO_CORREO = "@uamv.edu.ni";

    /** Solo letras (con tildes, ü y ñ) separadas por un espacio. */
    private static final Pattern NOMBRE = Pattern.compile("^\\p{L}+( \\p{L}+)*$");

    /** Usuario antes de @uamv.edu.ni: letras, números y . _ % + - */
    private static final Pattern CORREO = Pattern.compile(
            "^[A-Za-z0-9._%+-]+" + Pattern.quote(DOMINIO_CORREO) + "$",
            Pattern.CASE_INSENSITIVE);

    /** Caracteres que se pueden escribir en el campo de nombre. */
    private static final Pattern CARACTER_NOMBRE = Pattern.compile("[\\p{L} ]*");

    private Validaciones() {
    }

    public static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "Escribe el nombre completo.";
        }
        String limpio = nombre.trim();
        if (limpio.length() > 150) {
            return "El nombre no puede tener más de 150 caracteres.";
        }
        if (!NOMBRE.matcher(limpio).matches()) {
            return "El nombre solo puede tener letras y espacios (sin números ni símbolos).";
        }
        return null;
    }

    /** Une los espacios repetidos y quita los de los extremos. */
    public static String normalizarNombre(String nombre) {
        return nombre == null ? null : nombre.trim().replaceAll("\\s+", " ");
    }

    /** true si el texto solo contiene letras y espacios (para bloquear al escribir). */
    public static boolean esTextoDeNombre(String texto) {
        return texto != null && CARACTER_NOMBRE.matcher(texto).matches();
    }

    public static String validarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return "Escribe el correo institucional.";
        }
        String limpio = correo.trim();
        if (limpio.length() > 120) {
            return "El correo no puede tener más de 120 caracteres.";
        }
        if (!CORREO.matcher(limpio).matches()) {
            return "El correo debe ser institucional y terminar en " + DOMINIO_CORREO
                    + " (por ejemplo: nombre.apellido" + DOMINIO_CORREO + ").";
        }
        return null;
    }

    public static String validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.isEmpty()) {
            return "Escribe la contraseña.";
        }

        StringBuilder falta = new StringBuilder();
        if (contrasena.length() < 8) agregar(falta, "al menos 8 caracteres");
        if (contrasena.length() > 128) return "La contraseña no puede tener más de 128 caracteres.";
        if (contrasena.chars().noneMatch(Character::isUpperCase)) agregar(falta, "una mayúscula");
        if (contrasena.chars().noneMatch(Character::isLowerCase)) agregar(falta, "una minúscula");
        if (contrasena.chars().noneMatch(Character::isDigit)) agregar(falta, "un número");
        if (contrasena.chars().allMatch(Character::isLetterOrDigit)) agregar(falta, "un signo especial (por ejemplo ! @ # $ %)");
        if (contrasena.chars().anyMatch(Character::isWhitespace)) {
            return "La contraseña no puede tener espacios.";
        }

        return falta.length() == 0 ? null : "A la contraseña le falta: " + falta + ".";
    }

    private static void agregar(StringBuilder sb, String texto) {
        if (sb.length() > 0) sb.append(", ");
        sb.append(texto);
    }
}
