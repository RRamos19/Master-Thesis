package thesis.model.persistence.repository.entities.embeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ScheduledLessonPK implements Serializable {
    @Column(name = "timetable_id")
    private Integer timetableId;

    @Column(name = "class_id")
    private Integer classId;

    @Column(name = "time_block_id")
    private Integer timeBlockId;

    public ScheduledLessonPK() {}

    public ScheduledLessonPK(Integer timetableId, Integer classId, Integer timeBlockId) {
        this.timetableId = timetableId;
        this.classId = classId;
        this.timeBlockId = timeBlockId;
    }

    public Integer getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(Integer timetableId) {
        this.timetableId = timetableId;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Integer getTimeBlockId() {
        return timeBlockId;
    }

    public void setTimeBlockId(Integer timeBlockId) {
        this.timeBlockId = timeBlockId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ScheduledLessonPK)) return false;
        ScheduledLessonPK that = (ScheduledLessonPK) o;
        return Objects.equals(timetableId, that.timetableId) &&
                Objects.equals(classId, that.classId) &&
                Objects.equals(timeBlockId, that.timeBlockId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timetableId, classId, timeBlockId);
    }
}
