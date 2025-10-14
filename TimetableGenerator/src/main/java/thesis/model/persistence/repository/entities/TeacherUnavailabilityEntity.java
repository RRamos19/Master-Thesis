package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "teacher_unavailability")
public class TeacherUnavailabilityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", referencedColumnName = "id")
    private TeacherEntity teacherEntity;

    @ManyToOne
    @JoinColumn(name = "time_block_id", referencedColumnName = "id")
    private TimeBlockEntity timeBlockEntity;

    public TeacherUnavailabilityEntity() {}

    public TeacherUnavailabilityEntity(TeacherEntity teacherEntity, TimeBlockEntity timeBlockEntity) {
        teacherEntity.addUnavailability(this);
        this.teacherEntity = teacherEntity;
        this.timeBlockEntity = timeBlockEntity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
