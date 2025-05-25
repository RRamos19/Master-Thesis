package thesis.model.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teacher")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 30, nullable = false)
    private String name;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TeacherUnavailability> teacherUnavailabilityList = new ArrayList<>();

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TeacherClass> teacherClassList = new ArrayList<>();

    public Teacher() {}

    public Teacher(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addUnavailability(TeacherUnavailability teacherUnav) {
        teacherUnavailabilityList.add(teacherUnav);
        teacherUnav.setTeacher(this);
    }

    public void addTeacherClass(TeacherClass teacherClass) {
        teacherClassList.add(teacherClass);
        teacherClass.setTeacher(this);
    }
}
