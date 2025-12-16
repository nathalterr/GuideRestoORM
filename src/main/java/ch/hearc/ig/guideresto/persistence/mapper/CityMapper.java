package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class CityMapper extends AbstractMapper<City> {

    private static final Logger logger = LoggerFactory.getLogger(CityMapper.class);
    private final Connection connection;
    private final Map<Integer, City> identityMap = new HashMap<>();

    private static final String SQL_FIND_BY_ID = """
        SELECT numero, code_postal, nom_ville
        FROM VILLES
        WHERE numero = ?
        """;

    private static final String SQL_FIND_ALL = """
        SELECT numero, code_postal, nom_ville
        FROM VILLES
        """;

    private static final String SQL_CREATE = """
        BEGIN
            INSERT INTO VILLES (code_postal, nom_ville)
            VALUES (?, ?)
            RETURNING numero INTO ?;
        END;
        """;

    private static final String SQL_UPDATE = """
        UPDATE VILLES
        SET code_postal = ?, nom_ville = ?
        WHERE numero = ?
        """;

    private static final String SQL_FIND_BY_NAME = """
        SELECT numero, code_postal, nom_ville
        FROM VILLES
        WHERE nom_ville = ?
        """;

    private static final String SQL_FIND_BY_ZIP_CODE = """
        SELECT numero, code_postal, nom_ville
        FROM VILLES
        WHERE code_postal = ?
        """;

    private static final String SQL_EXISTS_BY_NAME = """
        SELECT 1
        FROM VILLES
        WHERE nom_ville = ?
        """;

    public CityMapper() {
        this.connection = getConnection();
    }

    @Override
    public City findById(Integer id) {
        // Vérifie le cache d'abord
        if (identityMap.containsKey(id)) {
            return identityMap.get(id);
        }

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    City city = new City(
                            id,
                            rs.getString("code_postal"),
                            rs.getString("nom_ville")
                    );

                    identityMap.put(id, city);
                    return city;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findById City: {}", e.getMessage());
        }

        return null;
    }

    @Override
    public Set<City> findAll() {
        Set<City> cities = new HashSet<>();
        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Integer id = rs.getInt("numero");
                City city = identityMap.get(id);
                if (city == null) {
                    city = new City(
                            id,
                            rs.getString("code_postal"),
                            rs.getString("nom_ville")
                    );
                    identityMap.put(id, city);
                }
                cities.add(city);
            }
        } catch (SQLException e) {
            logger.error("findAll SQLException: {}", e.getMessage());
        }
        return cities;
    }

    @Override
    public City create(City city) {

        try (CallableStatement stmt = connection.prepareCall(SQL_CREATE)) {
            stmt.setString(1, city.getZipCode());
            stmt.setString(2, city.getCityName());
            stmt.registerOutParameter(3, Types.INTEGER);

            stmt.executeUpdate();
            Integer generatedId = stmt.getInt(3);
            city.setId(generatedId);
            identityMap.put(generatedId, city);

            if (!connection.getAutoCommit()) connection.commit();
            return city;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1) { // doublon
                try {
                    return findByName(city.getCityName());
                } catch (SQLException ex) {
                    logger.error("Erreur findByName après doublon: {}", ex.getMessage());
                }
            } else {
                logger.error("Erreur create City: {}", e.getMessage());
            }
            try { connection.rollback(); } catch (SQLException r) {
                logger.error("Rollback failed: {}", r.getMessage());
            }
            return null;
        }
    }

    @Override
    public boolean update(City city) {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, city.getZipCode());
            stmt.setString(2, city.getCityName());
            stmt.setInt(3, city.getId());
            int updated = stmt.executeUpdate();
            if (!connection.getAutoCommit()) connection.commit();
            if (updated > 0) identityMap.put(city.getId(), city);
            return updated > 0;
        } catch (SQLException e) {
            logger.error("update SQLException: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(City city) {
        return deleteById(city.getId());
    }

    @Override
    public boolean deleteById(Integer id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            City entity = em.find(City.class, id);
            if (entity == null) {
                tx.commit();
                return false;
            }

            em.remove(entity);
            tx.commit();
            return true;

        } catch (Exception ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("City - Exception in deleteById", ex);
            return false;
        }
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_VILLES.NEXTVAL FROM dual";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM VILLES WHERE numero = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM VILLES";
    }

    public City findByName(String name) throws SQLException {
        // Vérifie d'abord si la ville est dans le cache
        for (City cachedCity : identityMap.values()) {
            if (cachedCity.getCityName().equalsIgnoreCase(name)) {
                return cachedCity;
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_BY_NAME)) {
            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Integer id = rs.getInt("numero");

                    // Vérifie encore le cache au cas où
                    if (identityMap.containsKey(id)) return identityMap.get(id);

                    City city = new City(
                            id,
                            rs.getString("code_postal"),
                            rs.getString("nom_ville")
                    );

                    identityMap.put(id, city);
                    return city;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByName City: {}", e.getMessage());
            throw e;
        }

        return null;
    }

    public City findByZipCode(String zipCode) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_FIND_BY_ZIP_CODE)) {
            stmt.setString(1, zipCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Integer id = rs.getInt("numero");
                    if (identityMap.containsKey(id)) return identityMap.get(id);
                    City city = new City(
                            id,
                            rs.getString("code_postal"),
                            rs.getString("nom_ville")
                    );
                    identityMap.put(id, city);
                    return city;
                }
            }
        }
        return null;
    }

    public boolean existsByName(String name) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_EXISTS_BY_NAME)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
