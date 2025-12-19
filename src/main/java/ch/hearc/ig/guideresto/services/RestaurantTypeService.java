package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.mapper.RestaurantTypeMapper;

import java.util.List;
import java.util.Set;

public class RestaurantTypeService {
    private final RestaurantTypeMapper typeMapper;

    public RestaurantTypeService(RestaurantTypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    public List<RestaurantType> getAllTypes() {
        return typeMapper.findAll();
    }

    public RestaurantType addType(String label, String description) {
        RestaurantType type = new RestaurantType(null, label, description);
        return typeMapper.create(type);
    }

    public RestaurantType findByLabel(String label) {
        return typeMapper.findByLabel(label);
    }
}
