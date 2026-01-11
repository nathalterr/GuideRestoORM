package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import ch.hearc.ig.guideresto.persistence.mapper.CityMapper;

import java.util.List;

public class CityService {
    private final CityMapper cityMapper = new CityMapper();
    private static CityService instance;

    private CityService() {
    }

    /**
     * Singleton pattern
     * @return instance unique de la classe CityService — créée si elle n'existe pas encore
     */
    public static CityService getInstance()  {
        if (instance == null) {
            instance = new CityService();
        }
        return instance;
    }
    /**
     * Récupérer toutes les villes
     * @return liste de toutes les villes
     */
    public List<City> getAllCities() {
        return cityMapper.findAll();
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
            cityMapper.create(city, em); // persist → city.id est rempli
        });
        return city; // retourne la même entité
    }


    /**
     * Ajouter une ville si elle n'existe pas déjà, sinon la récupérer
     * @param cityName - le nom de la ville
     * @param zipCode - le code postal de la ville
     * @return la ville existante ou nouvellement créée
     */
    public City addOrGetCity(String cityName, String zipCode) {
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
