package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.Set;

/**
 * @author cedric.baudet
 */

 @Entity

 @NamedQuery(
         name = "Restaurant.findByName",
         query = "SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER(:name)"
 )

 @NamedQuery(
         name = "Restaurant.findByDescription",
         query = "SELECT r FROM Restaurant r WHERE r.description LIKE :description"
 )

 @NamedQuery(
         name = "Restaurant.findByWebsite",
         query = "SELECT r FROM Restaurant r WHERE r.website LIKE :website"
 )

 @NamedQuery(
         name = "Restaurant.findByLocalisation",
         query = "SELECT r FROM Restaurant r WHERE r.address.street LIKE :street"
 )
 @NamedQuery(
         name = "Restaurant.findByRestaurantType",
         query = "SELECT r FROM Restaurant r WHERE r.type.label = :label"
 )
 @NamedQuery(
         name = "Restaurant.findByCity",
         query = "SELECT r FROM Restaurant r WHERE r.address.city.cityName = :cityName"
 )
 @NamedQuery(
         name = "Restaurant.updateAddress",
         query = "UPDATE Restaurant r SET r.address.street = :street, r.address.city = :city WHERE r.id = :id"
 )


 @Table(name = "RESTAURANTS")
public class Restaurant implements IBusinessObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="NUMERO", nullable=false)
    private Integer id;
    @Column(name="NOM", nullable=false)
    private String name;
    @Column(name="DESCRIPTION", nullable=false)
    private String description;
    @Column(name="SITE_WEB", nullable=false)
    private String website;
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CompleteEvaluation> completeEvaluations;
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BasicEvaluation> basicEvaluations;
    @Embedded
    private Localisation address; // ATTENTION, resto stocke une rue en String (adresse dans bd) et une fk pour la ville : fk_vill
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_type")
    private RestaurantType type;

    public Restaurant() {
        this(null, null, null, null, null, null);
    }

    public Restaurant(Integer id, String name, String description, String website, String street, City city, RestaurantType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.address = new Localisation(street, city);
        this.type = type;
    }

    public Restaurant(Integer id, String name, String description, String website, Localisation address, RestaurantType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.address = address;
        this.type = type;
    }
    public Restaurant( String name, String description, String website, Localisation address, RestaurantType type) {
        this(null, name, description, website, address, type);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Localisation getAddress() {
        return address;
    }

    public void setAddress(Localisation address) {
        this.address = address;
    }

    public RestaurantType getType() {
        return type;
    }

    public void setType(RestaurantType type) {
        this.type = type;
    }

    public Set<CompleteEvaluation> getCompleteEvaluations() {
        return this.completeEvaluations;
    }
    public Set<BasicEvaluation> getBasicEvaluations() {
        return this.basicEvaluations;
    }

    public void setBasicEvaluations(Set<BasicEvaluation> basicEvaluations) {
        this.basicEvaluations = basicEvaluations;
    }
    public void setCompleteEvaluations(Set<CompleteEvaluation> completeEvaluations) {
        this.completeEvaluations = completeEvaluations;
    }
}