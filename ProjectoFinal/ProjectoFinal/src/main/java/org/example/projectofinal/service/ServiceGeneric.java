package org.example.projectofinal.service;

import org.example.projectofinal.dao.IDao;

import java.util.List;

public class ServiceGeneric<T> {

    protected final IDao<T> dao;

    public ServiceGeneric(IDao<T> dao) {
        this.dao = dao;
    }

    public T guardar(T entidad) {
        return dao.guardar(entidad);
    }

    public List<T> listar() {
        return dao.listar();
    }

    public T buscarPorId(Object id) {
        return dao.buscarPorId(id);
    }

    public T actualizar(T entidad) {
        return dao.actualizar(entidad);
    }

    public void eliminar(Object id) {
        dao.eliminar(id);
    }
}