package org.example.projectofinal.service;

import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.ResponsableObjetivo;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ResponsableObjetivoService extends ServiceGeneric<ResponsableObjetivo> {

    private static final String SELECT_BASE =
            "SELECT r FROM ResponsableObjetivo r " +
                    "JOIN FETCH r.idEmpleado " +
                    "JOIN FETCH r.idAsignacion a " +
                    "JOIN FETCH a.idObjetivo ";

    public ResponsableObjetivoService() {
        super(new ImplDao<>(ResponsableObjetivo.class));
    }

    public List<ResponsableObjetivo> listarPorAsignacion(UUID idAsignacion) {
        return consultar(SELECT_BASE + "WHERE a.id = :id",
                ResponsableObjetivo.class, "id", idAsignacion);
    }

    public List<ResponsableObjetivo> listarPorEmpleado(UUID idEmpleado) {
        return consultar(SELECT_BASE + "WHERE r.idEmpleado.id = :id",
                ResponsableObjetivo.class, "id", idEmpleado);
    }

    public boolean asignar(ResponsableObjetivo responsable) {

        if (responsable == null
                || responsable.getIdAsignacion() == null
                || responsable.getIdEmpleado() == null) {
            return false;
        }

        boolean yaAsignado = consultarUno(
                "SELECT r FROM ResponsableObjetivo r " +
                        "WHERE r.idAsignacion.id = :asig AND r.idEmpleado.id = :emp",
                ResponsableObjetivo.class,
                "asig", responsable.getIdAsignacion().getId(),
                "emp", responsable.getIdEmpleado().getId()) != null;

        if (yaAsignado) {
            return false;
        }

        try {
            if (responsable.getFechaAsignacion() == null) {
                responsable.setFechaAsignacion(LocalDate.now());
            }
            guardar(responsable);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean quitar(UUID idResponsable) {
        try {
            eliminar(idResponsable);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
