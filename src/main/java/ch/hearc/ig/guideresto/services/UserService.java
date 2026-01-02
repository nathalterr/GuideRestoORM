package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.mapper.*;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;

public class UserService {
    private Connection connection;
    private final CityMapper cityMapper;
    private final RestaurantTypeMapper typeMapper;
    private final GradeMapper gradeMapper;
    private final RestaurantMapper restaurantMapper;
    private final EvaluationCriteriaMapper evaluationCriteriaMapper;
    private final BasicEvaluationMapper basicEvaluation;
    private final CompleteEvaluationMapper completeEvaluationMapper;

    public UserService() throws SQLException {
        MapperFactory mapperFactory = new MapperFactory();
        cityMapper = mapperFactory.getCityMapper();
        typeMapper = mapperFactory.getTypeMapper();
        gradeMapper = mapperFactory.getGradeMapper();
        restaurantMapper = mapperFactory.getRestaurantMapper();
        evaluationCriteriaMapper = mapperFactory.getCriteriaMapper();
        basicEvaluation = mapperFactory.getBasicEvalMapper();
        completeEvaluationMapper = mapperFactory.getCompleteEvalMapper();
    }

    public List<Restaurant> findRestaurantsByCity(String cityPart) {
        List<Restaurant> all = new ArrayList<>();
        if (cityPart == null || cityPart.isEmpty()) return all;

        all = restaurantMapper.findAll(); all.removeIf(r -> !r.getAddress().getCity().getCityName().toLowerCase() .contains(cityPart.toLowerCase()));
        return all; }

    public List<Restaurant> findRestaurantsByType(String typeLabel) {
        List<RestaurantType> types = typeMapper.findByLabel(typeLabel);
        if (types.isEmpty()) return Collections.emptyList(); // pas de type trouvé

        RestaurantType type = types.get(0); // prend le premier élément A CHANGER CAR PAS OUF
        List<Restaurant> all = restaurantMapper.findAll();
        all.removeIf(r -> !r.getType().getId().equals(type.getId()));
        return all;
    }
}
