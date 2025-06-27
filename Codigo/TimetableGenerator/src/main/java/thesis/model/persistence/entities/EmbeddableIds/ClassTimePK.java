package thesis.model.persistence.entities.embeddableids;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ClassTimePK implements Serializable {
    @Column(name = "class_id")
    private UUID classUnitPK;

    private UUID id;

    public ClassTimePK() {}

    public ClassTimePK(UUID classUnitPK, UUID id) {
        this.classUnitPK = classUnitPK;
        this.id = id;
    }

    public UUID getClassUnitPK() {
        return classUnitPK;
    }

    public void setClassUnitPK(UUID classUnitPK) {
        this.classUnitPK = classUnitPK;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassTimePK)) return false;
        ClassTimePK that = (ClassTimePK) o;
        return Objects.equals(classUnitPK, that.classUnitPK) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitPK, id);
    }
}
