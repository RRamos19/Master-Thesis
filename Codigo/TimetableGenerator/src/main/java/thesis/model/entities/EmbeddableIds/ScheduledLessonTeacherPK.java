package thesis.model.entities.EmbeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ScheduledLessonTeacherPK implements Serializable {
    @Embedded
    private ScheduledLessonPK scheduledLessonPK;

    @Column(name = "teacher_id")
    private int teacherId;

    public ScheduledLessonTeacherPK() {}

    public ScheduledLessonTeacherPK(ScheduledLessonPK scheduledLessonPK, int teacherId) {
        this.scheduledLessonPK = scheduledLessonPK;
        this.teacherId = teacherId;
    }

    public ScheduledLessonPK getScheduledLessonPK() {
        return scheduledLessonPK;
    }

    public void setScheduledLessonPK(ScheduledLessonPK scheduledLessonPK) {
        this.scheduledLessonPK = scheduledLessonPK;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ScheduledLessonTeacherPK)) return false;
        ScheduledLessonTeacherPK that = (ScheduledLessonTeacherPK) o;
        return teacherId == that.teacherId && Objects.equals(scheduledLessonPK, that.scheduledLessonPK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduledLessonPK, teacherId);
    }
}
