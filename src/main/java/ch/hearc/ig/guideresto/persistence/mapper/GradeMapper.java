package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;

import java.sql.*;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class GradeMapper extends AbstractMapper<Grade> {

    private static final Map<Integer, Grade> identityMap = new HashMap<>();
    private final Connection connection;
    private final EvaluationCriteriaMapper criteriaMapper;
    private final CompleteEvaluationMapper evaluationMapper;

    private static final String SQL_FIND_BY_ID = """
        SELECT numero, note, fk_comm, fk_crit
        FROM NOTES
        WHERE numero = ?
        """;

    private static final String SQL_FIND_ALL = """
        SELECT numero, note, fk_comm, fk_crit
        FROM NOTES
        """;

    private static final String SQL_CREATE = """
        BEGIN
            INSERT INTO NOTES (note, fk_comm, fk_crit)
            VALUES (?, ?, ?)
            RETURNING numero INTO ?;
        END;
        """;

    private static final String SQL_UPDATE = """
        UPDATE NOTES
        SET note = ?, fk_comm = ?, fk_crit = ?
        WHERE numero = ?
        """;

    private static final String SQL_FIND_BY_COMPLETE_EVALUATION = """
        SELECT numero, note, fk_comm, fk_crit
        FROM NOTES
        WHERE fk_comm = ?
        """;

    private static final String SQL_FIND_BY_EVALUATION = """
        SELECT numero, note, fk_crit
        FROM NOTES
        WHERE fk_comm = ?
        """;

    public GradeMapper() throws SQLException {
        this.connection = getConnection();
        this.criteriaMapper = new EvaluationCriteriaMapper();
        this.evaluationMapper = new CompleteEvaluationMapper();
    }

    @Override
    public Grade findById(Integer id) {
        // ✅ Vérifie d’abord le cache
        if (identityMap.containsKey(id)) {
            System.out.println("⚡ Grade " + id + " récupéré depuis l'Identity Map");
            return identityMap.get(id);
        }

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CompleteEvaluation eval = evaluationMapper.findById(rs.getInt("fk_comm"));
                    EvaluationCriteria crit = criteriaMapper.findById(rs.getInt("fk_crit"));
                    Grade grade = new Grade(
                            rs.getInt("numero"),
                            rs.getInt("note"),
                            eval,
                            crit
                    );

                    // ✅ Stocker dans la Map
                    identityMap.put(id, grade);
                    return grade;
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur findById Grade : " + ex.getMessage());
        }
        return null;
    }


    public List<Grade> findByGrade(Integer grade) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("Grade.findByGrade", Grade.class)
                .setParameter("grade", "%" + grade + "%")
                .getResultList();
    }

    @Override
    public List<Grade> findAll() {
        List<Grade> grades = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Integer id = rs.getInt("numero");
                Grade grade = identityMap.get(id);

                if (grade == null) {
                    CompleteEvaluation eval = evaluationMapper.findById(rs.getInt("fk_comm"));
                    EvaluationCriteria crit = criteriaMapper.findById(rs.getInt("fk_crit"));
                    grade = new Grade(id, rs.getInt("note"), eval, crit);
                    identityMap.put(id, grade);
                }
                grades.add(grade);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur findAll Grade : " + ex.getMessage());
        }
        return grades;
    }

    @Override
    public Grade create(Grade grade) {
        try (CallableStatement stmt = connection.prepareCall(SQL_CREATE)) {
            stmt.setInt(1, grade.getGrade());
            stmt.setInt(2, grade.getEvaluation().getId());
            stmt.setInt(3, grade.getCriteria().getId());
            stmt.registerOutParameter(4, Types.INTEGER);

            stmt.executeUpdate();

            Integer generatedId = stmt.getInt(4);
            grade.setId(generatedId);
            identityMap.put(generatedId, grade); // ✅ Ajouter au cache

            if (!connection.getAutoCommit()) connection.commit();
            return grade;

        } catch (SQLException e) {
            System.err.println("Erreur create Grade: " + e.getMessage());
            try { connection.rollback(); } catch (SQLException r) {
                System.err.println("Rollback échoué : " + r.getMessage());
            }
            return null;
        }
    }

    @Override
    public boolean update(Grade grade) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.merge(grade);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur update Grade", e);
            return false;
        }
    }

    @Override
    public boolean delete(Grade grade) {
        return deleteById(grade.getId());
    }

    @Override
    public boolean deleteById(Integer id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Grade ref = em.getReference(Grade.class, id);
            em.remove(ref);

            tx.commit();
            return true;

        } catch (EntityNotFoundException e) {
            if (tx.isActive()) tx.rollback();
            return false;

        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            logger.error("Exception in deleteById", ex);
            return false;
        }
    }


    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_NOTES.NEXTVAL FROM dual";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM NOTES WHERE numero = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM NOTES";
    }

    @Override
    public Set<Grade> findByCompleteEvaluation(CompleteEvaluation completeEvaluation) {
        EntityManager em = getEntityManager();
        return new HashSet<>(
                em.createNamedQuery(
                        "Grade.findByCompleteEvaluation",
                        Grade.class
                )
                        .setParameter("completeEvaluation", completeEvaluation)
                        .getResultList()
        );

    }

    // 🔹 Utilitaires avec cache aussi
    public Set<Grade> findByCompleteEvaluation(CompleteEvaluation eval) {
        Set<Grade> grades = new HashSet<>();

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_BY_COMPLETE_EVALUATION)) {
            stmt.setInt(1, eval.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Integer id = rs.getInt("numero");
                    Grade grade = identityMap.get(id);
                    if (grade == null) {
                        EvaluationCriteria crit = criteriaMapper.findById(rs.getInt("fk_crit"));
                        grade = new Grade(id, rs.getInt("note"), eval, crit);
                        identityMap.put(id, grade);
                    }
                    grades.add(grade);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur findByCompleteEvaluation Grade : " + ex.getMessage());
        }

        return grades;
    }
    public Set<Grade> findByEvaluation(CompleteEvaluation eval) {
        Set<Grade> grades = new LinkedHashSet<>();

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_BY_EVALUATION)) {
            stmt.setInt(1, eval.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                EvaluationCriteriaMapper critMapper = new EvaluationCriteriaMapper();

                while (rs.next()) {
                    Integer gradeId = rs.getInt("numero");

                    // ✅ Vérifie le cache d'identité avant de créer un nouvel objet
                    Grade grade = identityMap.get(gradeId);
                    if (grade == null) {
                        Integer noteValue = rs.getInt("note");
                        Integer critId = rs.getInt("fk_crit");
                        Grade newGrade = new Grade(
                                gradeId,
                                noteValue,
                                eval,
                                critMapper.findById(critId)
                        );

                        identityMap.put(gradeId, newGrade);
                        grade = newGrade;
                    }

                    grades.add(grade);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur findByEvaluation GradeMapper : {}", ex.getMessage());
        }

        return grades;
    }

}

