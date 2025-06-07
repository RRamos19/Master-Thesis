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

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TeacherUnavailabilityEntity> teacherUnavailabilityEntityList = new ArrayList<>();

    //@OneToMany(mappedBy = "teacher", orphanRemoval = true, fetch = FetchType.EAGER)
    @ManyToMany(mappedBy = "teacherClassList", fetch = FetchType.EAGER)
    private List<ClassUnitEntity> teacherClassList = new ArrayList<>();

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

//    public void addTeacherClass(TeacherClass teacherClass) {
//        teacherClassList.add(teacherClass);
//        teacherClass.setTeacher(this);
//    }
    public void addTeacherClass(ClassUnitEntity cls) {
        teacherClassList.add(cls);
    }
}
