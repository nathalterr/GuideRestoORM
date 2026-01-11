package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import ch.hearc.ig.guideresto.persistence.mapper.BasicEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.mapper.CompleteEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.mapper.EvaluationCriteriaMapper;
import ch.hearc.ig.guideresto.persistence.mapper.GradeMapper;
import jakarta.persistence.EntityManager;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class EvaluationService {
    private final BasicEvaluationMapper basicEvaluationMapper = new BasicEvaluationMapper();
    private final CompleteEvaluationMapper completeEvaluationMapper = new CompleteEvaluationMapper();
    private final GradeMapper gradeMapper  = new GradeMapper();
    private final EvaluationCriteriaMapper evalCriteriaMapper = new EvaluationCriteriaMapper();
    private static EvaluationService instance;

    private EvaluationService() {
    }

    /**
     * Singleton pattern
     * @return instance unique de la classe EvaluationService — créée si elle n'existe pas encore
     */
    public static EvaluationService getInstance() {
        if (instance == null) {
            instance = new EvaluationService();
        }
        return instance;
    }

    /**
     * Ajouter une évaluation basique pour un restaurant
     * @param restaurant - le restaurant évalué
     * @param likeRestaurant - true si le restaurant est aimé, false sinon
     * @return l'évaluation basique créée, ou null en cas d'erreur
     */
    public BasicEvaluation addBasicEvaluation(Restaurant restaurant, Boolean likeRestaurant) {
        if (restaurant == null || likeRestaurant == null) return null;
        String ip;
        try {
            ip = Inet4Address.getLocalHost().toString();
        } catch (UnknownHostException e) {
            ip = "Indisponible";
        }
        BasicEvaluation eval = new BasicEvaluation(null, new Date(), restaurant, likeRestaurant, ip);
        JpaUtils.inTransaction(em -> {
            basicEvaluationMapper.create(eval, em);
        });
        return eval;
    }

    /**
     * Ajouter une évaluation complète pour un restaurant
     * @param restaurant - le restaurant évalué
     * @param username - le nom d'utilisateur de l'évaluateur
     * @param comment - le commentaire de l'évaluation
     * @param notes - la carte des critères d'évaluation et leurs notes associées
     * @return CompleteEvaluation ajoutée, ou null en cas d'erreur
     */

    public CompleteEvaluation addCompleteEvaluation(Restaurant restaurant, String username,
                                                    String comment, Map<EvaluationCriteria, Integer> notes) {
        if (restaurant == null || username == null || notes == null) return null;
        CompleteEvaluation eval = new CompleteEvaluation(null, new Date(), restaurant, comment, username);

        for (Map.Entry<EvaluationCriteria, Integer> entry : notes.entrySet()) {
            Grade grade = new Grade(null, entry.getValue(), eval, entry.getKey());
            eval.getGrades().add(grade);
        }
        JpaUtils.inTransaction(em -> {
            completeEvaluationMapper.create(eval, em);
            for (Grade g : eval.getGrades()) {
                gradeMapper.create(g, em);
            }
        });

        return eval;
    }

    /**
     * Récupérer toutes les évaluations basiques d'un restaurant
     * @param restaurant - le restaurant dont on veut les évaluations
     * @return la liste des évaluations basiques
     */
    public List<BasicEvaluation> getBasicEvaluations(Restaurant restaurant) {
        if (restaurant == null) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("BasicEvaluation.findByRestaurant", BasicEvaluation.class)
                    .setParameter("restaurant", restaurant)
                    .getResultList();
        }
    }

    /**
     * Récupérer toutes les évaluations complètes d'un restaurant
     * @param restaurant - le restaurant dont on veut les évaluations
     * @return la liste des évaluations complètes
     */
    public List<CompleteEvaluation> getCompleteEvaluations(Restaurant restaurant) {
        return completeEvaluationMapper.findByRestaurant(restaurant);
    }

    /**
     * Parcourt la liste et compte le nombre d'évaluations basiques positives ou négatives en fonction du paramètre likeRestaurant
     *
     * @param evaluations    La liste des évaluations à parcourir
     * @param like Veut-on le nombre d'évaluations positives ou négatives ?
     * @return Le nombre d'évaluations positives ou négatives trouvées
     */
    public long countLikes(List<BasicEvaluation> evaluations, boolean like) {
        return evaluations.stream()
                .filter(be -> be.getLikeRestaurant() != null && be.getLikeRestaurant() == like)
                .count();
    }

    /**
     * Récupérer tous les critères d'évaluation
     * @return la liste des critères d'évaluation
     */
    public List<EvaluationCriteria> getAllCriteria() {
        return evalCriteriaMapper.findAll();
    }

}