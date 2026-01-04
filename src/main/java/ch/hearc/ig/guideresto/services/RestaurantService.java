package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.mapper.RestaurantMapper;
import ch.hearc.ig.guideresto.persistence.mapper.RestaurantTypeMapper;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RestaurantService {
    private final RestaurantMapper restaurantMapper = new RestaurantMapper();
    private static RestaurantService instance;

    private RestaurantService() throws SQLException {
    }

    public static RestaurantService getInstance() throws SQLException {
        if (instance == null) {
            instance = new RestaurantService();
        }
        return instance;
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantMapper.findAll();
    }

    public List<Restaurant> findRestaurantsByName(String name) throws SQLException {
        return restaurantMapper.findByName(name);
    }
    //Celle ci doit retourner qu'un resto car elle doit isoler un seul resto
    public Restaurant findRestaurantsByName(List<Restaurant> restaurants, String name) throws SQLException {
        for (Restaurant r : restaurants) {
            if (r.getName().equalsIgnoreCase(name)) {
                return r;
            }
        }
        return null;
    }

    public List<Restaurant> findRestaurantsByCity(String cityPart) {
        List<Restaurant> all = restaurantMapper.findAll();
        all.removeIf(r -> !r.getAddress().getCity().getCityName().toLowerCase().contains(cityPart.toLowerCase()));
        return all;
    }

    public List<Restaurant> findRestaurantsByType(String typeLabel) {
        RestaurantTypeMapper typeMapper = new RestaurantTypeMapper();
        List<RestaurantType> types = typeMapper.findByName(typeLabel);

        RestaurantType type = types.get(0); // prend le premier élément
        List<Restaurant> all = restaurantMapper.findAll();
        all.removeIf(r -> !r.getType().getId().equals(type.getId())); // filtre les restaurants
        return all;
    }


    public Restaurant addRestaurant(String name, String description, String website,
                                    String street, City city, RestaurantType type) {
        Restaurant restaurant = new Restaurant(null, name, description, website, street, city, type);
        return restaurantMapper.create(restaurant);
    }

    public boolean updateRestaurant(Restaurant restaurant) {
        return restaurantMapper.update(restaurant);
    }

    public boolean updateRestaurantAddress(Restaurant restaurant, String newStreet, City city) throws SQLException {
        return restaurantMapper.updateAddress(restaurant, newStreet, city);
    }

    public boolean deleteRestaurant(Restaurant restaurant) {
        return restaurantMapper.delete(restaurant);
    }

    public boolean updateRestaurantDetails(Restaurant restaurant, String newName, String newDescription,
                                           String newWebsite, RestaurantType newType,
                                           String newStreet, City newCity) {
        if (restaurant == null || newName == null || newStreet == null || newCity == null) return false;

        restaurant.setName(newName);
        restaurant.setDescription(newDescription);
        restaurant.setWebsite(newWebsite);
        if (newType != null) {
            restaurant.setType(newType);
        }
        restaurant.getAddress().setStreet(newStreet);
        restaurant.getAddress().setCity(newCity);

        return restaurantMapper.update(restaurant);
    }
}
