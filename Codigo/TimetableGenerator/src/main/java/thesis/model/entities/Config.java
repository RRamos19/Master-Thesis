package thesis.model.entities;

import jakarta.persistence.*;
import thesis.model.entities.EmbeddableIds.ConfigPK;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "config")
public class Config {
    @EmbeddedId
    private ConfigPK id;

    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "config", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<Subpart> subpartList = new ArrayList<>();

    private String name;

    public Config() {}

    public Config(Course course, String name) {
        this.id = new ConfigPK(course.getId(), UUID.randomUUID());
        this.course = course;
        this.name = name;
    }

    public ConfigPK getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<Subpart> getSubpartList() {
        return subpartList;
    }

    public void addSubpart(Subpart subpart) {
        subpartList.add(subpart);
        subpart.setConfig(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
