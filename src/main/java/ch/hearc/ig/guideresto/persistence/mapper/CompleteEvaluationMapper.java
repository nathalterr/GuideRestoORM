package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.List;

import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class CompleteEvaluationMapper extends AbstractMapper<CompleteEvaluation> {

    public CompleteEvaluationMapper() {
    }

    /**
     * Méthode de persistence en base de donnée
     * @param completeEvaluation à ajouter en base
     * @return l'objet CompleteEvaluation créé, ou null en cas d'erreur
     */
    @Override
    public CompleteEvaluation create(CompleteEvaluation completeEvaluation, EntityManager em) {
        em.persist(completeEvaluation);  // il devient "managed"
        return completeEvaluation;       // retourne l'entité persistée
    }

    /**
     * Méthode de mise à jour en base de donnée
     * @param evaluation - l'objet CompleteEvaluation à mettre à jour
     * @return true si la mise à jour a réussi, false en cas d'erreur
     */
    @Override
    public boolean update(CompleteEvaluation evaluation, EntityManager em) {
        em.merge(evaluation);  // merge l'entité avec l'EM courant
        return true;
    }

    /**
     * Méthode de suppression en base de donnée
     * @param evaluation - l'objet CompleteEvaluation à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean delete(CompleteEvaluation evaluation, EntityManager em) {
        // Récupérer l'entité gérée par l'EM
        CompleteEvaluation managed = em.find(CompleteEvaluation.class, evaluation.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (managed != null) {
            em.remove(managed);
        }
        return true;
    }

    /**
     * Méthode de suppression en base de donnée
     * @param id - identifiant de l'objet CompleteEvaluation à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean deleteById(Integer id, EntityManager em) {
        CompleteEvaluation entity = em.find(CompleteEvaluation.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (entity != null) {
            em.remove(entity);
        }
        return true;
    }

    /**
     * Méthode de recherche d'une évaluation complète en base de données par son identifiant.
     * @param id - identifiant du Grade recherché
     * @return l'évaluation trouvée, ou null s'il n'existe pas
     */
    public CompleteEvaluation findById(Integer id) {
        if (id == null) return null;

        try (EntityManager em = getEntityManager()) {
            return em.find(CompleteEvaluation.class, id, LockModeType.PESSIMISTIC_WRITE);
        }
    }

    /**
     * Méthode de recherche de toutes les évaluations complètes en base de donnée
     * @return la liste des évaluations complètes trouvées
     */
    @Override
    public List<CompleteEvaluation> findAll() {
        try (EntityManager em = getEntityManager()) {
            return em.createQuery(
                    "SELECT ce FROM CompleteEvaluation ce",
                    CompleteEvaluation.class
            ).getResultList();
        }
    }

    /**
     * Méthode de recherche d'une évaluation complète en base de données par commentaire
     * @param comment - commentaire
     * @return la liste des évaluations complètes trouvées
     */
    public List<CompleteEvaluation> findByComment(String comment) {
        if (comment == null || comment.isEmpty()) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("CompleteEvaluation.findByComment", CompleteEvaluation.class)
                    .setParameter("comment", "%" + comment + "%")
                    .getResultList();
        }
    }

    /**
     * Méthode de recherche d'une évaluation complète en base de données par nom d'utilisateur
     * @param username - nom de l'utilisateur
     * @return la liste des évaluations complètes trouvées
     */
    public List<CompleteEvaluation> findByUsername(String username) {
        if (username == null || username.isEmpty()) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("CompleteEvaluation.findByUsername", CompleteEvaluation.class)
                    .setParameter("username", "%" + username + "%")
                    .getResultList();
        }
    }
    /**
     * Méthode de recherche d'une évaluation complète en base de données par restaurant
     * @param restaurant - instance du restaurant
     * @return la liste des évaluations complètes trouvées
     */
    public List<CompleteEvaluation> findByRestaurant(Restaurant restaurant) {
        if (restaurant == null) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("CompleteEvaluation.findByRestaurant", CompleteEvaluation.class)
                    .setParameter("restaurant", restaurant)
                    .getResultList();
        }
    }
    /**
     * Méthode de recherche d'une évaluation complète en base de données par restaurant et nom d'utilisateur
     * @param username - nom de l'utilisateur
     * @param restaurantId - identifiant du restaurant
     * @return la liste des évaluations complètes trouvées
     */
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
}



