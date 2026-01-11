package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import java.sql.*;
import java.util.*;
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
    public EvaluationCriteria create(EvaluationCriteria critere, EntityManager em) {
        em.persist(critere);  // il devient "managed"
        return critere;       // retourne l'entité persistée
    }

    /**
     * Méthode de mise à jour en base de donnée
     * @param critere - l'objet EvaluationCriteria à mettre à jour
     * @return true si la mise à jour a réussi, false en cas d'erreur
     */
    @Override
    public boolean update(EvaluationCriteria critere, EntityManager em) {
        em.merge(critere);  // merge l'entité avec l'EM courant
        return true;
    }
    /**
     * Méthode de suppression en base de donnée
     * @param critere - l'objet EvaluationCriteria à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean delete(EvaluationCriteria critere, EntityManager em) {
        // Récupérer l'entité gérée par l'EM
        RestaurantType managed = em.find(RestaurantType.class, critere.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (managed != null) {
            em.remove(managed);
        }
        return true;
    }
    /**
     * Méthode de suppression en base de donnée
     * @param id - identifiant de l'objet EvaluationCriteria à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean deleteById(Integer id, EntityManager em) {
        EvaluationCriteria entity = em.find(EvaluationCriteria.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (entity != null) {
            em.remove(entity);
        }
        return true;
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
