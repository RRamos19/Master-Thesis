package thesis.model.persistence.entities.embeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class TeacherClassPK implements Serializable {
    @Column(name = "class_id")
    private UUID classUnitPK;

    @Column(name = "teacher_id")
    private Integer teacherPK;

    public TeacherClassPK() {}

    public TeacherClassPK(UUID classUnitPK, Integer teacherPK) {
        this.classUnitPK = classUnitPK;
        this.teacherPK = teacherPK;
    }

    public UUID getClassUnitPK() {
        return classUnitPK;
    }

    public void setClassUnitPK(UUID classUnitPK) {
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
