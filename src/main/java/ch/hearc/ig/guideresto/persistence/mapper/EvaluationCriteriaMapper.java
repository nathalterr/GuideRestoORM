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
    /**
     * Méthode de persistence en base de donnée
     * @param critere à ajouter en base
     * @return l'objet EvaluationCriteria créé, ou null en cas d'erreur
     */
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

                return null;
            }
        }
    }

    /**
     * Méthode de mise à jour en base de donnée
     * @param critere - l'objet EvaluationCriteria à mettre à jour
     * @return true si la mise à jour a réussi, false en cas d'erreur
     */
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

                return false;
            }
        }
    }
    /**
     * Méthode de suppression en base de donnée
     * @param critere - l'objet EvaluationCriteria à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean delete(EvaluationCriteria critere) {
        if (critere == null || critere.getId() == null) return false;
        return deleteById(critere.getId());
    }
    /**
     * Méthode de suppression en base de donnée
     * @param id - identifiant de l'objet EvaluationCriteria à supprimer
     * @return true si la suppression a réussi, false sinon
     */
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

                return false;
            }
        }
    }
    /**
     * Méthode de recherche d'un critère d'évaluation en base de données par son identifiant.
     * @param id - identifiant du EvaluationCriteria recherché
     * @return le critère d'évaluation trouvée, ou null s'il n'existe pas
     */
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

    /**
     * Méthode de recherche de tous les critères d'évaluations en base de données.
     * @return la liste des critères d'évaluations trouvés
     */
    public List<EvaluationCriteria> findAll() {
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("EvaluationCriteria.findAll", EvaluationCriteria.class)
                    .getResultList();
        }
    }
    /**
     * Méthode de recherche d'un critère d'évaluation en base de données par son nom.
     * @param name - nom du critère d'évaluation recherché
     * @return la liste des critères d'évaluations trouvés
     */
    public List<EvaluationCriteria> findByName(String name) {
        if (name == null || name.isEmpty()) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("EvaluationCriteria.findByName", EvaluationCriteria.class)
                    .setParameter("name", "%" + name + "%")
                    .getResultList();
        }
    }
    /**
     * Méthode de recherche d'un critère d'évaluation en base de données par sa description.
     * @param description - description du critère d'évaluation recherché
     * @return la liste des critères d'évaluations trouvés
     */
    public List<EvaluationCriteria> findByDescription(String description) {
        if (description == null || description.isEmpty()) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("EvaluationCriteria.findByDescription", EvaluationCriteria.class)
                    .setParameter("description", "%" + description + "%")
                    .getResultList();
        }
    }
}
