package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import ch.hearc.ig.guideresto.persistence.mapper.BasicEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.mapper.CompleteEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.mapper.GradeMapper;
import ch.hearc.ig.guideresto.persistence.mapper.RestaurantMapper;
import jakarta.persistence.EntityManager;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EvaluationService {
    private final BasicEvaluationMapper basicEvaluationMapper = new BasicEvaluationMapper();
    private final CompleteEvaluationMapper completeEvaluationMapper = new CompleteEvaluationMapper();
    private final GradeMapper gradeMapper  = new GradeMapper();

    public EvaluationService() throws SQLException {
    }

    public BasicEvaluation addBasicEvaluation(Restaurant restaurant, Boolean like) {
        if (restaurant == null || like == null) return null;
        String ip;
        try {
            ip = Inet4Address.getLocalHost().toString();
        } catch (UnknownHostException e) {
            ip = "Indisponible";
        }
        BasicEvaluation eval = new BasicEvaluation(null, new Date(), restaurant, like, ip);
        basicEvaluationMapper.create(eval);
        restaurant.getEvaluations().add(eval);
        return eval;
    }

    public CompleteEvaluation addCompleteEvaluation(Restaurant restaurant, String username,
                                                    String comment, Map<EvaluationCriteria, Integer> notes) {
        if (restaurant == null || username == null || notes == null) return null;
        CompleteEvaluation eval = new CompleteEvaluation(null, new Date(), restaurant, comment, username);

        for (Map.Entry<EvaluationCriteria, Integer> entry : notes.entrySet()) {
            Grade grade = new Grade(null, entry.getValue(), eval, entry.getKey());
            eval.getGrades().add(grade);
        }

        completeEvaluationMapper.create(eval);
        for (Grade g : eval.getGrades()) {
            gradeMapper.create(g);
        }

        restaurant.getEvaluations().add(eval);
        return eval;
    }

    public List<BasicEvaluation> getBasicEvaluations(Restaurant restaurant) {
        if (restaurant == null) return List.of();

        EntityManager em = JpaUtils.getEntityManager();
        try {
            return em.createNamedQuery("BasicEvaluation.findByRestaurant", BasicEvaluation.class)
                    .setParameter("restaurant", restaurant)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<CompleteEvaluation> getCompleteEvaluations(Restaurant restaurant) {
        return completeEvaluationMapper.findByRestaurant(restaurant);
    }
    public long countLikes(List<BasicEvaluation> evaluations, boolean like) {
        return evaluations.stream()
                .filter(be -> be.getLikeRestaurant() != null && be.getLikeRestaurant() == like)
                .count();
    }


}
