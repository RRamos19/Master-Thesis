package thesis.model.persistence.entities.EmbeddableIds;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ClassRestrictionPK implements Serializable {
    @Column(name = "class_id")
    private UUID classUnitId;

    @Column(name = "id")
    private UUID id;

    public ClassRestrictionPK() {}

    public ClassRestrictionPK(UUID id, UUID classUnitId) {
        this.id = id;
        this.classUnitId = classUnitId;
    }

    public UUID getClassUnitId() {
        return classUnitId;
    }

    public void setClassUnitId(UUID classUnitId) {
        this.classUnitId = classUnitId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ClassRestrictionPK)) return false;
        ClassRestrictionPK classRestrictionPK = (ClassRestrictionPK) o;
        return classUnitId == classRestrictionPK.classUnitId && Objects.equals(id, classRestrictionPK.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitId, id);
    }
}
