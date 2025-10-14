package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;
import thesis.model.persistence.repository.entities.embeddableIds.ScheduledLessonTeacherPK;

import java.util.Objects;

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
            @JoinColumn(name = "scheduled_lesson_time_block_id", referencedColumnName = "time_block_id"),
            @JoinColumn(name = "scheduled_lesson_class_id", referencedColumnName = "class_id")
    })
    private ScheduledLessonEntity scheduledLessonEntity;

    public ScheduledLessonTeacherEntity() {}

    public ScheduledLessonTeacherEntity(TeacherEntity teacherEntity, ScheduledLessonEntity scheduledLessonEntity) {
        this.id = new ScheduledLessonTeacherPK(scheduledLessonEntity.getId(), teacherEntity.getId());
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ScheduledLessonTeacherEntity)) return false;
        ScheduledLessonTeacherEntity that = (ScheduledLessonTeacherEntity) o;
        return Objects.equals(teacherEntity, that.teacherEntity) &&
                Objects.equals(scheduledLessonEntity, that.scheduledLessonEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherEntity, scheduledLessonEntity);
    }
}
