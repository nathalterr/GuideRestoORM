package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.sql.*;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class GradeMapper extends AbstractMapper<Grade> {

    private static final Map<Integer, Grade> identityMap = new HashMap<>();
    private final Connection connection;
    private final EvaluationCriteriaMapper criteriaMapper;
    private final CompleteEvaluationMapper evaluationMapper;

    public GradeMapper() throws SQLException {
        this.connection = getConnection();
        this.criteriaMapper = new EvaluationCriteriaMapper();
        this.evaluationMapper = new CompleteEvaluationMapper();
    }

    public Grade findById(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.createNamedQuery("Grade.findById", Grade.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }



    public List<Grade> findByGrade(Integer grade) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("Grade.findByGrade", Grade.class)
                .setParameter("grade", "%" + grade + "%")
                .getResultList();
    }

    public List<Grade> findAll() {
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("Grade.findAll", Grade.class)
                    .getResultList();
        }
    }


    public Grade create(Grade grade) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(grade);
            tx.commit();
            return grade;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return null;
        } finally {
            em.close();
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

    public Set<Grade> findByEvaluation(CompleteEvaluation eval) {
        try (EntityManager em = getEntityManager()) {
            List<Grade> grades = em.createNamedQuery("Grade.findByEvaluation", Grade.class)
                    .setParameter("evaluation", eval)
                    .getResultList();
            return new LinkedHashSet<>(grades);
        }
    }


}

