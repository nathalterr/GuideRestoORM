package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;

import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import ch.hearc.ig.guideresto.services.EvaluationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;


public class RestaurantMapper extends AbstractMapper<Restaurant> {

    public RestaurantMapper() {
    }

    /**
     * Méthode de persistence en base de donnée
     * @param restaurant à ajouter en base
     * @return l'objet Restaurant créé, ou null en cas d'erreur
     */
    @Override
    public Restaurant create(Restaurant restaurant, EntityManager em) {
        em.persist(restaurant);  // il devient "managed"
        return restaurant;       // retourne l'entité persistée
    }

    /**
     * Méthode de mise à jour en base de donnée
     * @param restaurant - l'objet Restaurant à mettre à jour
     * @return true si la mise à jour a réussi, false en cas d'erreur
     */
    @Override
    public boolean update(Restaurant restaurant, EntityManager em) {
        em.merge(restaurant);  // merge l'entité avec l'EM courant
        return true;
    }
    /**
     * Méthode de mise à jour de l'adresse et de la ville d'un restaurant
     * @param restaurant - Instance du restaurant à modifier
     * @param newStreet - Nom et numéro de rue
     * @param newCity - Instance de la nouvelle ville
     * @return true si réussi, false si pas réussi
     */
    public boolean updateAddress(Restaurant restaurant, String newStreet, City newCity) {
        if (restaurant == null) return false;

        EntityManager em = JpaUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            int updated = em.createNamedQuery("Restaurant.updateAddress")
                    .setParameter("street", newStreet)
                    .setParameter("city", newCity)
                    .setParameter("id", restaurant.getId())
                    .executeUpdate();

            tx.commit();
            return updated > 0;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Méthode de suppression en base de donnée
     * @param restaurant - l'objet Restaurant à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean delete(Restaurant restaurant, EntityManager em) {
        // Récupérer l'entité gérée par l'EM
        RestaurantType managed = em.find(RestaurantType.class, restaurant.getId());
        if (managed != null) {
            em.remove(managed);
        }
        return true;
    }

    /**
     * Méthode de suppression d'un restaurant en base de donnée par identifiant
     * @param id - identifiant du restaurant à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteById(Integer id, EntityManager em) {
        Restaurant entity = em.find(Restaurant.class, id);
        if (entity != null) {
            em.remove(entity);
        }
        return true;
    }

    /**
     * Méthode de recherche d'un restaurant en base de données par son identifiant.
     * @param id - identifiant du restaurant recherché
     * @return le restaurant trouvé, ou null s'il n'existe pas
     */
    @Override
    public Restaurant findById(Integer id) {
        EntityManager em = getEntityManager();
        return em.find(Restaurant.class, id);
    }

    /**
     * Méthode de recherche de tous les restaurants en base de donnée
     * @return la liste des restaurants trouvés
     */
    @Override
    public List<Restaurant> findAll() {
        EntityManager em = getEntityManager();
        return em.createQuery(
                "SELECT r FROM Restaurant r",
                Restaurant.class
        ).getResultList();
    }

    /**
     * Méthode de recherche de restaurants en base de données par nom.
     * @param name - nom du restaurant recherché
     * @return la liste des restaurants trouvés
     */
    public List<Restaurant> findByName(String name) {
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("Restaurant.findByName", Restaurant.class)
                    .setParameter("name", "%" + name + "%")
                    .getResultList();
        }
    }
    /**
     * Méthode de recherche de restaurants en base de données par description.
     * @param description - description du restaurant recherché
     * @return la liste des restaurants trouvés
     */
    public List<Restaurant> findByDescription(String description) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("Restaurant.findByDescription", Restaurant.class)
                .setParameter("description", "%" + description + "%")
                .getResultList();
    }
    /**
     * Méthode de recherche de restaurants en base de données par site internet.
     * @param website - site web du restaurant recherché
     * @return la liste des restaurants trouvés
     */
    public List<Restaurant> findByWebsite(String website) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("Restaurant.findByWebsite", Restaurant.class)
                .setParameter("website", "%" + website + "%")
                .getResultList();
    }
    /**
     * Méthode de recherche de restaurants en base de données par rue.
     * @param street - rue du restaurant recherché
     * @return la liste des restaurants trouvés
     */
    public List<Restaurant> findByLocalisation(String street) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("Restaurant.findByLocalisation", Restaurant.class)
                .setParameter("street", "%" + street + "%")
                .getResultList();
    }

    /**
     * Méthode de recherche de restaurants en base de données par ville.
     * @param cityName - nom de la ville du restaurant recherché
     * @return la liste des restaurants trouvés
     */
    public Set<Restaurant> findByCity(String cityName) {
        EntityManager em = getEntityManager();
        return new HashSet<>(
                em.createNamedQuery(
                        "Restaurant.findByCity",
                        Restaurant.class
                )
                        .setParameter("cityName", cityName)
                        .getResultList()
        );
        }

    /**
     * Méthode de recherche d'un restaurant en base de données par son type.
     * @param label - type du restaurant recherché
     * @return la liste des restaurants trouvés
     */
    public Set<Restaurant> findByRestaurantType(String label) {
        EntityManager em = getEntityManager();
        return new HashSet<>(
                em.createNamedQuery(
                        "Restaurant.findByRestaurantType",
                        Restaurant.class
                )
                        .setParameter("label", label)
                        .getResultList()
        );
    }
  }

