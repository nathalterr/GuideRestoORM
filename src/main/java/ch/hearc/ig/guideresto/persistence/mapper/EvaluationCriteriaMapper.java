package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.sql.*;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class EvaluationCriteriaMapper extends AbstractMapper<EvaluationCriteria> {

    private static final Map<Integer, EvaluationCriteria> identityMap = new HashMap<>();
    private final Connection connection;
    private static final String SQL_FIND_BY_ID = """
        
            SELECT numero, nom, description
        FROM CRITERES_EVALUATION
        WHERE numero = ?
        """;

    private static final String SQL_FIND_ALL =
            """
        SELECT numero, nom, description
        FROM CRITERES_EVALUATION
        """;

    private static final String SQL_CREATE =
            """
        BEGIN
            INSERT INTO CRITERES_EVALUATION (nom, description)
            VALUES (?, ?)
            RETURNING numero IN
                
                 """;

    private static final String SQL_UPDATE =
            """
        UPDATE CRITERES_EVALUATION
        SET nom = ?, description = ?
        WHERE number = ?;
                 """;

    private static final String SQL_FIND_BY_NAME =
            """
        SELECT numero, nom, description
        FROM CRITERES_EVALUATION
        WHERE no
                 """;

    public EvaluationCriteriaMapper() throws SQLException {
        this.connection = getConnection();
    }

    @Override
    public EvaluationCriteria findById(Integer id) {
        // ✅ Vérifie d’abord le cache
        if (identityMap.containsKey(id)) {
            return identityMap.get(id);
        }

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    EvaluationCriteria crit = new EvaluationCriteria(
                            rs.getInt("numero"),
                            rs.getString("nom"),
                            rs.getString("description")
                    );
                    identityMap.put(id, crit);
                    return crit;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du findById : {}", e.getMessage());
        }
        return null;
    }

    public List<EvaluationCriteria> findByName(String name) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("EvaluationCriteria.findByName", EvaluationCriteria.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();
    }

    public List<EvaluationCriteria> findByDescription(String description) {
        EntityManager em = getEntityManager();
       return em.createNamedQuery("EvaluationCriteria.findByDescription", EvaluationCriteria.class)
            .setParameter("descripton","%" + description + "%")
            .getResultList();
    }

    @Override
    public List<EvaluationCriteria> findAll() {
        List<EvaluationCriteria> criteres = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Integer id = rs.getInt("numero");
                EvaluationCriteria crit = identityMap.get(id);

                if (crit == null) {
                    crit = new EvaluationCriteria(
                            id,
                            rs.getString("nom"),
                            rs.getString("description")
                    );
                    identityMap.put(id, crit);
                }
                criteres.add(crit);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du findAll : {}", e.getMessage());
        }
        return criteres;
    }

    @Override
    public EvaluationCriteria create(EvaluationCriteria critere) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.persist(critere);
        em.getTransaction().commit();
        return critere;
    }


    @Override
    public boolean update(EvaluationCriteria critere) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.merge(critere);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur update evaluationCriteria", e);
            return false;
        }
    }

    @Override
    public boolean delete(EvaluationCriteria critere) {
        return deleteById(critere.getId());
    }

    @Override
    public boolean deleteById(Integer id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            EvaluationCriteria criteria =
                    em.find(EvaluationCriteria.class, id);

            if (criteria == null) {
                tx.commit();
                return false;
            }

            em.remove(criteria);

            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.error("EvaluationCriteria - Erreur lors de deleteById", e);
            return false;
        }
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_CRITERES_EVALUATION.NEXTVAL FROM dual";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM CRITERES_EVALUATION WHERE numero = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM CRITERES_EVALUATION";
    }
}
