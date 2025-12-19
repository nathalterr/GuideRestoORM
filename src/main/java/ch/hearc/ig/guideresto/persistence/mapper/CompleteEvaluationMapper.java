package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
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

    private static final String SQL_UPDATE = """
        UPDATE COMMENTAIRES
        SET date_eval = ?, commentaire = ?, nom_utilisateur = ?, fk_rest = ?
        WHERE numero = ?
        """;

    private static final String SQL_FIND_BY_RESTAURANT = """
        SELECT numero, date_eval, commentaire, nom_utilisateur
        FROM COMMENTAIRES
        WHERE fk_rest = ?
        """;

    private static final String SQL_FIND_BY_USER_AND_RESTAURANT = """
        SELECT numero, date_eval, commentaire, nom_utilisateur, fk_rest
        FROM COMMENTAIRES
        WHERE nom_utilisateur = ? AND fk_rest = ?
        """;

    public CompleteEvaluationMapper() {
        this.connection = getConnection();
    }

    public CompleteEvaluationMapper(RestaurantMapper rm) {
        this.connection = getConnection();
        this.restaurantMapper = rm;
        this.gradeMapper = new GradeMapper();
    }

    public CompleteEvaluationMapper(RestaurantMapper restaurantMapper, GradeMapper gradeMapper) {
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
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setDate(1, new java.sql.Date(evaluation.getVisitDate().getTime()));
            stmt.setString(2, evaluation.getComment());
            stmt.setString(3, evaluation.getUsername());
            stmt.setInt(4, evaluation.getRestaurant().getId());
            stmt.setInt(5, evaluation.getId());

            int rows = stmt.executeUpdate();

            if (!connection.getAutoCommit()) connection.commit();

            if (rows > 0) {
                // ✅ Mise à jour du cache
                identityMap.put(evaluation.getId(), evaluation);
            }

            return rows > 0;
        } catch (SQLException e) {
            logger.error("Erreur update CompleteEvaluation : {}", e.getMessage());
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

    public Set<CompleteEvaluation> findByRestaurant(Restaurant restaurant) {
        Set<CompleteEvaluation> evaluations = new LinkedHashSet<>();

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_BY_RESTAURANT)) {
            stmt.setInt(1, restaurant.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Integer id = rs.getInt("numero");
                    CompleteEvaluation eval = identityMap.get(id);

                    if (eval == null) {
                        eval = new CompleteEvaluation(
                                id,
                                rs.getDate("date_eval"),
                                restaurant,
                                rs.getString("commentaire"),
                                rs.getString("nom_utilisateur")
                        );
                        identityMap.put(id, eval);
                    }

                    if (eval.getGrades().isEmpty()) {
                        eval.getGrades().addAll(gradeMapper.findByEvaluation(eval));
                    }

                    evaluations.add(eval);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur findByRestaurant CompleteEvaluation : {}", ex.getMessage());
        }

        return evaluations;
    }

    public CompleteEvaluation findByUserAndRest(String username, Integer restaurantId) throws SQLException {
        for (CompleteEvaluation eval : identityMap.values()) {
            if (eval.getUsername().equalsIgnoreCase(username)
                    && eval.getRestaurant() != null
                    && Objects.equals(eval.getRestaurant().getId(), restaurantId)) {
                System.out.println("⚡ Évaluation trouvée dans le cache pour " + username);
                return eval;
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_BY_USER_AND_RESTAURANT)) {
            stmt.setString(1, username);
            stmt.setInt(2, restaurantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Restaurant rest = restaurantMapper.findById(restaurantId);
                    CompleteEvaluation eval = new CompleteEvaluation(
                            rs.getInt("numero"),
                            rs.getDate("date_eval"),
                            rest,
                            rs.getString("commentaire"),
                            rs.getString("nom_utilisateur")
                    );

                    identityMap.put(eval.getId(), eval);
                    return eval;
                }
            }
        }
        return null;
    }
}



