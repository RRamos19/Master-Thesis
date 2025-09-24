package thesis.model.persistence.entities.embeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ScheduledLessonPK implements Serializable {
    @Column(name = "id")
    private UUID scheduledLessonId;

    @Column(name = "timetable_id")
    private UUID timetableId;

    public ScheduledLessonPK() {}

    public ScheduledLessonPK(UUID timetableId, UUID scheduledLessonId) {
        this.timetableId = timetableId;
        this.scheduledLessonId = scheduledLessonId;
    }

    public UUID getScheduledLessonId() {
        return scheduledLessonId;
    }

    public void setScheduledLessonId(UUID scheduledLessonId) {
        this.scheduledLessonId = scheduledLessonId;
    }

    public UUID getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(UUID timetableId) {
        this.timetableId = timetableId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ScheduledLessonPK)) return false;
        ScheduledLessonPK that = (ScheduledLessonPK) o;
        return Objects.equals(scheduledLessonId, that.scheduledLessonId) && Objects.equals(timetableId, that.timetableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduledLessonId, timetableId);
    }
}
