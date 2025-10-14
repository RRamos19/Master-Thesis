package thesis.model.persistence.repository.entities.embeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TeacherClassPK implements Serializable {
    @Column(name = "class_id")
    private Integer classUnitPK;

    @Column(name = "teacher_id")
    private Integer teacherPK;

    public TeacherClassPK() {}

    public TeacherClassPK(Integer classUnitPK, Integer teacherPK) {
        this.classUnitPK = classUnitPK;
        this.teacherPK = teacherPK;
    }

    public Integer getClassUnitPK() {
        return classUnitPK;
    }

    public void setClassUnitPK(Integer classUnitPK) {
        this.classUnitPK = classUnitPK;
    }

    public Integer getTeacherPK() {
        return teacherPK;
    }

    public void setTeacherPK(int teacherPK) {
        this.teacherPK = teacherPK;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TeacherClassPK)) return false;
        TeacherClassPK that = (TeacherClassPK) o;
        return Objects.equals(teacherPK, that.teacherPK) && Objects.equals(classUnitPK, that.classUnitPK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitPK, teacherPK);
    }
}
