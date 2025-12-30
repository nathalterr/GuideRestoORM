package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
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

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantTypeMapper.class);
    private final Connection connection;
    private final Map<Integer, RestaurantType> identityMap = new HashMap<>();

    private static final String SQL_FIND_BY_ID = """
    SELECT numero, libelle, description
    FROM TYPES_GASTRONOMIQUES
    WHERE numero = ?
   """;

    private static final String SQL_FIND_BY_LABEL = """
    SELECT numero, libelle, description
    FROM TYPES_GASTRONOMIQUES
    WHERE libelle = ?
   """;

    private static final String SQL_FIND_BY_DESCRIPTION = """
    SELECT numero, libelle, description
    FROM TYPES_GASTRONOMIQUES
    WHERE libelle = ?
   """;

    private static final String SQL_FIND_ALL = """
    SELECT numero, libelle, description
    FROM TYPES_GASTRONOMIQUES
   """;

    private static final String SQL_UPDATE = """
    UPDATE TYPES_GASTRONOMIQUES SET libelle = ?, description = ? WHERE numero = ?
    """;

    private static final String SQL_EXISTS_BY_NAME = """
    SELECT 1 FROM TYPES_GASTRONOMIQUES
    WHERE libelle = ?
    """;
    private static final String SQL_FIND_BY_NAME= """
    SELECT numero, libelle, description FROM TYPES_GASTRONOMIQUES
    WHERE libelle = ?
    """;

    private static final String SQL_CREATE_GENERATE_ID= """
    SELECT SEQ_TYPES_GASTRONOMIQUES.NEXTVAL FROM dual
    """;

    private static final String SQL_CREATE_INSERT = """
    INSERT INTO TYPES_GASTRONOMIQUES (numero, libelle, description)
    VALUES (?, ?, ?)
    """;


    public RestaurantTypeMapper() {
        this.connection = getConnection();
    }

    public RestaurantType findById(Integer id) {
        EntityManager em = getEntityManager();
        return em.find(RestaurantType.class, id);
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
        EntityManager em = getEntityManager();
        return em.createNamedQuery(
                "RestaurantType.findAll",
                RestaurantType.class
        ).getResultList();
    }

    @Override
    public RestaurantType create(RestaurantType restaurantType) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(restaurantType);
            tx.commit();
            return restaurantType;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur create RestaurantType", e);
            return null;
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

    @Override
    public Set<RestaurantType> findByName(String name) {
        EntityManager em = getEntityManager();
        return new HashSet<>(
                em.createQuery(
                        "SELECT rt FROM RestaurantType rt WHERE rt.name = :name",
                        RestaurantType.class
                )
                        .setParameter("name", name)
                        .getResultList()
        );
    }

    public boolean existsByName(String name) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_EXISTS_BY_NAME)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            logger.error("existsByName SQLException: {}", ex.getMessage());
            throw ex;
        }
    }
}
