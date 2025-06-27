package thesis.model.persistence.entities;

import jakarta.persistence.*;
import thesis.model.persistence.entities.embeddableids.ScheduledLessonTeacherPK;

@Entity
@Table(name = "scheduled_lesson_teacher")
public class ScheduledLessonTeacherEntity {
    @EmbeddedId
    private ScheduledLessonTeacherPK id;

    @ManyToOne
    @MapsId("teacherId")
    @JoinColumn(name = "teacher_id", referencedColumnName = "id")
    private TeacherEntity teacherEntity;

    @ManyToOne
    @MapsId("scheduledLessonPK")
    @JoinColumns({
            @JoinColumn(name = "scheduled_lesson_timetable_id", referencedColumnName = "timetable_id"),
            @JoinColumn(name = "scheduled_lesson_id", referencedColumnName = "id")
    })
    private ScheduledLessonEntity scheduledLessonEntity;

    public ScheduledLessonTeacherEntity() {}

    public ScheduledLessonTeacherEntity(TeacherEntity teacherEntity, ScheduledLessonEntity scheduledLessonEntity) {
        this.id = new ScheduledLessonTeacherPK(scheduledLessonEntity.getId(), teacherEntity.getId());
        teacherEntity.addScheduledLessonTeacherEntity(this);
        this.teacherEntity = teacherEntity;
        scheduledLessonEntity.addScheduledLessonTeacherEntity(this);
        this.scheduledLessonEntity = scheduledLessonEntity;
    }

    public ScheduledLessonTeacherPK getId() {
        return id;
    }

    public void setId(ScheduledLessonTeacherPK id) {
        this.id = id;
    }

    public TeacherEntity getTeacher() {
        return teacherEntity;
    }

    public void setTeacher(TeacherEntity teacherEntity) {
        this.teacherEntity = teacherEntity;
        this.id.setTeacherId(teacherEntity.getId());
    }

    public ScheduledLessonEntity getScheduledLesson() {
        return scheduledLessonEntity;
    }

    public void setScheduledLesson(ScheduledLessonEntity scheduledLessonEntity) {
        this.scheduledLessonEntity = scheduledLessonEntity;
        this.id.setScheduledLessonPK(scheduledLessonEntity.getId());
    }
}
