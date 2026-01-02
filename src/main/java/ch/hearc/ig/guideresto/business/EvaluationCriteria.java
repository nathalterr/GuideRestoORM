package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cedric.baudet
 */

@Entity

@NamedQuery(
        name = "EvaluationCriteria.findByName",
        query = "SELECT ec FROM EvaluationCriteria ec WHERE ec.name LIKE :name"
)
@NamedQuery(
        name = "EvaluationCriteria.findByDescription",
        query = "SELECT ec FROM EvaluationCriteria ec WHERE ec.description LIKE :description"
)
@NamedQuery(
        name = "EvaluationCriteria.findById",
        query = "SELECT e FROM EvaluationCriteria e WHERE e.id = :id"
)
@NamedQuery(
        name = "EvaluationCriteria.findAll",
        query = "SELECT e FROM EvaluationCriteria e"
)

@Table(name="CRITERES_EVALUATION")
public class EvaluationCriteria implements IBusinessObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="NUMERO", nullable=false)
    private Integer id;
    @Column(name="NOM", nullable=false)
    private String name;
    @Column(name="DESCRIPTION", nullable=false)
    private String description;
    @OneToMany(
            mappedBy = "criteria",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Grade> grades = new HashSet<>();

    public EvaluationCriteria() {
        this(null, null);
    }

    public EvaluationCriteria(String name, String description) {
        this(null, name, description);
    }

    public EvaluationCriteria(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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
}