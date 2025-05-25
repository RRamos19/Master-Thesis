package thesis.model.entities.EmbeddableIds;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ClassTimePK implements Serializable {
    @Embedded
    private ClassUnitPK classUnitPK;

    private UUID id;

    public ClassTimePK() {}

    public ClassTimePK(ClassUnitPK classUnitPK, UUID id) {
        this.classUnitPK = classUnitPK;
        this.id = id;
    }

    public ClassUnitPK getClassUnitPK() {
        return classUnitPK;
    }

    public void setClassUnitPK(ClassUnitPK classUnitPK) {
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
