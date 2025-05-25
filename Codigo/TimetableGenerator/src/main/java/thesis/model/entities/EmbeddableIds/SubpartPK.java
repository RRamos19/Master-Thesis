package thesis.model.entities.EmbeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SubpartPK implements Serializable {
    @Embedded
    private ConfigPK configPK;

    @Column(name = "subpart_id")
    private UUID id;

    public SubpartPK() {}

    public SubpartPK(ConfigPK configPK, UUID id) {
        this.configPK = configPK;
        this.id = id;
    }

    public ConfigPK getConfigPK() {
        return configPK;
    }

    public void setConfigPK(ConfigPK configPK) {
        this.configPK = configPK;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SubpartPK)) return false;
        SubpartPK subpartPK = (SubpartPK) o;
        return Objects.equals(configPK, subpartPK.configPK) && Objects.equals(id, subpartPK.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configPK, id);
    }
}
