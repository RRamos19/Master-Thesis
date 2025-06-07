package thesis.model.persistence.entities;

import jakarta.persistence.*;
import thesis.model.persistence.entities.EmbeddableIds.ClassRestrictionPK;

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
    @MapsId("restrictionId")
    @JoinColumn(name = "restriction_id", referencedColumnName = "id")
    private RestrictionEntity restrictionEntity;

    private Integer penalty;

    @Column(nullable = false)
    private boolean required;

    public ClassRestrictionEntity() {}

    public ClassRestrictionEntity(ClassUnitEntity classUnitEntity, RestrictionEntity restrictionEntity, Integer penalty, boolean required) {
        this.id = new ClassRestrictionPK(classUnitEntity.getId(), restrictionEntity.getId());
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

    public void setId(ClassRestrictionPK id) {
        this.id = id;
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
        this.id.setRestrictionId(restrictionEntity.getId());
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
