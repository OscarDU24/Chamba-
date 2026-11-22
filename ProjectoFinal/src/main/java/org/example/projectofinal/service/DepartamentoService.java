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

    public boolean registrar(Departamento departamento) {

        if (departamento == null || esVacio(departamento.getNombre())) {
            return false;
        }

        if (existeNombre(departamento.getNombre(), null)) {
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
                || esVacio(departamento.getNombre())) {
            return false;
        }

        if (existeNombre(departamento.getNombre(), departamento.getId())) {
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
