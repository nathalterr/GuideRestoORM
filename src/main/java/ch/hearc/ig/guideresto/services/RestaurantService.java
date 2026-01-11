package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import ch.hearc.ig.guideresto.persistence.mapper.RestaurantMapper;
import ch.hearc.ig.guideresto.persistence.mapper.RestaurantTypeMapper;
import java.sql.SQLException;
import java.util.List;

public class RestaurantService {
    private final RestaurantMapper restaurantMapper = new RestaurantMapper();
    private static RestaurantService instance;

    private RestaurantService() throws SQLException {
    }

    /**
     * Singleton pattern pour obtenir une instance unique de RestaurantService
     * @return l'instance de RestaurantService
     * @throws SQLException en cas d'erreur de base de données
     */
    public static RestaurantService getInstance() throws SQLException {
        if (instance == null) {
            instance = new RestaurantService();
        }
        return instance;
    }

    /**
     * Affiche une liste de tous les restaurants
     * @return liste de tous les restaurants
     */
    public List<Restaurant> getAllRestaurants() {
        return restaurantMapper.findAll();
    }

    /**
     * Trouver des restaurants par leur nom
     * @param name - le nom du restaurant
     * @return la liste des restaurants trouvés
     * @throws SQLException en cas d'erreur de base de données
     */
    public List<Restaurant> findRestaurantsByName(String name) throws SQLException {
        return restaurantMapper.findByName(name);
    }
    //Celle ci doit retourner qu'un resto car elle doit isoler un seul resto
    /**
     * Trouver un restaurant par son nom dans une liste donnée
     * @param restaurants - la liste des restaurants à rechercher
     * @param name - le nom du restaurant
     * @return le restaurant trouvé, ou null s'il n'existe pas
     * @throws SQLException en cas d'erreur de base de données
     */
    public Restaurant findRestaurantsByName(List<Restaurant> restaurants, String name) throws SQLException {
        for (Restaurant r : restaurants) {
            if (r.getName().equalsIgnoreCase(name)) {
                return r;
            }
        }
        return null;
    }

    /**
    * Trouver des restaurants par une partie de nom de ville
    * @param cityPart - partie du nom de la ville
    * @return la liste des restaurants trouvés
     */
    public List<Restaurant> findRestaurantsByCity(String cityPart) {
        List<Restaurant> all = restaurantMapper.findAll();
        all.removeIf(r -> !r.getAddress().getCity().getCityName().toLowerCase().contains(cityPart.toLowerCase()));
        return all;
    }

    /**
     * Trouver des restaurants par type
     * @param label - le type de restaurant
     * @return la liste des restaurants trouvés
     */
    public List<Restaurant> findRestaurantsByType(String label) {
        RestaurantTypeMapper typeMapper = new RestaurantTypeMapper();
        List<RestaurantType> types = typeMapper.findByName(label);

        RestaurantType type = types.get(0); // prend le premier élément
        List<Restaurant> all = restaurantMapper.findAll();
        all.removeIf(r -> !r.getType().getId().equals(type.getId())); // filtre les restaurants
        return all;
    }

    /**
     * Ajouter un nouveau restaurant
     * @param name - le nom du restaurant
     * @param description - la description du restaurant
     * @param website - le site web du restaurant
     * @param street - la rue du restaurant
     * @param city - la ville du restaurant
     * @param restaurantType - le type de restaurant
     * @return le restaurant créé
     */
    public Restaurant addRestaurant(String name, String description, String website,
                                    String street, City city, RestaurantType restaurantType) {

        Restaurant restaurant = new Restaurant(null, name, description, website, street, city, restaurantType);

        JpaUtils.inTransaction(em -> {
            restaurantMapper.create(restaurant, em);  // persiste avec l'EM courant
        });

        return restaurant; // l'objet est déjà managed et a son ID généré
    }

    /**
     * Mettre à jour un restaurant existant
     * @param restaurant - le restaurant à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateRestaurant(Restaurant restaurant) {
        JpaUtils.inTransaction(em -> {
            restaurantMapper.update(restaurant, em);
        });
        return true;
    }

    /**
     * Mettre à jour l'adresse d'un restaurant
     * @param restaurant - le restaurant à mettre à jour
     * @param newStreet - la nouvelle rue
     * @param newCity - la nouvelle ville
     * @return true si la mise à jour a réussi, false sinon
     * @throws SQLException en cas d'erreur de base de données
     */
    public boolean updateRestaurantAddress(Restaurant restaurant, String newStreet, City newCity) {
        try {
            JpaUtils.inTransaction(em -> {
                restaurantMapper.updateAddress(restaurant, newStreet, newCity, em);
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprimer un restaurant
     * @param restaurant - le restaurant à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteRestaurant(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) return false;

        try {
            JpaUtils.inTransaction(em -> {
                restaurantMapper.delete(restaurant, em);
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
