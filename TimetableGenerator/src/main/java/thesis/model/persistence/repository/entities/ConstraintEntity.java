package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "timetable_constraint")
public class ConstraintEntity {
    // Serves to distinguish constraints from each other without relying on the classes it is associated to
    @Transient
    private final UUID instanceId = UUID.randomUUID();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "constraint_type_id", referencedColumnName = "id", nullable = false)
    private ConstraintTypeEntity constraintTypeEntity;

    private Integer penalty;

    private Boolean required;

    private Integer first_parameter;

    private Integer second_parameter;

    public ConstraintEntity() {}

    public ConstraintEntity(ConstraintTypeEntity constraintType, Integer first_parameter, Integer second_parameter, Integer penalty, Boolean required) {
        this.constraintTypeEntity = constraintType;
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
