package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;

import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;


public class RestaurantMapper extends AbstractMapper<Restaurant> {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantMapper.class);

    public RestaurantMapper() {
    }

    @Override
    public Restaurant create(Restaurant restaurant) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(restaurant);
            tx.commit();
            return restaurant;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur create Restaurant", e);
            return null;
        }
    }

    @Override
    public boolean update(Restaurant restaurant) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur update Restaurant", e);
            return false;
        }
    }
    /**
     * Met à jour l'adresse et la ville d'un restaurant
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

    @Override
    public boolean delete(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) {
            return false;
        }
        return deleteById(restaurant.getId());
    }

    public boolean deleteById(Integer id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Restaurant rest = em.find(Restaurant.class, id);
            if (rest == null) {
                tx.commit();
                return false;
            }

            em.remove(rest); // Hibernate supprime : CompleteEvaluation -> Grades + BasicEvaluation

            tx.commit();

            return true;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.error("Restaurant - ErrordDeleteByID", e);
            return false;
        }
    }

    @Override
    public Restaurant findById(Integer id) {
        EntityManager em = getEntityManager();
        return em.find(Restaurant.class, id);
    }

    @Override
    public List<Restaurant> findAll() {
        EntityManager em = getEntityManager();
        return em.createQuery(
                "SELECT r FROM Restaurant r",
                Restaurant.class
        ).getResultList();
    }

    public List<Restaurant> findByName(String name) {
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("Restaurant.findByName", Restaurant.class)
                    .setParameter("name", "%" + name + "%")
                    .getResultList();
        }
    }

    public List<Restaurant> findByDescription(String description) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("Restaurant.findByDescription", Restaurant.class)
                .setParameter("description", "%" + description + "%")
                .getResultList();
    }

    public List<Restaurant> findByWebsite(String website) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("Restaurant.findByWebsite", Restaurant.class)
                .setParameter("website", "%" + website + "%")
                .getResultList();
    }

    public List<Restaurant> findByLocalisation(String street) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("Restaurant.findByLocalisation", Restaurant.class)
                .setParameter("street", "%" + street + "%")
                .getResultList();
    }

    /**
     * Retourne tous les restaurants situés dans une ville donnée
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
     * Retourne tous les restaurants d'un type donné
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
    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_RESTAURANTS.NEXTVAL FROM dual";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM RESTAURANTS WHERE numero = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM RESTAURANTS";
    }
  }

