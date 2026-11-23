package org.example.projectofinal.service;

import org.example.projectofinal.dao.ImplDao;
import org.example.projectofinal.model.Objetivo;

import java.util.List;
import java.util.UUID;

public class ObjetivoService extends ServiceGeneric<Objetivo> {

    public ObjetivoService() {
        super(new ImplDao<>(Objetivo.class));
    }

    public List<Objetivo> listarConDetalle() {
        return consultar(
                "SELECT o FROM Objetivo o LEFT JOIN FETCH o.idObjetivoPadre ORDER BY o.nombre",
                Objetivo.class);
    }

    public static final String GENERAL = "GENERAL";
    public static final String ESPECIFICO = "ESPECIFICO";

    /**
     * Reglas de la base: GENERAL no tiene padre, ESPECIFICO sí;
     * la meta no puede ser negativa.
     */
    public String validar(Objetivo objetivo) {

        if (objetivo == null || objetivo.getNombre() == null || objetivo.getNombre().isBlank()) {
            return "Escribe el nombre del objetivo.";
        }
        if (objetivo.getNombre().trim().length() > 150) {
            return "El nombre no puede tener más de 150 caracteres.";
        }
        if (!GENERAL.equals(objetivo.getNivel()) && !ESPECIFICO.equals(objetivo.getNivel())) {
            return "Selecciona el nivel del objetivo.";
        }
        if (GENERAL.equals(objetivo.getNivel()) && objetivo.getIdObjetivoPadre() != null) {
            return "Un objetivo GENERAL no puede tener objetivo padre.";
        }
        if (ESPECIFICO.equals(objetivo.getNivel()) && objetivo.getIdObjetivoPadre() == null) {
            return "Un objetivo ESPECÍFICO debe tener un objetivo general como padre.";
        }
        if (objetivo.getMeta() != null && objetivo.getMeta().signum() < 0) {
            return "La meta no puede ser negativa.";
        }
        if (objetivo.getId() != null && objetivo.getIdObjetivoPadre() != null
                && objetivo.getId().equals(objetivo.getIdObjetivoPadre().getId())) {
            return "Un objetivo no puede ser su propio padre.";
        }
        return null;
    }

    public boolean registrar(Objetivo objetivo) {

        if (validar(objetivo) != null) {
            return false;
        }

        try {
            objetivo.setActivo(true);
            guardar(objetivo);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean modificar(Objetivo objetivo) {

        if (objetivo == null || objetivo.getId() == null || validar(objetivo) != null) {
            return false;
        }

        try {
            actualizar(objetivo);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cambiarEstado(UUID id) {

        Objetivo objetivo = buscarPorId(id);

        if (objetivo == null) {
            return false;
        }

        objetivo.setActivo(!Boolean.TRUE.equals(objetivo.getActivo()));
        return modificar(objetivo);
    }
}
