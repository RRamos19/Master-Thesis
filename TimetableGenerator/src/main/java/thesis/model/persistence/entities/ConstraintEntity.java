package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timetable_constraint")
public class ConstraintEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "constraint_type_id", referencedColumnName = "id", nullable = false)
    private ConstraintTypeEntity constraintTypeEntity;

    @OneToMany(mappedBy = "constraintEntity", orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<ClassConstraintEntity> classConstraintEntityList = new ArrayList<>();

    private Integer penalty;

    private Boolean required;

    public ConstraintEntity() {}

    public ConstraintEntity(ConstraintTypeEntity constraintType, Integer penalty, Boolean required) {
        this.constraintTypeEntity = constraintType;
        constraintType.addConstraintEntity(this);
        this.penalty = penalty;
        this.required = required;
    }

    public Integer getId() {
        return id;
    }

    public Integer getPenalty() {
        return penalty;
    }

    public void setPenalty(Integer penalty) {
        this.penalty = penalty;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public ConstraintTypeEntity getConstraintTypeEntity() {
        return constraintTypeEntity;
    }

    public void addClassRestriction(ClassConstraintEntity classConstraintEntity) {
        classConstraintEntityList.add(classConstraintEntity);
        classConstraintEntity.setConstraint(this);
    }

    public List<ClassConstraintEntity> getClassRestrictionEntityList() {
        return classConstraintEntityList;
    }
}
