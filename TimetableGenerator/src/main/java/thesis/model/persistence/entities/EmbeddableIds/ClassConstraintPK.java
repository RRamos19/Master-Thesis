package thesis.model.persistence.entities.embeddableids;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ClassConstraintPK implements Serializable {
    @Column(name = "class_id")
    private UUID classUnitId;

    @Column(name = "id")
    private UUID id;

    public ClassConstraintPK() {}

    public ClassConstraintPK(UUID id, UUID classUnitId) {
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
        if(!(o instanceof ClassConstraintPK)) return false;
        ClassConstraintPK classConstraintPK = (ClassConstraintPK) o;
        return classUnitId == classConstraintPK.classUnitId && Objects.equals(id, classConstraintPK.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitId, id);
    }
}
