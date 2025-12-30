package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
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

public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation> {

    private static final Logger logger = LoggerFactory.getLogger(BasicEvaluationMapper.class);
    private final Connection connection;
    private final RestaurantMapper restaurantMapper;
    private final Map<Integer, BasicEvaluation> identityMap = new HashMap<>();

    private static final String SQL_FIND_BY_ID = """
        SELECT numero, date_eval, appreciation, adresse_ip, fk_rest
        FROM LIKES
        WHERE numero = ?
        """;

    private static final String SQL_FIND_ALL = """
        SELECT numero, date_eval, appreciation, adresse_ip, fk_rest
        FROM LIKES
        """;

    private static final String SQL_CREATE = """
        INSERT INTO LIKES (date_eval, appreciation, adresse_ip, fk_rest)
        VALUES (?, ?, ?, ?)
        """;

    private static final String SQL_UPDATE = """
        UPDATE LIKES
        SET date_eval = ?, appreciation = ?, adresse_ip = ?, fk_rest = ?
        WHERE numero = ?
        """;

    private static final String SQL_FIND_BY_RESTAURANT = """
        SELECT numero, date_eval, appreciation, adresse_ip, fk_rest
        FROM LIKES
        WHERE fk_rest = ?
        """;

    private static final String SQL_FIND_BY_IP_AND_RESTAURANT = """
        SELECT numero, date_eval, appreciation, adresse_ip, fk_rest
        FROM LIKES
        WHERE adresse_ip = ? AND fk_rest = ?
        """;


    public BasicEvaluationMapper() throws SQLException {
        this.connection = getConnection();
        this.restaurantMapper = new RestaurantMapper();
    }


    public BasicEvaluation findById(Integer id) {
        EntityManager em = getEntityManager();
        return em.find(BasicEvaluation.class, id);
    }

    public List<BasicEvaluation> findByLikeRestaurant(Boolean likeRestaurant) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("BasicEvaluation.findByLikeRestaurant", BasicEvaluation.class)
                .setParameter("likeRestaurant", "%" + likeRestaurant + "%")
                .getResultList();
    }

    public List<BasicEvaluation> findByIpAddress(String ipAddress) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("BasicEvaluation.findByIpAddress", BasicEvaluation.class)
                .setParameter("ipAddress", "%" + ipAddress + "%")
                .getResultList();
    }

    @Override
    public List<BasicEvaluation> findAll() {
        EntityManager em = getEntityManager();
        return em.createQuery(
                "SELECT l FROM likes, l",
                BasicEvaluation.class
        ).getResultList();
    }

    @Override
    public BasicEvaluation create(BasicEvaluation eval) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(eval);
            tx.commit();
            return eval;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur create BasicEvaluation", e);
            return null;
        }
    }

    @Override
    public boolean update(BasicEvaluation eval) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.merge(eval);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur update BasicEvaluation", e);
            return false;
        }
    }

    @Override
    public boolean delete(BasicEvaluation eval) {
        return deleteById(eval.getId());
    }

    @Override
    public boolean deleteById(Integer id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            BasicEvaluation entity = em.find(BasicEvaluation.class, id);
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
            logger.error("Basic Evaluation - Exception in deleteById", ex);
            return false;
        }
    }


    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_EVAL.NEXTVAL FROM dual";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM LIKES WHERE numero = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM LIKES";
    }

    public List<BasicEvaluation> findByRestaurant(Restaurant restaurant) {
        EntityManager em = JpaUtils.getEntityManager();
        try {
            return new ArrayList<>(
                    em.createNamedQuery("BasicEvaluation.findByRestaurant", BasicEvaluation.class)
                            .setParameter("restaurant", restaurant)
                            .getResultList()
            );
        } finally {
            em.close();
        }
    }

    @Override
    public Set<BasicEvaluation> findByIpAndRest(String ip, Integer restaurantId) {
        EntityManager em = getEntityManager();

        return em.createQuery(
                        "SELECT l FROM likes l" +
                                "WHERE l.ipAddress = :ip" +
                                "AND l.restaurant.id = :restaurantId",
                        BasicEvaluation.class
                )
                .setParameter("ip", ip)
                .setParameter("restaurantId", restaurantId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}

