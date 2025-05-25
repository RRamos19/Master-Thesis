package thesis.model.entities.EmbeddableIds;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClassRestrictionPK implements Serializable {
    @Embedded
    private ClassUnitPK classUnitId;

    @Column(name = "restriction_id")
    private int restrictionId;

    public ClassRestrictionPK() {}

    public ClassRestrictionPK(ClassUnitPK classUnitId, int restrictionId) {
        this.classUnitId = classUnitId;
        this.restrictionId = restrictionId;
    }

    public ClassUnitPK getClassUnitId() {
        return classUnitId;
    }

    public void setClassUnitId(ClassUnitPK classUnitId) {
        this.classUnitId = classUnitId;
    }

    public int getRestrictionId() {
        return restrictionId;
    }

    public void setRestrictionId(int restrictionId) {
        this.restrictionId = restrictionId;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ClassRestrictionPK)) return false;
        ClassRestrictionPK classRestrictionPK = (ClassRestrictionPK) o;
        return classUnitId == classRestrictionPK.classUnitId && restrictionId == classRestrictionPK.restrictionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitId, restrictionId);
    }
}
