package thesis.model.entities;

import jakarta.persistence.*;
import thesis.model.entities.EmbeddableIds.TeacherClassPK;

@Entity
@Table(name = "teacher_class")
public class TeacherClass {
    @EmbeddedId
    private TeacherClassPK id;

    @ManyToOne
    @MapsId("teacherPK")
    @JoinColumn(name = "teacher_id", referencedColumnName = "id")
    private Teacher teacher;

    @ManyToOne
    @MapsId("classUnitPK")
    @JoinColumns({
            @JoinColumn(name = "course_id", referencedColumnName = "course_id"),
            @JoinColumn(name = "config_id", referencedColumnName = "config_id"),
            @JoinColumn(name = "subpart_id", referencedColumnName = "subpart_id"),
            @JoinColumn(name = "class_id", referencedColumnName = "class_id")
    })
    private ClassUnit classUnit;

    public TeacherClass() {}

    public TeacherClass(ClassUnit classUnit, Teacher teacher) {
        this.id = new TeacherClassPK(classUnit.getId(), teacher.getId());
        this.classUnit = classUnit;
        this.teacher = teacher;
    }

    public TeacherClassPK getId() {
        return id;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
        this.id.setTeacherPK(teacher.getId());
    }

    public ClassUnit getClassUnit() {
        return classUnit;
    }

    public void setClassUnit(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.id.setClassUnitPK(classUnit.getId());
    }
}
