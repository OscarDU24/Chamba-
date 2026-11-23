package org.example.projectofinal.service;

import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.Departamento;

import java.util.List;
import java.util.UUID;

public class DepartamentoService extends ServiceGeneric<Departamento> {

    public DepartamentoService() {
        super(new ImplDao<>(Departamento.class));
    }

    public List<Departamento> listarOrdenados() {
        return consultar(
                "SELECT d FROM Departamento d ORDER BY d.nombre",
                Departamento.class);
    }

    public List<Departamento> listarActivos() {
        return consultar(
                "SELECT d FROM Departamento d WHERE d.activo = true ORDER BY d.nombre",
                Departamento.class);
    }

    private static final java.util.regex.Pattern CORREO =
            java.util.regex.Pattern.compile("^[\\w.%+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final java.util.regex.Pattern TELEFONO =
            java.util.regex.Pattern.compile("^[0-9+() -]{4,20}$");

    /**
     * Reglas del formulario de departamentos.
     * Devuelve el mensaje de error para mostrar, o null si es válido.
     */
    public String validar(Departamento d) {
        if (d == null || esVacio(d.getNombre())) {
            return "Escribe el nombre del departamento.";
        }
        d.setNombre(d.getNombre().trim());
        if (d.getNombre().length() > 100) {
            return "El nombre no puede tener más de 100 caracteres.";
        }
        if (existeNombre(d.getNombre(), d.getId())) {
            return "Ya existe un departamento con ese nombre.";
        }
        if (d.getUbicacion() != null && d.getUbicacion().length() > 150) {
            return "La ubicación no puede tener más de 150 caracteres.";
        }
        if (d.getCorreoContacto() != null
                && (d.getCorreoContacto().length() > 120 || !CORREO.matcher(d.getCorreoContacto()).matches())) {
            return "El correo de contacto no es válido.";
        }
        if (d.getTelefono() != null && !TELEFONO.matcher(d.getTelefono()).matches()) {
            return "El teléfono solo puede tener números, espacios, +, - y paréntesis (4 a 20 caracteres).";
        }
        return null;
    }

    public boolean registrar(Departamento departamento) {

        if (validar(departamento) != null) {
            return false;
        }

        try {
            departamento.setActivo(true);
            guardar(departamento);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean modificar(Departamento departamento) {

        if (departamento == null || departamento.getId() == null
                || validar(departamento) != null) {
            return false;
        }

        try {
            actualizar(departamento);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cambiarEstado(UUID id) {

        Departamento departamento = buscarPorId(id);

        if (departamento == null) {
            return false;
        }

        departamento.setActivo(!Boolean.TRUE.equals(departamento.getActivo()));
        return modificar(departamento);
    }

    public boolean existeNombre(String nombre, UUID idExcluido) {

        Departamento encontrado = consultarUno(
                "SELECT d FROM Departamento d WHERE LOWER(d.nombre) = LOWER(:nombre)",
                Departamento.class,
                "nombre", nombre.trim());

        return encontrado != null && !encontrado.getId().equals(idExcluido);
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
