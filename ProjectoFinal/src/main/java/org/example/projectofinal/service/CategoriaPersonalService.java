package org.example.projectofinal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.projectofinal.dao.EntityManagerAdmin;
import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.CategoriaPersonal;
import org.example.projectofinal.model.Departamento;
import org.example.projectofinal.model.Empleado;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CategoriaPersonalService extends ServiceGeneric<CategoriaPersonal> {

    public CategoriaPersonalService() {
        super(new ImplDao<>(CategoriaPersonal.class));
    }

    public List<CategoriaPersonal> listarOrdenadas() {
        return consultar(
                "SELECT c FROM CategoriaPersonal c ORDER BY c.nombre",
                CategoriaPersonal.class);
    }

    /**
     * Reglas del formulario de categorías.
     * Devuelve el mensaje de error para mostrar, o null si es válido.
     */
    public String validar(CategoriaPersonal c) {
        if (c == null || esVacio(c.getNombre())) {
            return "Escribe el nombre de la categoría.";
        }
        c.setNombre(c.getNombre().trim());
        if (c.getNombre().length() > 50) {
            return "El nombre no puede tener más de 50 caracteres.";
        }
        if (existeNombre(c.getNombre(), c.getId())) {
            return "Ya existe una categoría con ese nombre.";
        }
        return null;
    }

    public boolean registrar(CategoriaPersonal categoria) {

        if (validar(categoria) != null) {
            return false;
        }

        try {
            categoria.setActivo(true);
            guardar(categoria);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean modificar(CategoriaPersonal categoria) {

        if (categoria == null || categoria.getId() == null
                || validar(categoria) != null) {
            return false;
        }

        try {
            actualizar(categoria);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cambiarEstado(UUID id) {

        CategoriaPersonal categoria = buscarPorId(id);

        if (categoria == null) {
            return false;
        }

        categoria.setActivo(!Boolean.TRUE.equals(categoria.getActivo()));
        return modificar(categoria);
    }

    public boolean existeNombre(String nombre, UUID idExcluido) {

        CategoriaPersonal encontrada = consultarUno(
                "SELECT c FROM CategoriaPersonal c WHERE LOWER(c.nombre) = LOWER(:nombre)",
                CategoriaPersonal.class,
                "nombre", nombre.trim());

        return encontrada != null && !encontrada.getId().equals(idExcluido);
    }

    // =========================================================
    // REGLA CATEGORÍA -> DEPARTAMENTO (tabla categoria_departamento)
    // Categoría sin filas: puede ir en cualquier departamento (o sin departamento).
    // Categoría con filas: solo en esos departamentos y el departamento es obligatorio.
    // =========================================================

    /** Ids de los departamentos permitidos para la categoría. Vacío = sin restricción. */
    public Set<UUID> departamentosPermitidos(UUID idCategoria) {

        if (idCategoria == null) {
            return Set.of();
        }

        EntityManager em = EntityManagerAdmin.getEntityManager();

        try {
            List<?> filas = em.createNativeQuery(
                            "SELECT id_departamento FROM categoria_departamento WHERE id_categoria = ?1")
                    .setParameter(1, idCategoria)
                    .getResultList();

            Set<UUID> ids = new HashSet<>();
            for (Object fila : filas) {
                ids.add(fila instanceof UUID u ? u : UUID.fromString(fila.toString()));
            }
            return ids;

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    /** Devuelve el mensaje de error si la combinación no está permitida, o null si es válida. */
    public String validarCombinacion(CategoriaPersonal categoria, Departamento departamento) {

        if (categoria == null) {
            return "Seleccione la categoría.";
        }

        Set<UUID> permitidos = departamentosPermitidos(categoria.getId());

        if (permitidos.isEmpty()) {
            return null;
        }
        if (departamento == null) {
            return "La categoría " + categoria.getNombre() + " requiere un departamento.";
        }
        if (!permitidos.contains(departamento.getId())) {
            return "La categoría " + categoria.getNombre()
                    + " no puede asignarse al departamento " + departamento.getNombre() + ".";
        }
        return null;
    }

    /**
     * Reemplaza los departamentos permitidos de una categoría.
     * No deja guardar si algún empleado de esa categoría quedaría fuera de la regla.
     * Devuelve el mensaje de error, o null si se guardó.
     */
    public String guardarDepartamentosPermitidos(UUID idCategoria, Set<UUID> idsDepartamentos) {

        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            List<Empleado> empleados = em.createQuery(
                            "SELECT e FROM Empleado e LEFT JOIN FETCH e.idDepartamento " +
                                    "WHERE e.idCategoria.id = :cat", Empleado.class)
                    .setParameter("cat", idCategoria)
                    .getResultList();

            if (!idsDepartamentos.isEmpty()) {
                List<String> fuera = empleados.stream()
                        .filter(e -> e.getIdDepartamento() == null
                                || !idsDepartamentos.contains(e.getIdDepartamento().getId()))
                        .map(Empleado::getNombreCompleto)
                        .toList();

                if (!fuera.isEmpty()) {
                    return "No se puede guardar: " + fuera.size() + " empleado(s) de esta categoría "
                            + "quedarían en un departamento no permitido (por ejemplo: "
                            + String.join(", ", fuera.subList(0, Math.min(3, fuera.size())))
                            + "). Cámbialos primero en Gestión de empleados.";
                }
            }

            tx.begin();

            em.createNativeQuery("DELETE FROM categoria_departamento WHERE id_categoria = ?1")
                    .setParameter(1, idCategoria)
                    .executeUpdate();

            for (UUID idDep : idsDepartamentos) {
                em.createNativeQuery(
                                "INSERT INTO categoria_departamento (id_categoria, id_departamento) VALUES (?1, ?2)")
                        .setParameter(1, idCategoria)
                        .setParameter(2, idDep)
                        .executeUpdate();
            }

            tx.commit();
            return null;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return "No se pudieron guardar los departamentos permitidos.";

        } finally {
            EntityManagerAdmin.cerrar(em);
        }
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
