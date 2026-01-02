package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantTypeMapper.class);

    public RestaurantTypeMapper() {
    }

    @Override
    public RestaurantType create(RestaurantType type) {
        try (EntityManager em = getEntityManager()) {

            // Vérifie si un type avec le même label existe déjà
            List<RestaurantType> existingTypes = findByName(type.getLabel());
            if (!existingTypes.isEmpty()) {
                return existingTypes.get(0);
            }

            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(type);
                tx.commit();
                return type;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("Erreur create RestaurantType", e);
                return null;
            }
        }
    }

    @Override
    public boolean update(RestaurantType type) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.merge(type);
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("Erreur update RestaurantType", e);
                return false;
            }
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                RestaurantType entity = em.find(RestaurantType.class, id);
                if (entity == null) {
                    tx.commit();
                    return false;
                }
                em.remove(entity);
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("Erreur deleteById RestaurantType", e);
                return false;
            }
        }
    }

    @Override
    public boolean delete(RestaurantType type) {
        if (type == null || type.getId() == null) return false;
        return deleteById(type.getId());
    }


    @Override
    public RestaurantType findById(Integer id) {
        if (id == null) return null;

        try (EntityManager em = JpaUtils.getEntityManager()) {
            try {
                return em.createNamedQuery("RestaurantType.findById", RestaurantType.class)
                        .setParameter("id", id)
                        .getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        }
    }

    @Override
    public List<RestaurantType> findAll() {
        try (EntityManager em = JpaUtils.getEntityManager()) {
            return em.createNamedQuery("RestaurantType.findAll", RestaurantType.class)
                    .getResultList();
        }
    }

    public List<RestaurantType> findByLabel(String label) {
        try (EntityManager em = JpaUtils.getEntityManager()) {
            return em.createNamedQuery("RestaurantType.findByLabel", RestaurantType.class)
                    .setParameter("label", "%" + label + "%")
                    .getResultList();
        }
    }

    public List<RestaurantType> findByDescription(String description) {
        try (EntityManager em = JpaUtils.getEntityManager()) {
            return em.createNamedQuery("RestaurantType.findByDescription", RestaurantType.class)
                    .setParameter("description", "%" + description + "%")
                    .getResultList();
        }
    }

    public List<RestaurantType> findByName(String name) {
        if (name == null || name.isEmpty()) return List.of();

        try (EntityManager em = JpaUtils.getEntityManager()) {
            return em.createQuery(
                            "SELECT rt FROM RestaurantType rt WHERE rt.label = :name",
                            RestaurantType.class
                    )
                    .setParameter("name", name)
                    .getResultList();
        }
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_TYPES_GASTRONOMIQUES.NEXTVAL FROM dual";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM TYPES_GASTRONOMIQUES WHERE numero = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM TYPES_GASTRONOMIQUES";
    }

    public boolean existsByName(String name) {
        if (name == null || name.isEmpty()) return false;

        try (EntityManager em = JpaUtils.getEntityManager()) {
            Long count = em.createNamedQuery("RestaurantType.existsByName", Long.class)
                    .setParameter("label", name)
                    .getSingleResult();
            return count != null && count > 0;
        }
    }
}
