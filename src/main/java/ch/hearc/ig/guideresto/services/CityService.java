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
    public void setCityService(CityService cityService) {}
    public List<City> getAllCities() {
        return cityMapper.findAll();
    }

    public City findCityByName(String name) {
        try {
            return cityMapper.findByName(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public City addCity(String name, String zipCode) {
        City city = new City(null, zipCode, name);
        JpaUtils.inTransaction(em -> {
            cityMapper.create(city);});
        return cityMapper.findById(city.getId());
    }

    public City addOrGetCity(String cityName, String postalCode) throws SQLException {
        City city = cityMapper.findByName(cityName);
        if (city == null) {
            city = new City(null, postalCode, cityName);
            City finalCity = city;
            JpaUtils.inTransaction(em -> {
            cityMapper.create(finalCity);
            });
        }
        return cityMapper.findByName(cityName);
    }
}












