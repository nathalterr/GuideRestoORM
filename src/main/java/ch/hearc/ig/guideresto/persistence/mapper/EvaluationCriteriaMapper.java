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
        WHERE numer
                 """;

    private static final String SQL_FIND_BY_NAME =
            """
        SELECT numero, nom, description
        FROM CRITERES_EVALUATION
        WHERE no
                 """;

    public EvaluationCriteriaMapper() {
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

    public List<EvaluationCriteria> findByName(String name) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("EvaluationCriteria.findByName", EvaluationCriteria.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();
    }

    public List<EvaluationCriteria> findByDescription(String description) {
        EntityManager em = getEntityManager(),
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

        try (CallableStatement stmt = connection.prepareCall(SQL_CREATE)) {
            stmt.setString(1, critere.getName());
            stmt.setString(2, critere.getDescription());
            stmt.registerOutParameter(3, Types.INTEGER);

            stmt.executeUpdate();
            Integer generatedId = stmt.getInt(3);
            critere.setId(generatedId);

            // ✅ Ajout dans le cache
            identityMap.put(generatedId, critere);

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

            return critere;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) { // Doublon
                try {
                    return findByName(critere.getName());
                } catch (SQLException ex) {
                    logger.error("Erreur findByName après doublon: {}", ex.getMessage());
                }
            } else {
                logger.error("Erreur create EvaluationCriteria: {}", e.getMessage());
            }

            try {
                connection.rollback();
            } catch (SQLException r) {
                logger.error("Rollback failed: {}", r.getMessage());
            }

            return null;
        }
    }

    @Override
    public boolean update(EvaluationCriteria critere) {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, critere.getName());
            stmt.setString(2, critere.getDescription());
            stmt.setInt(3, critere.getId());
            int rows = stmt.executeUpdate();

            if (!connection.getAutoCommit()) connection.commit();

            if (rows > 0) {
                // ✅ Met à jour le cache
                identityMap.put(critere.getId(), critere);
            }

            return rows > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour : {}", e.getMessage());
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

    public EvaluationCriteria findByName(String name) throws SQLException {
        // ✅ Vérifie d’abord dans le cache
        for (EvaluationCriteria crit : identityMap.values()) {
            if (crit.getName().equalsIgnoreCase(name)) {
                System.out.println("⚡ Critère '" + name + "' trouvé dans le cache");
                return crit;
            }
        }
        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_BY_NAME)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    EvaluationCriteria crit = new EvaluationCriteria(
                            rs.getInt("numero"),
                            rs.getString("nom"),
                            rs.getString("description")
                    );
                    identityMap.put(crit.getId(), crit);
                    return crit;
                }
            }
        }
        return null;
    }
}
