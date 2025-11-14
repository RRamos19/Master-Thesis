package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;
import thesis.model.persistence.repository.entities.embeddableIds.ConstraintPK;

import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "timetable_constraint")
public class ConstraintEntity implements Serializable {
    @EmbeddedId
    private ConstraintPK id = new ConstraintPK();

    @ManyToOne(optional = false)
    @JoinColumn(name = "constraint_type_id", referencedColumnName = "id", nullable = false)
    private ConstraintTypeEntity constraintTypeEntity;

    @ManyToOne(optional = false)
    @MapsId("programPK")
    @JoinColumn(name = "program_id")
    private ProgramEntity programEntity;

    @OneToMany(mappedBy = "constraintEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<ClassConstraintEntity> classConstraintEntityList = new HashSet<>();

    private Integer penalty;

    private Boolean required;

    private Integer first_parameter;

    private Integer second_parameter;

    public ConstraintEntity() {}

    public ConstraintEntity(Integer constraintId, ProgramEntity program, ConstraintTypeEntity constraintTypeEntity, Integer first_parameter, Integer second_parameter, Integer penalty, Boolean required) {
        this.id.setConstraintPK(constraintId);
        this.programEntity = program;
        program.addConstraintEntity(this);
        this.constraintTypeEntity = constraintTypeEntity;
        this.first_parameter = first_parameter;
        this.second_parameter = second_parameter;
        this.penalty = penalty;
        this.required = required;
    }

    public ConstraintPK getId() {
        return id;
    }

    public void setId(ConstraintPK id) {
        this.id = id;
    }

    public ProgramEntity getProgramEntity() {
        return programEntity;
    }

    public void setProgramEntity(ProgramEntity programEntity) {
        this.programEntity = programEntity;
    }

    public Integer getFirst_parameter() {
        return first_parameter;
    }

    public void setFirst_parameter(Integer first_parameter) {
        this.first_parameter = first_parameter;
    }

    public Integer getSecond_parameter() {
        return second_parameter;
    }

    public void setSecond_parameter(Integer second_parameter) {
        this.second_parameter = second_parameter;
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

    public void removeClassConstraint(ClassConstraintEntity classConstraintEntity) {
        this.classConstraintEntityList.remove(classConstraintEntity);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstraintEntity)) return false;
        ConstraintEntity that = (ConstraintEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
