package thesis.model.entities.EmbeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TeacherClassPK implements Serializable {
    @Embedded
    private ClassUnitPK classUnitPK;

    @Column(name = "teacher_id")
    private int teacherPK;

    public TeacherClassPK() {}

    public TeacherClassPK(ClassUnitPK classUnitPK, int teacherPK) {
        this.classUnitPK = classUnitPK;
        this.teacherPK = teacherPK;
    }

    public ClassUnitPK getClassUnitPK() {
        return classUnitPK;
    }

    public void setClassUnitPK(ClassUnitPK classUnitPK) {
        this.classUnitPK = classUnitPK;
    }

    public int getTeacherPK() {
        return teacherPK;
    }

    public void setTeacherPK(int teacherPK) {
        this.teacherPK = teacherPK;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TeacherClassPK)) return false;
        TeacherClassPK that = (TeacherClassPK) o;
        return teacherPK == that.teacherPK && Objects.equals(classUnitPK, that.classUnitPK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitPK, teacherPK);
    }
}
