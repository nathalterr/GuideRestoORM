package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cedric.baudet
 */

@Entity

@NamedQuery(
        name = "RestaurantType.findByLabel",
        query = "SELECT rt FROM RestaurantType rt WHERE rt.label LIKE :label"
)

@NamedQuery(
        name = "RestaurantType.findByDescription",
        query = "SELECT rt FROM RestaurantType rt WHERE rt.description LIKE :description"
)
@NamedQuery(
        name = "RestaurantType.findById",
        query = "SELECT rt FROM RestaurantType rt WHERE rt.id = :id"
)
@NamedQuery(
        name = "RestaurantType.findAll",
        query = "SELECT rt FROM RestaurantType rt ORDER BY rt.label"
)
@NamedQuery(
        name = "RestaurantType.existsByName",
        query = "SELECT COUNT(rt) FROM RestaurantType rt WHERE rt.label = :label"
)
@Table(name="TYPES_GASTRONOMIQUES")
public class RestaurantType implements IBusinessObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="NUMERO", nullable=false)
    private Integer id;
    @Column (name="LIBELLE", nullable=false)
    private String label;
    @Column (name="DESCRIPTION", nullable=false)
    private String description;
    @OneToMany(mappedBy = "type", fetch = FetchType.LAZY)
    private Set<Restaurant> restaurants;

    public RestaurantType() {
        this(null, null);
    }

    public RestaurantType(String label, String description) {
        this(null, label, description);
    }

    public RestaurantType(Integer id, String label, String description) {
        this.restaurants = new HashSet();
        this.id = id;
        this.label = label;
        this.description = description;
    }

    @Override
    public String toString() {
        return label;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

}