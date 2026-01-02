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

    public CompleteEvaluationMapper() {
    }

    @Override
    public CompleteEvaluation create(CompleteEvaluation completeEvaluation) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(completeEvaluation);
                tx.commit();
                return completeEvaluation;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("Erreur create CompleteEvaluation", e);
                return null;
            }
        }
    }

    @Override
    public boolean update(CompleteEvaluation evaluation) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.merge(evaluation);
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("Erreur update CompleteEvaluation", e);
                return false;
            }
        }
    }

    @Override
    public boolean delete(CompleteEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) return false;
        return deleteById(evaluation.getId());
    }

    @Override
    public boolean deleteById(Integer id) {
        if (id == null) return false;

        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                CompleteEvaluation evaluation = em.find(CompleteEvaluation.class, id);
                if (evaluation == null) {
                    tx.commit();
                    return false;
                }
                em.remove(evaluation);
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("CompleteEvaluation - Erreur deleteById", e);
                return false;
            }
        }
    }

    public CompleteEvaluation findById(Integer id) {
        if (id == null) return null;

        try (EntityManager em = getEntityManager()) {
            return em.find(CompleteEvaluation.class, id);
        }
    }

    @Override
    public List<CompleteEvaluation> findAll() {
        try (EntityManager em = getEntityManager()) {
            return em.createQuery(
                    "SELECT ce FROM CompleteEvaluation ce",
                    CompleteEvaluation.class
            ).getResultList();
        }
    }

    public List<CompleteEvaluation> findByComment(String comment) {
        if (comment == null || comment.isEmpty()) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("CompleteEvaluation.findByComment", CompleteEvaluation.class)
                    .setParameter("comment", "%" + comment + "%")
                    .getResultList();
        }
    }

    public List<CompleteEvaluation> findByUsername(String username) {
        if (username == null || username.isEmpty()) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("CompleteEvaluation.findByUsername", CompleteEvaluation.class)
                    .setParameter("username", "%" + username + "%")
                    .getResultList();
        }
    }

    public List<CompleteEvaluation> findByRestaurant(Restaurant restaurant) {
        if (restaurant == null) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("CompleteEvaluation.findByRestaurant", CompleteEvaluation.class)
                    .setParameter("restaurant", restaurant)
                    .getResultList();
        }
    }

    public CompleteEvaluation findByUserAndRest(String username, Integer restaurantId) {
        if (username == null || restaurantId == null) return null;

        try (EntityManager em = getEntityManager()) {
            return em.createQuery(
                            "SELECT ce FROM CompleteEvaluation ce " +
                                    "WHERE ce.username = :username " +
                                    "AND ce.restaurant.id = :restaurantId",
                            CompleteEvaluation.class
                    )
                    .setParameter("username", username)
                    .setParameter("restaurantId", restaurantId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
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
}



