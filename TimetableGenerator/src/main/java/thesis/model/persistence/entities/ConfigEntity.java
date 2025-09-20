package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "config")
public class ConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "course_id", referencedColumnName = "id", nullable = false)
    private CourseEntity courseEntity;

    @OneToMany(mappedBy = "configEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<SubpartEntity> subpartEntityList = new ArrayList<>();

    private String name;

    public ConfigEntity() {}

    public ConfigEntity(CourseEntity courseEntity, String name) {
        courseEntity.addConfig(this);
        this.courseEntity = courseEntity;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public CourseEntity getCourse() {
        return courseEntity;
    }

    public void setCourse(CourseEntity courseEntity) {
        this.courseEntity = courseEntity;
    }

    public List<SubpartEntity> getSubpartList() {
        return subpartEntityList;
    }

    public void addSubpart(SubpartEntity subpartEntity) {
        subpartEntityList.add(subpartEntity);
        subpartEntity.setConfig(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
