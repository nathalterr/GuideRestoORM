package ch.hearc.ig.guideresto.business;

/**
 * @author cedric.baudet
 */

import jakarta.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity

@NamedQuery(
        name = "CompleteEvaluation.findByComment",
        query = "SELECT ce FROM CompleteEvaluation ce WHERE ce.comment LIKE :comment"
)

@NamedQuery(
        name = "CompleteEvaluation.findByUsername",
        query = "SELECT ce FROM CompleteEvaluation ce WHERE ce.username LIKEE :username"
)

@NamedQuery(
        name = "CompleteEvaluation.findAll",
        query = "SELECT ce FROM CompleteEvaluation ce"
)

@Table(name="COMMENTAIRES")
public class CompleteEvaluation extends Evaluation {


    @Column(name="numero")
    private Integer id;
    @Column(name="COMMENTAIRE")
    private String comment;
    @Column(name="NOM_UTILISATEUR")
    private String username;
    @OneToMany(mappedBy = "evaluation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Grade> grades;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_rest", nullable = false)
    private Restaurant restaurant;

    public CompleteEvaluation() {
        this(null, null, null, null);
    }

    public CompleteEvaluation(Date visitDate, Restaurant restaurant, String comment, String username) {
        this(null, visitDate, restaurant, comment, username);
    }

    public CompleteEvaluation(Integer id, Date visitDate, Restaurant restaurant, String comment, String username) {
        super(id, visitDate, restaurant);
        this.comment = comment;
        this.username = username;
        this.grades = new HashSet();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Grade> getGrades() {
        return grades;
    }

    public void setGrades(Set<Grade> grades) {
        this.grades = grades;
    }

    @Override
    public Integer getId() {
        return super.getId();
    }
    @Override
    public void setId(Integer generatedId) {super.setId(generatedId);}
}