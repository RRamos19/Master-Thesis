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

    @ManyToMany(mappedBy = "constraintEntityList", fetch = FetchType.EAGER)
    private final List<ClassUnitEntity> classUnitEntityList = new ArrayList<>();

    private Integer penalty;

    private Boolean required;

    private Integer first_parameter;

    private Integer second_parameter;

    public ConstraintEntity() {}

    public ConstraintEntity(ConstraintTypeEntity constraintType, Integer first_parameter, Integer second_parameter, Integer penalty, Boolean required) {
        this.constraintTypeEntity = constraintType;
        constraintType.addConstraintEntity(this);
        this.first_parameter = first_parameter;
        this.second_parameter = second_parameter;
        this.penalty = penalty;
        this.required = required;
    }

    public Integer getId() {
        return id;
    }

    public void setFirstParameter(Integer first_parameter) {
        this.first_parameter = first_parameter;
    }

    public Integer getFirstParameter() {
        return first_parameter;
    }

    public void setSecondParameter(Integer second_parameter) {
        this.second_parameter = second_parameter;
    }

    public Integer getSecondParameter() {
        return second_parameter;
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

    public void addClassRestriction(ClassUnitEntity classUnitEntity) {
        classUnitEntityList.add(classUnitEntity);
    }

    public List<ClassUnitEntity> getClassRestrictionEntityList() {
        return classUnitEntityList;
    }
}
