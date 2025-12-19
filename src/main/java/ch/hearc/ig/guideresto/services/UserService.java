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
    public List<City> getAllCities() {
        return cityMapper.findAll();
    }

    public City findCityByName(String name) {
        try {
            return cityMapper.findByName(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public City addCity(String name, String zipCode) {
        City city = new City(null, zipCode, name);
        return cityMapper.create(city);
    }
    // Récupère tous les types
    public List<RestaurantType> getAllTypes() {
        return typeMapper.findAll();
    }

    // Crée un nouveau type (optionnel)
    public RestaurantType addType(String label, String description) {
        RestaurantType type = new RestaurantType(null, label, description);
        return typeMapper.create(type);
    }

    // Recherche par libellé
    public List<RestaurantType> findByLabel(String label) {
        return typeMapper.findByLabel(label);
    }
    public List<Restaurant> getAllRestaurants() {
        return restaurantMapper.findAll();
    }

    public List<Restaurant> findRestaurantsByName(String name) throws SQLException{
        return restaurantMapper.findByName(name);
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

        public Restaurant addRestaurant(String name, String description, String website,
                                    String street, City city, RestaurantType type) throws SQLException {
        Restaurant restaurant = new Restaurant(null, name, description, website, street, city, type);

        // Gestion transactionnelle simple
        Connection conn = getConnection();
        boolean autoCommit = true;
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            restaurantMapper.create(restaurant);

            conn.commit();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return null;
        } finally {
            try { conn.setAutoCommit(autoCommit); } catch (SQLException ignored) {}
        }
        return restaurant;
    }

    public boolean updateRestaurant(Restaurant restaurant) {
        return restaurantMapper.update(restaurant);
    }

    public boolean updateRestaurantAddress(Restaurant restaurant, String newStreet, City city) throws SQLException{
        return restaurantMapper.updateAddress(restaurant, newStreet, city);
    }

    public boolean deleteRestaurantService(Restaurant restaurant) {
        return restaurantMapper.delete(restaurant);
    }


    public Integer countLikes(Restaurant restaurant, boolean like) {
        if (restaurant == null || restaurant.getEvaluations() == null) return 0;

        Integer count = 0;
        for (Evaluation currentEval : restaurant.getEvaluations()) {
            if (currentEval instanceof BasicEvaluation) {
                Boolean b = ((BasicEvaluation) currentEval).getLikeRestaurant();
                if (b != null && b == like) {
                    count++;
                }
            }
        }
        return count;
    }


    public Set<Evaluation> getEvaluations(Restaurant restaurant) {
        return restaurant != null ? restaurant.getEvaluations() : Set.of();
    }
    public BasicEvaluation addBasicEvaluation(Restaurant restaurant, Boolean like) throws SQLException {
        if (restaurant == null || like == null) return null;

        String ipAddress;
        try {
            ipAddress = Inet4Address.getLocalHost().toString();
        } catch (UnknownHostException ex) {
            ipAddress = "Indisponible";
            throw new RuntimeException(ex);
        }

        BasicEvaluation eval = new BasicEvaluation(null, new Date(), restaurant, like, ipAddress);

        // Persistance
        BasicEvaluationMapper BEM = new BasicEvaluationMapper();
        BEM.create(eval);

        // Ajout à l'objet restaurant
        restaurant.getEvaluations().add(eval);

        return eval;
    }

    public CompleteEvaluation addCompleteEvaluation(Restaurant restaurant, String username, String comment, Map<EvaluationCriteria, Integer> notes) throws SQLException {
        if (restaurant == null || username == null || notes == null) return null;

        CompleteEvaluation eval = new CompleteEvaluation(null, new Date(), restaurant, comment, username);

        for (Map.Entry<EvaluationCriteria, Integer> entry : notes.entrySet()) {
            Grade grade = new Grade(null, entry.getValue(), eval, entry.getKey());
            eval.getGrades().add(grade);
        }

        // Persistance
        CompleteEvaluationMapper evalMapper = new CompleteEvaluationMapper();
        evalMapper.create(eval);

        GradeMapper gradeMapper = new GradeMapper();
        for (Grade g : eval.getGrades()) {
            gradeMapper.create(g);
        }

        // Ajouter à l'objet restaurant
        restaurant.getEvaluations().add(eval);

        return eval;
    }
    public boolean updateRestaurantDetails(Restaurant restaurant, String newName, String newDescription,
                                           String newWebsite, RestaurantType newType,
                                           String newStreet, City newCity) {
        if (restaurant == null || newName == null || newStreet == null || newCity == null) return false;

        // Mise à jour de l'objet
        restaurant.setName(newName);
        restaurant.setDescription(newDescription);
        restaurant.setWebsite(newWebsite);
        if (newType != null) {
            restaurant.setType(newType);
        }
        restaurant.getAddress().setStreet(newStreet);
        restaurant.getAddress().setCity(newCity);

        // Persistance
        return restaurantMapper.update(restaurant);
    }

    public City addOrGetCity(String cityName, String postalCode) throws SQLException{
        City city = cityMapper.findByName(cityName);
        if (city == null) {
            city = new City(null, postalCode, cityName);
            cityMapper.create(city);
        }
        return city;
    }
    public Set<BasicEvaluation> getBasicEvaluations(Restaurant restaurant) throws SQLException {
        if (restaurant == null) return Set.of();

        // Suppose que BasicEvaluationMapper a une méthode findByRestaurant
        Set<BasicEvaluation> basicEvals = new BasicEvaluationMapper().findByRestaurant(restaurant);

        // Ajoute au restaurant pour que countLikes fonctionne
        restaurant.getEvaluations().removeIf(e -> e instanceof BasicEvaluation);
        restaurant.getEvaluations().addAll(basicEvals);

        return basicEvals;
    }
    public List<CompleteEvaluation> getCompleteEvaluations(Restaurant restaurant) {
        // 🔹 Récupère depuis le mapper
        List<CompleteEvaluation> completeEvalsFromDB = completeEvaluationMapper.findByRestaurant(restaurant);

        // 🔹 Ajoute au restaurant pour que la méthode fonctionne ensuite
        restaurant.getEvaluations().removeIf(e -> e instanceof CompleteEvaluation);
        restaurant.getEvaluations().addAll(completeEvalsFromDB);

        return completeEvalsFromDB;
    }


}
