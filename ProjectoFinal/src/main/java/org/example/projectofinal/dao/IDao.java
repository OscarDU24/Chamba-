package org.example.projectofinal.dao;

import java.util.List;

public interface IDao<T> {

    T guardar(T entidad);

    List<T> listar();

    T buscarPorId(Object id);

    T actualizar(T entidad);

    void eliminar(Object id);
}
