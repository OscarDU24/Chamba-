package org.example.projectofinal.service;

import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.CategoriaPersonal;

import java.util.List;
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

    public List<CategoriaPersonal> listarActivas() {
        return consultar(
                "SELECT c FROM CategoriaPersonal c WHERE c.activo = true ORDER BY c.nombre",
                CategoriaPersonal.class);
    }

    public boolean registrar(CategoriaPersonal categoria) {

        if (categoria == null || esVacio(categoria.getNombre())) {
            return false;
        }

        if (existeNombre(categoria.getNombre(), null)) {
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
                || esVacio(categoria.getNombre())) {
            return false;
        }

        if (existeNombre(categoria.getNombre(), categoria.getId())) {
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

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
