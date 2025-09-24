package thesis.model.persistence.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "teacher_unavailability")
public class TeacherUnavailabilityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", referencedColumnName = "id")
    private TeacherEntity teacherEntity;

    @Column(nullable = false)
    private short duration;

    @Column(name = "start_slot", nullable = false)
    private short startSlot;

    @Column(nullable = false)
    private short days;

    @Column(nullable = false)
    private int weeks;

    public TeacherUnavailabilityEntity() {}

    public TeacherUnavailabilityEntity(TeacherEntity teacherEntity, short duration, short startSlot, short days, int weeks) {
        teacherEntity.addUnavailability(this);
        this.teacherEntity = teacherEntity;
        this.duration = duration;
        this.startSlot = startSlot;
        this.days = days;
        this.weeks = weeks;
    }

    public Integer getId() {
        return id;
    }

    public TeacherEntity getTeacher() {
        return teacherEntity;
    }

    public void setTeacher(TeacherEntity teacherEntity) {
        this.teacherEntity = teacherEntity;
    }

    public short getDuration() {
        return duration;
    }

    public void setDuration(short duration) {
        this.duration = duration;
    }

    public short getStartSlot() {
        return startSlot;
    }

    public void setStartSlot(short startSlot) {
        this.startSlot = startSlot;
    }

    public short getDays() {
        return days;
    }

    public void setDays(short days) {
        this.days = days;
    }

    public int getWeeks() {
        return weeks;
    }

    public void setWeeks(short weeks) {
        this.weeks = weeks;
    }
}
