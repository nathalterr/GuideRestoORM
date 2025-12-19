package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.mapper.RestaurantMapper;
import ch.hearc.ig.guideresto.persistence.mapper.RestaurantTypeMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class RestaurantService {
    private final RestaurantMapper restaurantMapper;

    public RestaurantService(RestaurantMapper restaurantMapper) {
        this.restaurantMapper = restaurantMapper;
    }

    public Set<Restaurant> getAllRestaurants() {
        return restaurantMapper.findAll();
    }

    public List<Restaurant> findRestaurantsByName(String name) throws SQLException {
        return restaurantMapper.findByName(name);
    }

    public Set<Restaurant> findRestaurantsByCity(String cityPart) {
        if (cityPart == null || cityPart.isEmpty()) return Set.of();
        Set<Restaurant> all = restaurantMapper.findAll();
        all.removeIf(r -> !r.getAddress().getCity().getCityName().toLowerCase().contains(cityPart.toLowerCase()));
        return all;
    }

    public Set<Restaurant> findRestaurantsByType(String typeLabel, RestaurantTypeMapper typeMapper) {
        RestaurantType type = typeMapper.findByLabel(typeLabel);
        if (type == null) return Set.of();
        Set<Restaurant> all = restaurantMapper.findAll();
        all.removeIf(r -> !r.getType().getId().equals(type.getId()));
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
