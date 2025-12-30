package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.sql.*;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class CompleteEvaluationMapper extends AbstractMapper<CompleteEvaluation> {

    private static final Map<Integer, CompleteEvaluation> identityMap = new HashMap<>();
    private final Connection connection;
    private RestaurantMapper restaurantMapper;
    private GradeMapper gradeMapper;

    private static final String SQL_FIND_BY_ID = """
            SELECT numero, date_eval, commentaire, nom_utilisateur, fk_rest
            FROM COMMENTAIRES
            WHERE numero = ?
            """;

    private static final String SQL_FIND_ALL = """
            SELECT numero, date_eval, commentaire, nom_utilisateur, fk_rest
            FROM COMMENTAIRES
            """;

    private static final String SQL_CREATE = """
            BEGIN
                INSERT INTO COMMENTAIRES (date_eval, commentaire, nom_utilisateur, fk_rest)
                VALUES (?, ?, ?, ?)
                RETURNING numero INTO ?;
            END;
            """;

    private static final String SQL_UPDATE =
            """
        UPDATE COMMENTAIRES
        SET date_eval = ?, commentaire = ?, nom_utilisateur = ?, fk_rest
                WHERE numero
                """;

    private static final String SQL_FIND_BY_RESTAURANT =
            """
        SELECT numero, date_eval, commentaire, nom_utilisateur
        FROM COMMENTAIRES
        WHERE fk_rest
                """;

    private static final String SQL_FIND_BY_USER_AND_RESTAURANT =
            """
        SELECT numero, date_eval, commentaire, nom_utilisateur, fk_rest
        FROM COMMENTAIRES
        WHERE nom_utilisateur = ? AND fk_rest
                """;

    public CompleteEvaluationMapper() throws SQLException {
        this.connection = getConnection();
    }

    public CompleteEvaluationMapper(RestaurantMapper rm) throws SQLException {
        this.connection = getConnection();
        this.restaurantMapper = rm;
        this.gradeMapper = new GradeMapper();
    }

    public CompleteEvaluationMapper(RestaurantMapper restaurantMapper, GradeMapper gradeMapper) throws SQLException {
        this.connection = getConnection();
        this.restaurantMapper = restaurantMapper;
        this.gradeMapper = gradeMapper;
    }

    public void setDependencies(RestaurantMapper restaurantMapper, GradeMapper gradeMapper) {
        this.restaurantMapper = restaurantMapper;
        this.gradeMapper = gradeMapper;
    }

    public CompleteEvaluation findById(Integer id) {
        EntityManager em = getEntityManager();
        return em.find(CompleteEvaluation.class, id);
    }

    public List<CompleteEvaluation> findByComment(String comment) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("CompleteEvaluation.findByComment", CompleteEvaluation.class)
                .setParameter("comment", "%" + comment + "%")
                .getResultList();
    }

    public List<CompleteEvaluation> findByUsername(String username) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("CompleteEvaluation.findByUsername", CompleteEvaluation.class)
                .setParameter("username", "%" + username + "%")
                .getResultList();
    }

    @Override
    public List<CompleteEvaluation> findAll() {
        EntityManager em = getEntityManager();
        return em.createQuery(
                "SELECT ce FROM CompleteEvaluation ce",
                CompleteEvaluation.class
        ).getResultList();
    }

    @Override
    public CompleteEvaluation create(CompleteEvaluation completeEvaluation) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(completeEvaluation);
            tx.commit();
            return completeEvaluation;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur create CompleteEvaluation", e);
            return null;
        }
    }

    @Override
    public boolean update(CompleteEvaluation evaluation) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.merge(evaluation);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur update CompleteEvaluation", e);
            return false;
        }
    }

    @Override
    public boolean delete(CompleteEvaluation evaluation) {
        return deleteById(evaluation.getId());
    }

    @Override
    public boolean deleteById(Integer id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            CompleteEvaluation evaluation =
                    em.find(CompleteEvaluation.class, id);

            if (evaluation == null) {
                tx.commit();
                return false;
            }

            em.remove(evaluation);

            tx.commit();
            return true;

        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            logger.error("CompleteEvaluation - Erreur deleteById", ex);
            return false;
        }
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_EVAL.NEXTVAL FROM dual";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM COMMENTAIRES WHERE numero = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM COMMENTAIRES";
    }

    public List<CompleteEvaluation> findByRestaurant(Restaurant restaurant) {
        EntityManager em = getEntityManager();
        return new ArrayList<>(
                em.createNamedQuery(
                        "CompleteEvaluation.findByRestaurant",
                        CompleteEvaluation.class
                )
                        .setParameter("restaurant", restaurant)
                        .getResultList()
        );
    }

    public CompleteEvaluation findByUserAndRest(String username, Integer restaurantId) throws SQLException {
        EntityManager em = getEntityManager();

        return em.createQuery(
                "SELECT c FROM commentaires c" +
                        "WHERE c.nom_utilisateur = :username" +
                        "AND c.fk_rest = :restaurantid",
                        CompleteEvaluation.class
        )
                .setParameter("username", username)
                .setParameter("restaurantId", restaurantId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}



