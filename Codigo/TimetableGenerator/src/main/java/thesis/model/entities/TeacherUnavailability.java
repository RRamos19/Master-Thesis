package thesis.model.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "teacher_unavailability")
public class TeacherUnavailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", referencedColumnName = "id")
    private Teacher teacher;

    @Column(nullable = false)
    private int duration;

    @Column(name = "start_slot", nullable = false)
    private int startSlot;

    @Column(length = 7, nullable = false)
    private String days;

    @Column(length = 16, nullable = false)
    private String weeks;

    public TeacherUnavailability() {}

    public TeacherUnavailability(Teacher teacher, int duration, int startSlot, String days, String weeks) {
        this.teacher = teacher;
        this.duration = duration;
        this.startSlot = startSlot;
        this.days = days;
        this.weeks = weeks;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
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
