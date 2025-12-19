package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import jakarta.persistence.*;

import java.util.List;

import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class MainTest {

    public static void main(String[] args) {

        EntityManager em = getEntityManager();
        em.getTransaction().begin();


        System.out.println("TEST_Données persistées avec succès !");

        // --- Test de findByName ---
        // Création d'un repository fictif (ou méthode statique pour le test)
        List<Restaurant> found = em.createNamedQuery("Restaurant.findByName", Restaurant.class)
                .setParameter("name", "%Lys%")
                .getResultList();

        System.out.println("Restaurants trouvés : " + found.size());
        for (Restaurant r : found) {
            System.out.println(" - " + r.getName() + " / " + r.getWebsite());
        }
        System.out.println();

        em.close();
    }
}
