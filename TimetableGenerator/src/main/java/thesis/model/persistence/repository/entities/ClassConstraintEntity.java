package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;
import thesis.model.persistence.repository.entities.embeddableIds.ClassConstraintPK;

import java.io.Serializable;

@Entity
@Table(name = "class_constraint")
public class ClassConstraintEntity implements Serializable {
    @EmbeddedId
    private ClassConstraintPK id = new ClassConstraintPK();

    @ManyToOne
    @MapsId("classUnitId")
    @JoinColumn(name = "class_id")
    private ClassUnitEntity classUnitEntity;

    @ManyToOne
    @MapsId("constraintId")
    @JoinColumns({
        @JoinColumn(name = "program_id", referencedColumnName = "program_id"),
        @JoinColumn(name = "constraint_id", referencedColumnName = "constraint_id")
    })
    private ConstraintEntity constraintEntity;

    public ClassConstraintEntity() {}

    public ClassConstraintEntity(ClassUnitEntity classUnitEntity, ConstraintEntity constraintEntity) {
        this.classUnitEntity = classUnitEntity;
        this.constraintEntity = constraintEntity;

        classUnitEntity.addClassConstraint(this);
        constraintEntity.addClassConstraint(this);
    }

    public ClassConstraintPK getId() {
        return id;
    }

    public void setId(ClassConstraintPK id) {
        this.id = id;
    }

    public ClassUnitEntity getClassUnitEntity() {
        return classUnitEntity;
    }

    public void setClassUnitEntity(ClassUnitEntity classUnitEntity) {
        this.classUnitEntity = classUnitEntity;
    }

    public ConstraintEntity getConstraintEntity() {
        return constraintEntity;
    }

    public void setConstraintEntity(ConstraintEntity constraintEntity) {
        this.constraintEntity = constraintEntity;
    }
}
