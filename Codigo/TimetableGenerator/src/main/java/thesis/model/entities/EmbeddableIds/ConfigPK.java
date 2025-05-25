package thesis.model.entities.EmbeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ConfigPK implements Serializable {
    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "config_id")
    private UUID id;

    public ConfigPK() {}

    public ConfigPK(UUID courseId, UUID id) {
        this.courseId = courseId;
        this.id = id;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConfigPK)) return false;
        ConfigPK configPK = (ConfigPK) o;
        return Objects.equals(courseId, configPK.courseId) && Objects.equals(id, configPK.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, id);
    }
}
