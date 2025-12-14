package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "config")
public class ConfigEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "name_id", referencedColumnName = "id", nullable = false)
    private ConfigNameEntity configNameEntity;

    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "id", nullable = false)
    private CourseEntity courseEntity;

    @OneToMany(mappedBy = "configEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<SubpartEntity> subpartEntitySet = new HashSet<>();

    public ConfigEntity() {}

    public ConfigEntity(CourseEntity courseEntity, ConfigNameEntity configNameEntity) {
        courseEntity.addConfig(this);
        this.courseEntity = courseEntity;
        this.configNameEntity = configNameEntity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CourseEntity getCourse() {
        return courseEntity;
    }

    public void setCourse(CourseEntity courseEntity) {
        this.courseEntity = courseEntity;
    }

    public Set<SubpartEntity> getSubpartSet() {
        return subpartEntitySet;
    }

    public void addSubpart(SubpartEntity subpartEntity) {
        subpartEntitySet.add(subpartEntity);
        subpartEntity.setConfig(this);
    }

    public ConfigNameEntity getConfigNameEntity() {
        return configNameEntity;
    }

    public void setConfigNameEntity(ConfigNameEntity configNameEntity) {
        this.configNameEntity = configNameEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConfigEntity)) return false;
        ConfigEntity that = (ConfigEntity) o;
        return Objects.equals(configNameEntity, that.configNameEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configNameEntity);
    }
}
