package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;
import thesis.model.persistence.repository.entities.embeddableIds.TeacherUnavailabilityPK;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "teacher_unavailability")
public class TeacherUnavailabilityEntity implements Serializable {
    @EmbeddedId
    private TeacherUnavailabilityPK id = new TeacherUnavailabilityPK();

    @ManyToOne
    @MapsId("teacherId")
    @JoinColumn(name = "teacher_id")
    private TeacherEntity teacherEntity;

    @ManyToOne
    @MapsId("timeBlockId")
    @JoinColumn(name = "time_block_id")
    private TimeBlockEntity timeBlockEntity;

    public TeacherUnavailabilityEntity() {}

    public TeacherUnavailabilityEntity(TeacherEntity teacherEntity, TimeBlockEntity timeBlockEntity) {
        this.teacherEntity = teacherEntity;
        this.timeBlockEntity = timeBlockEntity;

        teacherEntity.addUnavailability(this);
    }

    public TeacherUnavailabilityPK getId() {
        return id;
    }

    public void setId(TeacherUnavailabilityPK id) {
        this.id = id;
    }

    public TeacherEntity getTeacher() {
        return teacherEntity;
    }

    public void setTeacher(TeacherEntity teacherEntity) {
        this.teacherEntity = teacherEntity;
    }

    public short getStartSlot() {
        return timeBlockEntity.getStartSlot();
    }

    public short getDuration() {
        return timeBlockEntity.getDuration();
    }

    public short getDays() {
        return timeBlockEntity.getDays();
    }

    public int getWeeks() {
        return timeBlockEntity.getWeeks();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TeacherUnavailabilityEntity)) return false;
        TeacherUnavailabilityEntity that = (TeacherUnavailabilityEntity) o;
        return Objects.equals(teacherEntity, that.teacherEntity) &&
                Objects.equals(timeBlockEntity, that.timeBlockEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherEntity, timeBlockEntity);
    }
}
