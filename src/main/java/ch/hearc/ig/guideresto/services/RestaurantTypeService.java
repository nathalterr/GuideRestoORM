package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import ch.hearc.ig.guideresto.persistence.mapper.RestaurantTypeMapper;
import java.util.List;

public class RestaurantTypeService {
    private final RestaurantTypeMapper typeMapper = new RestaurantTypeMapper();
    private static RestaurantTypeService instance;

    private RestaurantTypeService() {
    }

    public static RestaurantTypeService getInstance() {
        if (instance == null) {
            instance = new RestaurantTypeService();
        }
        return instance;
    }

    public List<RestaurantType> getAllTypes() {
        return typeMapper.findAll();
    }

    public RestaurantType addType(String label, String description) {
        RestaurantType type = new RestaurantType(null, label, description);
        JpaUtils.inTransaction(em -> {
            typeMapper.create(type, em);
        });
        return type;
    }

    public List<RestaurantType> findByLabel(String label) {
        return typeMapper.findByName(label);
    }
}
