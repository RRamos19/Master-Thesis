package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teacher")
public class TeacherEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 30, nullable = false)
    private String name;

    @OneToMany(mappedBy = "teacherEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<TeacherUnavailabilityEntity> teacherUnavailabilityEntityList = new ArrayList<>();

    @ManyToMany(mappedBy = "teacherClassEntityList", fetch = FetchType.EAGER)
    private final List<ClassUnitEntity> teacherClassList = new ArrayList<>();

    @OneToMany(mappedBy = "teacherEntity", orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<ScheduledLessonTeacherEntity> scheduledLessonTeacherEntityList = new ArrayList<>();

    public TeacherEntity() {}

    public TeacherEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addUnavailability(TeacherUnavailabilityEntity teacherUnav) {
        teacherUnavailabilityEntityList.add(teacherUnav);
        teacherUnav.setTeacher(this);
    }

    public List<TeacherUnavailabilityEntity> getTeacherUnavailabilityEntityList() {
        return teacherUnavailabilityEntityList;
    }

    public List<ClassUnitEntity> getTeacherClassList() {
        return teacherClassList;
    }

    public void addTeacherClass(ClassUnitEntity cls) {
        teacherClassList.add(cls);
    }

    public void addScheduledLessonTeacherEntity(ScheduledLessonTeacherEntity scheduledLessonTeacherEntity) {
        scheduledLessonTeacherEntityList.add(scheduledLessonTeacherEntity);
    }
}
