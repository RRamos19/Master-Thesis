package thesis.model.persistence.entities.embeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TeacherUnavailabilityPK implements Serializable {
    @Column(name = "teacher_id")
    private Integer teacherId;

    @Column(name = "time_block_id")
    private Integer timeBlockId;

    public TeacherUnavailabilityPK() {}

    public TeacherUnavailabilityPK(Integer teacherPK, Integer timeBlockId) {
        this.teacherId = teacherPK;
        this.timeBlockId = timeBlockId;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getTimeBlockId() {
        return timeBlockId;
    }

    public void setTimeBlockId(Integer timeBlockId) {
        this.timeBlockId = timeBlockId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TeacherUnavailabilityPK)) return false;
        TeacherUnavailabilityPK that = (TeacherUnavailabilityPK) o;
        return Objects.equals(teacherId, that.teacherId) &&
                Objects.equals(timeBlockId, that.timeBlockId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherId, timeBlockId);
    }
}
