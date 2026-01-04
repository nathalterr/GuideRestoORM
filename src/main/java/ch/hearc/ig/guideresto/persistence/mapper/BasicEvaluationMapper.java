package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation> {

    public BasicEvaluationMapper() {
    }

    /**
     * Méthode de persistence en base de donnée
     * @param eval à ajouter en base
     * @return l'objet BasicEvaluation créé, ou null en cas d'erreur
     */
    @Override
    public BasicEvaluation create(BasicEvaluation eval) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(eval);
                tx.commit();
                return eval;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();

                return null;
            }
        }
    }

    /**
     * Méthode de mise à jour en base de donnée
     * @param eval - l'objet BasicEvaluation à mettre à jour
     * @return true si la mise à jour a réussi, false en cas d'erreur
     */

    @Override
    public boolean update(BasicEvaluation eval) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.merge(eval);
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();

                return false;
            }
        }
    }
    /**
     * Méthode de suppression en base de donnée
     * @param eval - l'objet BasicEvaluation à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean delete(BasicEvaluation eval) {
        if (eval == null || eval.getId() == null) return false;
        return deleteById(eval.getId());
    }

    /**
     * Méthode de suppression en base de donnée
     * @param id - identifiant de l'objet BasicEvaluation à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean deleteById(Integer id) {
        if (id == null) return false;

        try (EntityManager em = getEntityManager()) {
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
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                return false;
            }
        }
    }
    /**
     * Méthode de recherche d'une évaluation basique en base de données par son identifiant.
     * @param id - identifiant du BasicEvaluation recherché
     * @return la note trouvée, ou null s'il n'existe pas
     */
    public BasicEvaluation findById(Integer id) {
        if (id == null) return null;
        try (EntityManager em = getEntityManager()) {
            return em.find(BasicEvaluation.class, id);
        }
    }

    /**
     * Méthode de recherche de toutes les évaluations basiques en base de donnée
     * @return la liste des BasicEvaluations trouvées
     */
    @Override
    public List<BasicEvaluation> findAll() {
        try (EntityManager em = getEntityManager()) {
            return em.createQuery("SELECT be FROM BasicEvaluation be", BasicEvaluation.class)
                    .getResultList();
        }
    }

    /**
     * Méthode de recherche d'une évaluation basique en base de données par valeur true ou false
     * @param likeRestaurant - true ou false
     * @return la liste des BasicEvaluations trouvées
     */
    public List<BasicEvaluation> findByLikeRestaurant(Boolean likeRestaurant) {
        if (likeRestaurant == null) return List.of();
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("BasicEvaluation.findByLikeRestaurant", BasicEvaluation.class)
                    .setParameter("likeRestaurant", likeRestaurant)
                    .getResultList();
        }
    }

    /**
     * Méthode de recherche d'une évaluation basique en base de données par adresse ip
     * @param ipAddress - adresse ip de l'user
     * @return la liste des BasicEvaluations trouvées
     */
    public List<BasicEvaluation> findByIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) return List.of();
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("BasicEvaluation.findByIpAddress", BasicEvaluation.class)
                    .setParameter("ipAddress", ipAddress)
                    .getResultList();
        }
    }

    /**
     * Méthode de recherche d'une évaluation basique en base de données par restaurant
     * @param restaurant - restaurant dont on veut les BasicEvaluations
     * @return la liste des BasicEvaluations trouvées
     */
    public List<BasicEvaluation> findByRestaurant(Restaurant restaurant) {
        if (restaurant == null) return List.of();
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("BasicEvaluation.findByRestaurant", BasicEvaluation.class)
                    .setParameter("restaurant", restaurant)
                    .getResultList();
        }
    }

    /**
     * Méthode de recherche d'une évaluation basique en base de données par restaurant et par adresse ip
     * @param ip - adresse ip de l'user
     * @param restaurantId - restaurant dont on veut les BasicEvaluations
     * @return la liste des BasicEvaluations trouvées
     */
    public List<BasicEvaluation> findByIpAndRest(String ip, Integer restaurantId) {
        if (ip == null || ip.isEmpty() || restaurantId == null) return List.of();
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("BasicEvaluation.findByIpAndRestaurant", BasicEvaluation.class)
                    .setParameter("ip", ip)
                    .setParameter("restaurantId", restaurantId)
                    .getResultList();
        }
    }

}

