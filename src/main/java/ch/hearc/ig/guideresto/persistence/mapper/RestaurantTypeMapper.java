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
    private final Connection connection;
    private final Map<Integer, RestaurantType> identityMap = new HashMap<>();

    public RestaurantTypeMapper() throws SQLException {
        this.connection = getConnection();
    }

    @Override
    public RestaurantType findById(Integer id) {
        if (id == null) return null;

        // 🔹 cache identité
        if (identityMap.containsKey(id)) {
            return identityMap.get(id);
        }

        EntityManager em = JpaUtils.getEntityManager();
        try {
            RestaurantType type = em.createNamedQuery(
                            "RestaurantType.findById",
                            RestaurantType.class
                    )
                    .setParameter("id", id)
                    .getSingleResult();

            identityMap.put(type.getId(), type);
            return type;

        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }


    public List<RestaurantType> findByLabel(String label) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("RestaurantType.findByLabel", RestaurantType.class)
                .setParameter("label", "%" + label + "%")
                .getResultList();
    }

    public List<RestaurantType> findByDescription(String description) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("RestaurantType.findByDescription", RestaurantType.class)
                .setParameter("description", "%" + description + "%")
                .getResultList();
    }

    @Override
    public List<RestaurantType> findAll() {
        EntityManager em = JpaUtils.getEntityManager();
        try {
            return em.createNamedQuery(
                    "RestaurantType.findAll",
                    RestaurantType.class
            ).getResultList();
        } finally {
            em.close();
        }
    }


    @Override
    public RestaurantType create(RestaurantType type) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            // 🔹 Vérifie si le type existe déjà pour éviter doublons
            List<RestaurantType> types = findByName(type.getLabel());
            if (types.isEmpty()) {
                return types.getFirst();
            }

            // 🔹 Persist via Hibernate
            tx.begin();
            em.persist(type);  // Hibernate gère l'ID automatiquement
            tx.commit();

            return type;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.error("Erreur create RestaurantType: {}", e.getMessage(), e);
            return null;
        } finally {
            em.close();
        }
    }


    @Override
    public boolean update(RestaurantType object) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.merge(object);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur update RestaurantType", e);
            return false;
        }
    }

    @Override
    public boolean delete(RestaurantType typeResto) {
        return deleteById(typeResto.getId());
    }

    @Override
    public boolean deleteById(Integer id) {
        EntityManager em = getEntityManager();
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

        } catch (Exception ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("RestaurantType - Exception in deleteById", ex);
            return false;
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

    public List<RestaurantType> findByName(String name) {
        EntityManager em = getEntityManager();
        return new ArrayList<>(
                em.createQuery(
                        "SELECT rt FROM RestaurantType rt WHERE rt.label = :name",
                        RestaurantType.class
                )
                        .setParameter("name", name)
                        .getResultList()
        );
    }

    public boolean existsByName(String name) {
        try (EntityManager em = JpaUtils.getEntityManager()) {
            Long count = em.createNamedQuery("RestaurantType.existsByName", Long.class)
                    .setParameter("label", name)
                    .getSingleResult();
            return count != null && count > 0;
        }
    }

}
