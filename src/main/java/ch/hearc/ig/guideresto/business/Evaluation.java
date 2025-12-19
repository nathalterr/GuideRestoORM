package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.Date;

/**
 * @author cedric.baudet
 */
@Entity

 @NamedQuery(
         name = "Evaluation.findByVisitDate",
         query = "SELECT e FROM Evaluation e WHERE e.visitDate LIKE :visitDate"
 )

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Evaluation implements IBusinessObject {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SeqEval")
    @SequenceGenerator(
            name = "SeqEval",
            sequenceName = "SEQ_EVAL", // MAJUSCULE ! Oracle = case-insensitive MAIS Hibernate ≠
            allocationSize = 1
    )
    @Column(name="numero")
    private Integer id;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}