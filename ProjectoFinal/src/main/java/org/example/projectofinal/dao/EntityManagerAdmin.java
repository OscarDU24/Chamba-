package org.example.projectofinal.dao;

import jakarta.persistence.EntityManager;
import org.example.projectofinal.util.JpaUtil;

public class EntityManagerAdmin {

    public static EntityManager getEntityManager() {
        return JpaUtil.getEntityManager();
    }

    public static void cerrar(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
