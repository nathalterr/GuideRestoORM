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

    public GradeMapper() {
    }

    public Grade create(Grade grade) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(grade);
                tx.commit();
                return grade;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("Erreur create Grade", e);
                return null;
            }
        }
    }

    public boolean update(Grade grade) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.merge(grade);
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("Erreur update Grade", e);
                return false;
            }
        }
    }

    @Override
    public boolean delete(Grade grade) {
        return deleteById(grade.getId());
    }

    @Override
    public boolean deleteById(Integer id) {
        try (EntityManager em = getEntityManager()) {
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
    }

    public Grade findById(Integer id) {
        try (EntityManager em = getEntityManager()) {
            try {
                return em.createNamedQuery("Grade.findById", Grade.class)
                        .setParameter("id", id)
                        .getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        }
    }

    public List<Grade> findAll() {
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("Grade.findAll", Grade.class)
                    .getResultList();
        }
    }

    public List<Grade> findByGrade(Integer gradeValue) {
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("Grade.findByGrade", Grade.class)
                    .setParameter("grade", gradeValue)
                    .getResultList();
        }
    }

    public Set<Grade> findByCompleteEvaluation(CompleteEvaluation completeEvaluation) {
        try (EntityManager em = getEntityManager()) {
            List<Grade> grades = em.createNamedQuery(
                            "Grade.findByCompleteEvaluation",
                            Grade.class
                    )
                    .setParameter("completeEvaluation", completeEvaluation)
                    .getResultList();

            return new HashSet<>(grades);
        }
    }

    public Set<Grade> findByEvaluation(CompleteEvaluation eval) {
        try (EntityManager em = getEntityManager()) {
            List<Grade> grades = em.createNamedQuery(
                            "Grade.findByEvaluation",
                            Grade.class
                    )
                    .setParameter("evaluation", eval)
                    .getResultList();

            return new LinkedHashSet<>(grades);
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


}

