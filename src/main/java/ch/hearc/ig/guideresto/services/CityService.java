package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import ch.hearc.ig.guideresto.persistence.mapper.CityMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class CityService {
    private final CityMapper cityMapper = new CityMapper();
    private static CityService instance;

    private CityService() throws SQLException {
    }

    public static CityService getInstance() throws SQLException {
        if (instance == null) {
            instance = new CityService();
        }
        return instance;
    }
    public List<City> getAllCities() {
        return cityMapper.findAll();
    }

    /**
     * Trouver une ville par son nom
     * @param cityName - le nom de la ville
     * @return la ville trouvée, ou null si aucune ville ne correspond ou en cas d'erreur
     */
    public City findCityByName(String cityName) {
        try {
            return cityMapper.findByName(cityName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Ajouter une nouvelle ville
     * @param cityName - le nom de la ville
     * @param zipCode - le code postal de la ville
     * @return la ville créée, ou null en cas d'erreur
     */
    public City addCity(String cityName, String zipCode) {
        City city = new City(null, zipCode, cityName);
        JpaUtils.inTransaction(em -> {
            cityMapper.create(city, em);});
        return cityMapper.findById(city.getId());
    }


    /**
     * Ajouter une ville si elle n'existe pas déjà, sinon la récupérer
     * @param cityName - le nom de la ville
     * @param zipCode - le code postal de la ville
     * @return la ville existante ou nouvellement créée
     * @throws SQLException - en cas d'erreur de base de données
     */
    public City addOrGetCity(String cityName, String zipCode) throws SQLException {
        City city = cityMapper.findByName(cityName);
        if (city == null) {
            city = new City(null, zipCode, cityName);
            City finalCity = city;
            JpaUtils.inTransaction(em -> {
            cityMapper.create(finalCity, em);
            });
        }
        return cityMapper.findByName(cityName);
    }
}
