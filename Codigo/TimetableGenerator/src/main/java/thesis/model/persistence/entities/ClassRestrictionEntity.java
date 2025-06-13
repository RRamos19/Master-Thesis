package thesis.model.persistence.entities;

import jakarta.persistence.*;
import thesis.model.persistence.entities.EmbeddableIds.ClassRestrictionPK;

import java.util.UUID;

@Entity
@Table(name = "class_restriction")
public class ClassRestrictionEntity {
    @EmbeddedId
    private ClassRestrictionPK id;

    @ManyToOne
    @MapsId("classUnitId")
    @JoinColumn(name = "class_id", referencedColumnName = "id", nullable = false)
    private ClassUnitEntity classUnitEntity;

    @ManyToOne
    @JoinColumn(name = "restriction_id", referencedColumnName = "id")
    private RestrictionEntity restrictionEntity;

    private Integer penalty;

    @Column(nullable = false)
    private boolean required;

    public ClassRestrictionEntity() {}

    public ClassRestrictionEntity(UUID classRestrictionId, ClassUnitEntity classUnitEntity, RestrictionEntity restrictionEntity, Integer penalty, boolean required) {
        this.id = new ClassRestrictionPK(classRestrictionId, classUnitEntity.getId());
        classUnitEntity.addClassRestriction(this);
        this.classUnitEntity = classUnitEntity;
        restrictionEntity.addClassRestriction(this);
        this.restrictionEntity = restrictionEntity;
        this.penalty = penalty;
        this.required = required;
    }

    public ClassRestrictionPK getId() {
        return id;
    }

    public ClassUnitEntity getClassUnit() {
        return classUnitEntity;
    }

    public void setClassUnit(ClassUnitEntity classUnitEntity) {
        this.classUnitEntity = classUnitEntity;
        this.id.setClassUnitId(classUnitEntity.getId());
    }

    public RestrictionEntity getRestriction() {
        return restrictionEntity;
    }

    public void setRestriction(RestrictionEntity restrictionEntity) {
        this.restrictionEntity = restrictionEntity;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean getRequired() {
        return required;
    }
}
