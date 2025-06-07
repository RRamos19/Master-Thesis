package thesis.model.persistence.entities.EmbeddableIds;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ClassRestrictionPK implements Serializable {
    @Column(name = "class_id")
    private UUID classUnitId;

    @Column(name = "restriction_id")
    private Integer restrictionId;

    public ClassRestrictionPK() {}

    public ClassRestrictionPK(UUID classUnitId, Integer restrictionId) {
        this.classUnitId = classUnitId;
        this.restrictionId = restrictionId;
    }

    public UUID getClassUnitId() {
        return classUnitId;
    }

    public void setClassUnitId(UUID classUnitId) {
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
        return classUnitId == classRestrictionPK.classUnitId && Objects.equals(restrictionId, classRestrictionPK.restrictionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitId, restrictionId);
    }
}
