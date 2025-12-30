package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.Date;

/**
 * @author cedric.baudet
 */
@Entity

@NamedQuery(
        name = "BasicEvaluation.findByLikeRestaurant",
        query = "SELECT be FROM BasicEvaluation be WHERE be.likeRestaurant = :likeRestaurant"
)

@NamedQuery(
        name = "BasicEvaluation.findByIpAddress",
        query = "SELECT be FROM BasicEvaluation be WHERE be.ipAddress LIKE :ipAddress"
)
@NamedQuery(
        name = "BasicEvaluation.findByRestaurant",
        query = "SELECT be FROM BasicEvaluation be WHERE be.restaurant = :restaurant"
)
@NamedQuery(
        name = "BasicEvaluation.findByIpAndRestaurant",
        query = "SELECT l FROM BasicEvaluation l " +
                "WHERE l.ipAddress = :ip " +
                "AND l.restaurant.id = :restaurantId"
)
@Table(name="LIKES")
public class BasicEvaluation extends Evaluation {
    @Column
    private int id;
    @Column(name="APPRECIATION")
    private Boolean likeRestaurant;
    @Column(name="ADRESSE_IP")
    private String ipAddress;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_rest", nullable = false)
    private Restaurant restaurant;

    public BasicEvaluation() {
        this(null, null, null, null);
    }

    public BasicEvaluation(Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        this(null, visitDate, restaurant, likeRestaurant, ipAddress);
    }

    public BasicEvaluation(Integer id, Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        super(id, visitDate, restaurant);
        this.likeRestaurant = likeRestaurant;
        this.ipAddress = ipAddress;
    }

    public Boolean getLikeRestaurant() {
        return likeRestaurant;
    }

    public void setLikeRestaurant(Boolean likeRestaurant) {
        this.likeRestaurant = likeRestaurant;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public Integer getId() {
        return super.getId();
    }
    @Override
    public void setId(Integer generatedId) {super.setId(generatedId);}
}