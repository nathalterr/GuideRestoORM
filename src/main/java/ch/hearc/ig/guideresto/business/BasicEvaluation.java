package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.Date;

/**
 * @author cedric.baudet
 */
@Entity
@Table(name="LIKES")
public class BasicEvaluation extends Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SeqEval")
    @SequenceGenerator(
            name = "SeqEval",
            sequenceName = "SEQ_EVAL", // MAJUSCULE ! Oracle = case-insensitive MAIS Hibernate ≠
            allocationSize = 1
    )
    @Column
    private int id;
    @Column(name="APPRECIATION")
    private Boolean likeRestaurant;
    @Column(name="ADRESSE_IP")
    private String ipAddress;

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
        return this.id;
    }
    public void setId(Integer id) {
        this.id= id;
    }
}