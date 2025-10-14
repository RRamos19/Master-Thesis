package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "config")
public class ConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "id", nullable = false)
    private CourseEntity courseEntity;

    @OneToMany(mappedBy = "configEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<SubpartEntity> subpartEntitySet = new HashSet<>();

    private String name;

    public ConfigEntity() {}

    public ConfigEntity(CourseEntity courseEntity, String name) {
        courseEntity.addConfig(this);
        this.courseEntity = courseEntity;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConfigEntity)) return false;
        ConfigEntity that = (ConfigEntity) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
