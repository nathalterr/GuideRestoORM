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

    public CityMapper() {
    }

    @Override
    public City create(City city) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(city);
                tx.commit();
                return city;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("Erreur create City", e);
                return null;
            }
        }
    }

    @Override
    public boolean update(City city) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.merge(city);
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("Erreur update City", e);
                return false;
            }
        }
    }

    @Override
    public boolean delete(City city) {
        if (city == null || city.getId() == null) return false;
        return deleteById(city.getId());
    }

    @Override
    public boolean deleteById(Integer id) {
        if (id == null) return false;

        try (EntityManager em = getEntityManager()) {
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
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                logger.error("City - Exception in deleteById", e);
                return false;
            }
        }
    }

    @Override
    public City findById(Integer id) {
        if (id == null) return null;

        try (EntityManager em = getEntityManager()) {
            return em.find(City.class, id);
        }
    }

    @Override
    public List<City> findAll() {
        try (EntityManager em = getEntityManager()) {
            return em.createQuery("SELECT c FROM City c", City.class)
                    .getResultList();
        }
    }

    public List<City> findByZipCode(String zipCode) {
        if (zipCode == null || zipCode.isEmpty()) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("City.findByZipCode", City.class)
                    .setParameter("zipCode", "%" + zipCode + "%")
                    .getResultList();
        }
    }

    public List<City> findByCityName(String cityName) {
        if (cityName == null || cityName.isEmpty()) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("City.findByCityName", City.class)
                    .setParameter("name", "%" + cityName + "%")
                    .getResultList();
        }
    }

    public City findByName(String name) {
        if (name == null || name.isEmpty()) return null;

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("City.findByName", City.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
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

    public boolean existsByName(String name) {
        if (name == null || name.isEmpty()) return false;

        try (EntityManager em = getEntityManager()) {
            Long count = em.createNamedQuery("City.existsByName", Long.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return count != null && count > 0;
        }
    }


}
