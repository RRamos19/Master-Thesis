package thesis.model.persistence.entities.embeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TeacherClassPK implements Serializable {
    @Column(name = "class_id")
    private Integer classUnitId;

    @Column(name = "teacher_id")
    private Integer teacherId;

    public TeacherClassPK() {}

    public TeacherClassPK(Integer classUnitId, Integer teacherId) {
        this.classUnitId = classUnitId;
        this.teacherId = teacherId;
    }

    public Integer getClassUnitId() {
        return classUnitId;
    }

    public void setClassUnitId(Integer classUnitId) {
        this.classUnitId = classUnitId;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TeacherClassPK)) return false;
        TeacherClassPK that = (TeacherClassPK) o;
        return Objects.equals(teacherId, that.teacherId) &&
                Objects.equals(classUnitId, that.classUnitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitId, teacherId);
    }
}
