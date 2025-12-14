package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.HashSet;

import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class MainTest {

    public static void main(String[] args) {

        EntityManager em = getEntityManager();
        em.getTransaction().begin();

// --- Création de la ville ---
        City paris = new City();
        paris.setCityName("TEST_Paris");
        paris.setZipCode("75001");
        em.persist(paris);

// --- Création du type de restaurant ---
        RestaurantType italian = new RestaurantType();
        italian.setLabel("TEST_Ithaztugtgrwlwiwen");
        italian.setDescription("TEST_Cuiswhine iwtztutaliennegtrgtr trawditionnelle");
        italian.setRestaurants(new HashSet<>());
        em.persist(italian);

// --- Création de la localisation ---
        Localisation loc = new Localisation();
        loc.setStreet("TEST_12 rue de Rivoli");
        loc.setCity(paris);

// --- Création du restaurant ---
        Restaurant r1 = new Restaurant();
        r1.setName("TEST_La Bellah wita");
        r1.setDescription("TEST_Restaurtzhant italien cosy");
        r1.setWebsite("www.TEST_labgrtgtrelwlavhita.fr");
        r1.setAddress(loc);
        r1.setType(italian);
        r1.setCompleteEvaluations(new HashSet<>());
        r1.setBasicEvaluations(new HashSet<>());

        italian.getRestaurants().add(r1);

        em.persist(r1);

// --- Création d'une évaluation complète ---
        CompleteEvaluation eval1 = new CompleteEvaluation();
        eval1.setVisitDate(new java.util.Date());
        eval1.setComment("TEST_Super accueil, excellente cuisine");
        eval1.setUsername("TEST_Cedric");
        eval1.setRestaurant(r1);
        eval1.setGrades(new HashSet<>());

        em.persist(eval1);

// --- Création d'un critère ---
        EvaluationCriteria crit1 = new EvaluationCriteria();
        crit1.setName("TEST_Quahliztututérgtg dwuw plat");
        crit1.setDescription("TEST_Évawlue la qztutgrfgritwé desh plats");
        em.persist(crit1);

// --- Création d'une note ---
        Grade grade1 = new Grade();
        grade1.setGrade(18);
        grade1.setEvaluation(eval1);
        grade1.setCriteria(crit1);
        em.persist(grade1);

        BasicEvaluation be = new BasicEvaluation();
        be.setRestaurant(r1);
        be.setLikeRestaurant(true);
        be.setVisitDate(new java.util.Date());
        be.setIpAddress("9.8.7.6");
        em.persist(be);

// Ajout de la note dans l'évaluation
        eval1.getGrades().add(grade1);

// Ajout de l'évaluation dans le restaurant
        r1.getCompleteEvaluations().add(eval1);

        em.getTransaction().commit();
        System.out.println("TEST_Données persistées avec succès !");
        em.close();

    }
}
