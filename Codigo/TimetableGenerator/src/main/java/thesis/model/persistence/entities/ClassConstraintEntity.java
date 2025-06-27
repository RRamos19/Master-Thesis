package thesis.model.persistence.entities;

import jakarta.persistence.*;
import thesis.model.persistence.entities.embeddableids.ClassConstraintPK;

import java.util.UUID;

@Entity
@Table(name = "class_constraint")
public class ClassConstraintEntity {
    @EmbeddedId
    private ClassConstraintPK id;

    @ManyToOne
    @MapsId("classUnitId")
    @JoinColumn(name = "class_id", referencedColumnName = "id", nullable = false)
    private ClassUnitEntity classUnitEntity;

    @ManyToOne
    @JoinColumn(name = "constraint_id", referencedColumnName = "id")
    private ConstraintEntity constraintEntity;

    public ClassConstraintEntity() {}

    public ClassConstraintEntity(UUID classConstraintId, ClassUnitEntity classUnitEntity, ConstraintEntity constraintEntity) {
        this.id = new ClassConstraintPK(classConstraintId, classUnitEntity.getId());
        classUnitEntity.addClassConstraint(this);
        this.classUnitEntity = classUnitEntity;
        constraintEntity.addClassRestriction(this);
        this.constraintEntity = constraintEntity;
    }

    public ClassConstraintPK getId() {
        return id;
    }

    public ClassUnitEntity getClassUnit() {
        return classUnitEntity;
    }

    public void setClassUnit(ClassUnitEntity classUnitEntity) {
        this.classUnitEntity = classUnitEntity;
        this.id.setClassUnitId(classUnitEntity.getId());
    }

    public ConstraintEntity getConstraint() {
        return constraintEntity;
    }

    public void setConstraint(ConstraintEntity constraintEntity) {
        this.constraintEntity = constraintEntity;
    }
}
