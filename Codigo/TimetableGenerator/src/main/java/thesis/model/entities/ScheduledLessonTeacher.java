package thesis.model.entities;

import jakarta.persistence.*;
import thesis.model.entities.EmbeddableIds.ScheduledLessonTeacherPK;

@Entity
@Table(name = "scheduled_lesson_teacher")
public class ScheduledLessonTeacher {
    @EmbeddedId
    private ScheduledLessonTeacherPK id;

    @ManyToOne
    @MapsId("teacherId")
    @JoinColumn(name = "teacher_id", referencedColumnName = "id")
    private Teacher teacher;

    @ManyToOne
    @MapsId("scheduledLessonPK")
    @JoinColumns({
            @JoinColumn(name = "scheduled_lesson_timetable_id", referencedColumnName = "timetable_id"),
            @JoinColumn(name = "scheduled_lesson_id", referencedColumnName = "id")
    })
    private ScheduledLesson scheduledLesson;

    public ScheduledLessonTeacher() {}

    public ScheduledLessonTeacher(Teacher teacher, ScheduledLesson scheduledLesson) {
        this.id = new ScheduledLessonTeacherPK(scheduledLesson.getId(), teacher.getId());
        this.teacher = teacher;
        this.scheduledLesson = scheduledLesson;
    }

    public ScheduledLessonTeacherPK getId() {
        return id;
    }

    public void setId(ScheduledLessonTeacherPK id) {
        this.id = id;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
        this.id.setTeacherId(teacher.getId());
    }

    public ScheduledLesson getScheduledLesson() {
        return scheduledLesson;
    }

    public void setScheduledLesson(ScheduledLesson scheduledLesson) {
        this.scheduledLesson = scheduledLesson;
        this.id.setScheduledLessonPK(scheduledLesson.getId());
    }
}
