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

    /**
     * Méthode permettant d'obtenir la valeur de l'évaluation basique
     * @return valeur de l'évaluation basique en question
     */
    public Boolean getLikeRestaurant() {
        return likeRestaurant;
    }

    /**
     * Méthode qui assigne la valeur de l'évaluation
     * @param likeRestaurant - true = like, false=dislike
     */
    public void setLikeRestaurant(Boolean likeRestaurant) {
        this.likeRestaurant = likeRestaurant;
    }
    /**
     * Méthode permettant d'obtenir l'adresse ip de l'utilisateur à la base de l'évaluation basique
     * @return l'adresse ip de l'user
     */
    public String getIpAddress() {
        return ipAddress;
    }
    /**
     * Méthode qui assigne l'adresse ip de l'user à l'évaluation
     * @param ipAddress - adresse IP de l'user
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    /**
     * Méthode permettant d'obtenir l'identifiant de l'évaluation basique
     * @return l'id de l'évaluation basique
     */
    @Override
    public Integer getId() {
        return super.getId();
    }
    /**
     * Méthode qui assigne l'identifiant de l'évaluation basique
     * @param generatedId - l'id de l'évaluation basique
     */
    @Override
    public void setId(Integer generatedId) {super.setId(generatedId);}
}