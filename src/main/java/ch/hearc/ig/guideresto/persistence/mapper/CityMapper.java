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

    private static final String SQL_FIND_BY_NAME = """
        SELECT numero, code_postal, nom_ville
        FROM VILLES
        WHERE nom_ville = ?
        """;


    private static final String SQL_EXISTS_BY_NAME = """
        SELECT 1
        FROM VILLES
        WHERE nom_ville = ?
        """;

    public CityMapper() throws SQLException {
        this.connection = getConnection();
    }

    @Override
    public City findById(Integer id) {
        EntityManager em = getEntityManager();
        return em.find(City.class, id);
    }

    public List<City> findByZipCode(String zipCode) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("City.findByZipCode", City.class)
                .setParameter("zipCode", "%" + zipCode + "%") // mettre les % ici
                .getResultList();
    }

    public List<City> findByCityName(String cityName) {
        EntityManager em = getEntityManager();
        return em.createNamedQuery("City.findByCityName", City.class)
                .setParameter("name", "%" + cityName + "%")
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
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.merge(city);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur update City", e);
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

    public City findByName(String name) {
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("City.findByName", City.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

    public boolean existsByName(String name) {
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("City.existsByName", Boolean.class)
                    .setParameter("name", name)
                    .getSingleResult();
        }
    }

}
