package thesis.model.entities;

import jakarta.persistence.*;
import thesis.model.entities.EmbeddableIds.ClassUnitPK;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "class_unit")
public class ClassUnit {
    @EmbeddedId
    private ClassUnitPK id;

    @ManyToOne
    @MapsId("subpartPK")
    @JoinColumns({
            @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false),
            @JoinColumn(name = "config_id", referencedColumnName = "config_id", nullable = false),
            @JoinColumn(name = "subpart_id", referencedColumnName = "subpart_id", nullable = false)
    })
    private Subpart subpart;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "parent_class_course_id", referencedColumnName = "course_id"),
            @JoinColumn(name = "parent_class_config_id", referencedColumnName = "config_id"),
            @JoinColumn(name = "parent_class_subpart_id", referencedColumnName = "subpart_id"),
            @JoinColumn(name = "parent_class_id", referencedColumnName = "class_id")
    })
    private ClassUnit parentClass;

    @Column(length = 10)
    private String name;

    @OneToMany(mappedBy = "classUnit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ScheduledLesson> scheduledLessonList = new ArrayList<>();

    @OneToMany(mappedBy = "classUnit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TeacherClass> teacherClassList = new ArrayList<>();

    @OneToMany(mappedBy = "classUnit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClassTime> classTimeList = new ArrayList<>();

    public ClassUnit() {}

    public ClassUnit(String name, Subpart subpart) {
        this.id = new ClassUnitPK(subpart.getId(), UUID.randomUUID());
        this.name = name;
        this.subpart = subpart;
    }

    public ClassUnitPK getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addScheduledLesson(ScheduledLesson scheduledLesson) {
        scheduledLessonList.add(scheduledLesson);
        scheduledLesson.setClassUnit(this);
    }

    public void addTeacherClass(TeacherClass teacherClass) {
        teacherClassList.add(teacherClass);
        teacherClass.setClassUnit(this);
    }

    public void addClassTime(ClassTime classTime) {
        classTimeList.add(classTime);
        classTime.setClassUnit(this);
    }
}
