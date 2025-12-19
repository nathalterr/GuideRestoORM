package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

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

    public City findById(Integer id) {
        EntityManager em = getEntityManager();
        return em.find(City.class, id);
    }

    public List<City> findByZipCode(String zipCode) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("City.findByZipCode", City.class)
                .setParameter("zipCode" + "%" + zipCode + "%")
                .getResultList();
    }

    public List<City> findByCityName(String cityName) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("City.findByCityName", City.class)
                .setParameter("cityName", "%" + cityName + "%")
                .getResultList();
    }

    @Override
    public List<City> findAll() {
        EntityManager em = getEntityManager();
        return em.createQuery(
                "SELECT c FROM City c",
                City.class
        ).getResultList();
    }

    @Override
    public City create(City city) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try{
            tx.begin();
            em.persist(city);
            tx.commit();
            return city;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur create City", e);
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
