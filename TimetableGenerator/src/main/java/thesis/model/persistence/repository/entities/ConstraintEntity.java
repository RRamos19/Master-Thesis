package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "timetable_constraint")
public class ConstraintEntity implements Serializable {
    // Serves to distinguish constraints from each other without relying on the classes it is associated to
    @Transient
    private final UUID instanceId = UUID.randomUUID();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "constraint_type_id", referencedColumnName = "id", nullable = false)
    private ConstraintTypeEntity constraintTypeEntity;

    @OneToMany(mappedBy = "constraintEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<ClassConstraintEntity> classConstraintEntityList = new HashSet<>();

    private Integer penalty;

    private Boolean required;

    private Integer first_parameter;

    private Integer second_parameter;

    public ConstraintEntity() {}

    public ConstraintEntity(ConstraintTypeEntity constraintTypeEntity, Integer first_parameter, Integer second_parameter, Integer penalty, Boolean required) {
        this.constraintTypeEntity = constraintTypeEntity;
        this.first_parameter = first_parameter;
        this.second_parameter = second_parameter;
        this.penalty = penalty;
        this.required = required;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public void setConstraintTypeEntity(ConstraintTypeEntity constraintTypeEntity) {
        this.constraintTypeEntity = constraintTypeEntity;
    }

    public Set<ClassConstraintEntity> getClassConstraintEntityList() {
        return classConstraintEntityList;
    }

    public void addClassConstraint(ClassConstraintEntity classConstraintEntity) {
        this.classConstraintEntityList.add(classConstraintEntity);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstraintEntity)) return false;
        ConstraintEntity that = (ConstraintEntity) o;
        return (id != null && Objects.equals(id, that.id)) ||
                (id == null && that.id == null && Objects.equals(instanceId, that.instanceId));
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : instanceId.hashCode();
    }
}
