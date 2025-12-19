package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.mapper.BasicEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.mapper.CompleteEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.mapper.GradeMapper;
import ch.hearc.ig.guideresto.persistence.mapper.RestaurantMapper;

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

    public Integer countLikes(Restaurant restaurant, boolean like) {
        if (restaurant == null || restaurant.getEvaluations() == null) return 0;
        int count = 0;
        for (Evaluation eval : restaurant.getEvaluations()) {
            if (eval instanceof BasicEvaluation && ((BasicEvaluation) eval).getLikeRestaurant() == like) {
                count++;
            }
        }
        return count;
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

    public Set<BasicEvaluation> getBasicEvaluations(Restaurant restaurant) {
        if (restaurant == null) return Set.of();
        Set<BasicEvaluation> basicEvals = basicEvaluationMapper.findByRestaurant(restaurant);
        restaurant.getEvaluations().removeIf(e -> e instanceof BasicEvaluation);
        restaurant.getEvaluations().addAll(basicEvals);
        return basicEvals;
    }

    public List<CompleteEvaluation> getCompleteEvaluations(Restaurant restaurant) {
        List<CompleteEvaluation> completeEvalsFromDB = completeEvaluationMapper.findByRestaurant(restaurant);
        restaurant.getEvaluations().removeIf(e -> e instanceof CompleteEvaluation);
        restaurant.getEvaluations().addAll(completeEvalsFromDB);
        return completeEvalsFromDB;
    }
}
