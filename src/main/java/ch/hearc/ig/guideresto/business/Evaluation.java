package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.Date;

/**
 * @author cedric.baudet
 */
@MappedSuperclass
public abstract class Evaluation implements IBusinessObject {

    @Column(name="date_eval")
    private Date visitDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_rest")
    private Restaurant restaurant;

    public Evaluation() {
        this(null, null, null);
    }

    public Evaluation(Integer id, Date visitDate, Restaurant restaurant) {
        this.visitDate = visitDate;
        this.restaurant = restaurant;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

}