package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;
import thesis.model.persistence.repository.entities.embeddableIds.ScheduledLessonTeacherPK;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "scheduled_lesson_teacher")
public class ScheduledLessonTeacherEntity implements Serializable {
    @EmbeddedId
    private ScheduledLessonTeacherPK id = new ScheduledLessonTeacherPK();

    @ManyToOne
    @MapsId("teacherId")
    @JoinColumn(name = "teacher_id")
    private TeacherEntity teacherEntity;

    @ManyToOne
    @MapsId("scheduledLessonPK")
    private ScheduledLessonEntity scheduledLessonEntity;

    public ScheduledLessonTeacherEntity() {}

    public ScheduledLessonTeacherEntity(TeacherEntity teacherEntity, ScheduledLessonEntity scheduledLessonEntity) {
        this.teacherEntity = teacherEntity;
        this.scheduledLessonEntity = scheduledLessonEntity;

        scheduledLessonEntity.addScheduledLessonTeacherEntity(this);
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
