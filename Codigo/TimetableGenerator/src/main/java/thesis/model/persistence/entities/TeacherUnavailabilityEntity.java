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
    private int duration;

    @Column(name = "start_slot", nullable = false)
    private int startSlot;

    @Column(length = 7, nullable = false)
    private String days;

    @Column(length = 16, nullable = false)
    private String weeks;

    public TeacherUnavailabilityEntity() {}

    public TeacherUnavailabilityEntity(TeacherEntity teacherEntity, int duration, int startSlot, String days, String weeks) {
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getStartSlot() {
        return startSlot;
    }

    public void setStartSlot(int startSlot) {
        this.startSlot = startSlot;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getWeeks() {
        return weeks;
    }

    public void setWeeks(String weeks) {
        this.weeks = weeks;
    }
}
