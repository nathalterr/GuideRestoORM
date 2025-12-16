package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;


public class RestaurantMapper extends AbstractMapper<Restaurant> {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantMapper.class);
    private final Connection connection;
    private static final Map<Integer, Restaurant> identityMap = new HashMap<>();
    private CityMapper cityMapper;
    public RestaurantTypeMapper typeMapper;

    private static final String SQL_FIND_BY_ID = """
        SELECT numero, nom, description, site_web, adresse, fk_type, fk_vill
        FROM RESTAURANTS
        WHERE numero = ?
        """;

    private static final String SQL_FIND_ALL = """
        SELECT numero, nom, description, site_web, adresse, fk_type, fk_vill
        FROM RESTAURANTS
        """;

    private static final String SQL_INSERT = """
        INSERT INTO RESTAURANTS (numero, nom, description, site_web, adresse, fk_type, fk_vill)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String SQL_UPDATE = """
        UPDATE RESTAURANTS
        SET nom = ?, description = ?, site_web = ?, fk_type = ?
        WHERE numero = ?
        """;

    private static final String SQL_UPDATE_ADDRESS = """
        UPDATE RESTAURANTS
        SET adresse = ?, fk_vill = ?
        WHERE numero = ?
        """;

    private static final String SQL_FIND_BY_CITY = """
        SELECT r.numero, r.nom, r.description, r.site_web, r.adresse, r.fk_type, r.fk_vill
        FROM RESTAURANTS r
        INNER JOIN VILLES v ON r.fk_vill = v.numero
        WHERE v.nom_ville = ?
        """;

    private static final String SQL_FIND_BY_TYPE = """
        SELECT r.numero, r.nom, r.description, r.site_web, r.adresse, r.fk_type, r.fk_vill
        FROM RESTAURANTS r
        INNER JOIN TYPES_GASTRONOMIQUES t ON r.fk_type = t.numero
        WHERE t.libelle = ?
        """;

    private static final String SQL_FIND_BY_NAME = """
        SELECT numero, nom, description, site_web, adresse, fk_type, fk_vill
        FROM RESTAURANTS
        WHERE LOWER(nom) LIKE LOWER(?)
        """;

    public RestaurantMapper() {
        this.connection = getConnection();
    }

    public void setDependenciesEval(CompleteEvaluationMapper completeEvalMapper,
                                    GradeMapper gradeMapper,
                                    BasicEvaluationMapper basicEvalMapper) {
    }

    public void setDependenciesCityType(CityMapper cityMapper, RestaurantTypeMapper typeMapper){
        this.cityMapper = cityMapper;
        this.typeMapper = typeMapper;
    }


    public Restaurant findById(Integer id) {
        // Vérifie le cache d'abord
        if (identityMap.containsKey(id)) {
            return identityMap.get(id);
        }

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Crée City "léger" pour éviter boucle infinie
                    City city = this.cityMapper.findById(rs.getInt("fk_vill"));
                    RestaurantType type = this.typeMapper.findById(rs.getInt("fk_type"));

                    Localisation address = new Localisation(rs.getString("adresse"), city);

                    Restaurant restaurant = new Restaurant(
                            id,
                            rs.getString("nom"),
                            rs.getString("description"),
                            rs.getString("site_web"),
                            address,
                            type
                    );

                    identityMap.put(id, restaurant);

                    return restaurant;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findById Restaurant: {}", e.getMessage());
        }

        return null;
    }

    @Override
    public Set<Restaurant> findAll() {
        identityMap.clear(); // vider le cache pour recharger depuis la DB
        Set<Restaurant> restaurants = new LinkedHashSet<>();

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Integer id = rs.getInt("numero");

                // Création des objets associés
                Integer typeId = rs.getInt("fk_type");
                Integer cityId = rs.getInt("fk_vill");

                RestaurantType type = this.typeMapper.findById(typeId);
                City city = this.cityMapper.findById(cityId);

                if (type == null) {
                    logger.warn("⚠ Restaurant {} ignoré : type {} introuvable", id, typeId);
                    continue;
                }
                if (city == null) {
                    logger.warn("⚠ Restaurant {} ignoré : city {} introuvable", id, cityId);
                    continue;
                }

                // Création de la localisation
                Localisation address = new Localisation(rs.getString("adresse"), city);

                // Création du restaurant sans ID
                Restaurant restaurant = new Restaurant(
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("site_web"),
                        address,
                        type
                );
                // Assigner l'ID
                restaurant.setId(id);

                // Ajout au cache et au set
                identityMap.put(id, restaurant);
                restaurants.add(restaurant);
            }

        } catch (SQLException e) {
            logger.error("Erreur findAll Restaurant: {}", e.getMessage());
        }

        return restaurants;
    }


    @Override
    public Restaurant create(Restaurant restaurant) {
        try {
            // 🔹 Assure que le restaurant a un ID
            if (restaurant.getId() == null) {
                restaurant.setId(getSequenceValue()); // ta méthode qui récupère NEXTVAL
            }

            // 🔹 Vérifie que le type et la ville ont un ID valide
            if (restaurant.getType() == null || restaurant.getType().getId() == null) {
                throw new IllegalStateException("RestaurantType non initialisé ou sans ID");
            }
            if (restaurant.getAddress() == null || restaurant.getAddress().getCity() == null ||
                    restaurant.getAddress().getCity().getId() == null) {
                throw new IllegalStateException("City non initialisée ou sans ID");
            }

            // 🔹 Insert dans la table
            try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT)) {
                stmt.setInt(1, restaurant.getId());
                stmt.setString(2, restaurant.getName());
                stmt.setString(3, restaurant.getDescription());
                stmt.setString(4, restaurant.getWebsite());
                stmt.setString(5, restaurant.getAddress().getStreet());
                stmt.setInt(6, restaurant.getType().getId());
                stmt.setInt(7, restaurant.getAddress().getCity().getId());
                stmt.executeUpdate();
            }

            // 🔹 Commit si nécessaire
            if (!connection.getAutoCommit()) connection.commit();

            // 🔹 Ajout au cache
            identityMap.put(restaurant.getId(), restaurant);

            return restaurant;

        } catch (SQLException e) {
            logger.error("Erreur create Restaurant: {}", e.getMessage());
            try {
                if (!connection.getAutoCommit()) connection.rollback();
            } catch (SQLException ex) {
                logger.error("Create - Rollback failed: {}", ex.getMessage());
            }
            return null;
        }
    }

    @Override
    public boolean update(Restaurant restaurant) {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, restaurant.getName());
            stmt.setString(2, restaurant.getDescription());
            stmt.setString(3, restaurant.getWebsite());
            stmt.setInt(4, restaurant.getType().getId());
            stmt.setInt(5, restaurant.getId());

            updateAddress(restaurant, restaurant.getAddress().getStreet(), restaurant.getAddress().getCity());

            int rows = stmt.executeUpdate();
            if (!connection.getAutoCommit()) connection.commit();

            identityMap.put(restaurant.getId(), restaurant);
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Erreur update Restaurant: {}", e.getMessage());
            try { if (!connection.getAutoCommit()) connection.rollback(); } catch (SQLException ex) { logger.error("Update - Rollback failed: {}", ex.getMessage()); }
            return false;
        }
    }

    @Override
    public boolean delete(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) {
            return false;
        }
        return deleteById(restaurant.getId());
    }

    public boolean deleteById(Integer id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Restaurant rest = em.find(Restaurant.class, id);
            if (rest == null) {
                tx.commit();
                return false;
            }

            em.remove(rest); // Hibernate supprime : CompleteEvaluation -> Grades + BasicEvaluation

            tx.commit();

            removeFromCache(id); // si tu gères un cache à part
            return true;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.error("Restaurant - ErrordDeleteByID", e);
            return false;
        }
    }


    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_RESTAURANTS.NEXTVAL FROM dual";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM RESTAURANTS WHERE numero = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM RESTAURANTS";
    }

    /**
     * Met à jour l'adresse et la ville d'un restaurant
     */
    public boolean updateAddress(Restaurant restaurant, String newStreet, City newCity) throws SQLException {
        restaurant.getAddress().setStreet(newStreet);

        if (newCity != null && newCity != restaurant.getAddress().getCity()) {
            restaurant.getAddress().getCity().getRestaurants().remove(restaurant);
            restaurant.getAddress().setCity(newCity);
            newCity.getRestaurants().add(restaurant);
        }

        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE_ADDRESS)) {
            stmt.setString(1, restaurant.getAddress().getStreet());
            stmt.setInt(2, restaurant.getAddress().getCity().getId());
            stmt.setInt(3, restaurant.getId());
            int rows = stmt.executeUpdate();
            if (!connection.getAutoCommit()) connection.commit();

            identityMap.put(restaurant.getId(), restaurant);
            return rows > 0;
        }
    }

    public void removeFromCache(Integer id) {
        identityMap.remove(id);
    }

    public void clearCache() {
        identityMap.clear();
    }
    /**
     * Retourne tous les restaurants situés dans une ville donnée
     */
    public Set<Restaurant> findByCity(String cityName) throws SQLException {
        Set<Restaurant> restaurants = new HashSet<>();

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_BY_CITY)) {
            stmt.setString(1, cityName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Integer id = rs.getInt("numero");
                    Restaurant restaurant = identityMap.get(id);

                    if (restaurant == null) {
                        RestaurantType type = new RestaurantTypeMapper().findById(rs.getInt("fk_type"));
                        City city = new CityMapper().findById(rs.getInt("fk_vill"));
                        restaurant = new Restaurant(
                                id,
                                rs.getString("nom"),
                                rs.getString("description"),
                                rs.getString("site_web"),
                                rs.getString("adresse"),
                                city,
                                type
                        );
                        identityMap.put(id, restaurant);
                    }

                    restaurants.add(restaurant);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByCity Restaurant: {}", e.getMessage());
            throw e;
        }

        return restaurants;
    }

    /**
     * Retourne tous les restaurants d'un type donné
     */
    public Set<Restaurant> findByRestaurantType(String typeLabel) throws SQLException {
        Set<Restaurant> restaurants = new HashSet<>();

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_BY_TYPE)) {
            stmt.setString(1, typeLabel);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Integer id = rs.getInt("numero");
                    Restaurant restaurant = identityMap.get(id);

                    if (restaurant == null) {
                        RestaurantType type = new RestaurantTypeMapper().findById(rs.getInt("fk_type"));
                        City city = new CityMapper().findById(rs.getInt("fk_vill"));
                        restaurant = new Restaurant(
                                id,
                                rs.getString("nom"),
                                rs.getString("description"),
                                rs.getString("site_web"),
                                rs.getString("adresse"),
                                city,
                                type
                        );
                        identityMap.put(id, restaurant);
                    }

                    restaurants.add(restaurant);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByRestaurantType Restaurant: {}", e.getMessage());
            throw e;
        }

        return restaurants;
    }

    public List<Restaurant> findByName(String name) {
        EntityManager em = getEntityManager();
        //Retour d'une liste de restaurant avec le nom en paramètre
        return  em.createNamedQuery("Restaurant.findByName", Restaurant.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();
    }


}

