package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.sql.*;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class EvaluationCriteriaMapper extends AbstractMapper<EvaluationCriteria> {
    public EvaluationCriteriaMapper() {
    }

    @Override
    public EvaluationCriteria create(EvaluationCriteria critere) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(critere);
                tx.commit();
                return critere;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("Erreur create EvaluationCriteria", e);
                return null;
            }
        }
    }

    @Override
    public boolean update(EvaluationCriteria critere) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.merge(critere);
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("Erreur update EvaluationCriteria", e);
                return false;
            }
        }
    }

    @Override
    public boolean delete(EvaluationCriteria critere) {
        if (critere == null || critere.getId() == null) return false;
        return deleteById(critere.getId());
    }

    @Override
    public boolean deleteById(Integer id) {
        if (id == null) return false;

        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                EvaluationCriteria critere = em.find(EvaluationCriteria.class, id);
                if (critere == null) {
                    tx.commit();
                    return false;
                }
                em.remove(critere);
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("Erreur deleteById EvaluationCriteria", e);
                return false;
            }
        }
    }

    public EvaluationCriteria findById(Integer id) {
        if (id == null) return null;

        try (EntityManager em = getEntityManager()) {
            try {
                return em.createNamedQuery("EvaluationCriteria.findById", EvaluationCriteria.class)
                        .setParameter("id", id)
                        .getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        }
    }

    public List<EvaluationCriteria> findAll() {
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("EvaluationCriteria.findAll", EvaluationCriteria.class)
                    .getResultList();
        }
    }

    public List<EvaluationCriteria> findByName(String name) {
        if (name == null || name.isEmpty()) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("EvaluationCriteria.findByName", EvaluationCriteria.class)
                    .setParameter("name", "%" + name + "%")
                    .getResultList();
        }
    }

    public List<EvaluationCriteria> findByDescription(String description) {
        if (description == null || description.isEmpty()) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("EvaluationCriteria.findByDescription", EvaluationCriteria.class)
                    .setParameter("description", "%" + description + "%")
                    .getResultList();
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
