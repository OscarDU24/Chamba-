package org.example.projectofinal.service;

import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.AdministradoresSistema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AdministradoresSistemaService extends ServiceGeneric<AdministradoresSistema> {

    public AdministradoresSistemaService() {
        super(new ImplDao<>(AdministradoresSistema.class));
    }

    public List<AdministradoresSistema> listarConDetalle() {
        return consultar(
                "SELECT a FROM AdministradoresSistema a " +
                        "JOIN FETCH a.idUsuario u JOIN FETCH u.idEmpleado " +
                        "ORDER BY a.fechaCreacion DESC",
                AdministradoresSistema.class);
    }

    /** true si la cuenta de usuario es administrador activo del sistema. */
    public boolean esAdministrador(UUID idCuentaUsuario) {
        return consultarUno(
                "SELECT a FROM AdministradoresSistema a " +
                        "WHERE a.idUsuario.id = :id AND a.activo = true",
                AdministradoresSistema.class,
                "id", idCuentaUsuario) != null;
    }

    public boolean registrar(AdministradoresSistema administrador) {

        if (administrador == null || administrador.getIdUsuario() == null) {
            return false;
        }

        if (esAdministrador(administrador.getIdUsuario().getId())) {
            return false;
        }

        try {
            administrador.setActivo(true);
            if (administrador.getFechaCreacion() == null) {
                administrador.setFechaCreacion(Instant.now());
            }
            guardar(administrador);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cambiarEstado(UUID id) {

        AdministradoresSistema administrador = buscarPorId(id);

        if (administrador == null) {
            return false;
        }

        try {
            administrador.setActivo(!Boolean.TRUE.equals(administrador.getActivo()));
            actualizar(administrador);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
