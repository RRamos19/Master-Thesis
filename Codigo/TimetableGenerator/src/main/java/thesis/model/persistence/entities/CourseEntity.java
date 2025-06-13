package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "course")
public class CourseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 10, unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "courseEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<ConfigEntity> configEntityList = new ArrayList<>();

    public CourseEntity() {}

    public CourseEntity(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<ConfigEntity> getConfigList() {
        return configEntityList;
    }

    public void addConfig(ConfigEntity configEntity) {
        configEntityList.add(configEntity);
        configEntity.setCourse(this);
    }
}
