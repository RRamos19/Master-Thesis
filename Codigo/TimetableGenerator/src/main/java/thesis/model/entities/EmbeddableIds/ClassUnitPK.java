package thesis.model.entities.EmbeddableIds;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ClassUnitPK implements Serializable {
    @Embedded
    private SubpartPK subpartId;

    @Column(name = "class_id")
    private UUID id;

    public ClassUnitPK() {}

    public ClassUnitPK(SubpartPK subpartId, UUID id) {
        this.subpartId = subpartId;
        this.id = id;
    }

    public SubpartPK getSubpartId() {
        return subpartId;
    }

    public void setSubpartId(SubpartPK subpartId) {
        this.subpartId = subpartId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassUnitPK)) return false;
        ClassUnitPK classUnitPK = (ClassUnitPK) o;
        return Objects.equals(subpartId, classUnitPK.subpartId) && Objects.equals(id, classUnitPK.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subpartId, id);
    }
}
