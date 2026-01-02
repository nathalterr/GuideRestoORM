package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.mapper.RestaurantTypeMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class RestaurantTypeService {
    private final RestaurantTypeMapper typeMapper = new RestaurantTypeMapper();

    public RestaurantTypeService() throws SQLException {
    }

    public List<RestaurantType> getAllTypes() {
        return typeMapper.findAll();
    }

    public RestaurantType addType(String label, String description) {
        RestaurantType type = new RestaurantType(null, label, description);
        return typeMapper.create(type);
    }

    public List<RestaurantType> findByLabel(String label) {
        return typeMapper.findByLabel(label);
    }
}
