package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "course")
public class CourseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 10, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "program_id", referencedColumnName = "id", nullable = false)
    private ProgramEntity programEntity;

    @OneToMany(mappedBy = "courseEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<ConfigEntity> configEntitySet = new HashSet<>();

    public CourseEntity() {}

    public CourseEntity(ProgramEntity programEntity, String name) {
        this.programEntity = programEntity;
        programEntity.addCourse(this);
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ProgramEntity getProgramEntity() {
        return programEntity;
    }

    public Set<ConfigEntity> getConfigSet() {
        return configEntitySet;
    }

    public void addConfig(ConfigEntity configEntity) {
        configEntitySet.add(configEntity);
        configEntity.setCourse(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CourseEntity)) return false;
        CourseEntity that = (CourseEntity) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
