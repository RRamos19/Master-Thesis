package thesis.model.entities;

import jakarta.persistence.*;
import thesis.model.entities.EmbeddableIds.ClassTimePK;

import java.util.UUID;

@Entity
@Table(name = "class_time")
public class ClassTime {
    @EmbeddedId
    private ClassTimePK id;

    @ManyToOne
    @MapsId("classUnitPK")
    @JoinColumns({
            @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false),
            @JoinColumn(name = "config_id", referencedColumnName = "config_id", nullable = false),
            @JoinColumn(name = "subpart_id", referencedColumnName = "subpart_id", nullable = false),
            @JoinColumn(name = "class_id", referencedColumnName = "class_id", nullable = false)
    })
    private ClassUnit classUnit;

    @Column(nullable = false)
    private int penalty;

    @Column(length = 7, nullable = false)
    private String days;

    @Column(name = "start_slot", nullable = false)
    private int startSlot;

    @Column(nullable = false)
    private int duration;

    @Column(length = 16, nullable = false)
    private String weeks;

    public ClassTime() {}

    public ClassTime(ClassUnit classUnit, String days, String weeks, int startSlot, int duration, int penalty) {
        this.id = new ClassTimePK(classUnit.getId(), UUID.randomUUID());
        this.classUnit = classUnit;
        this.days = days;
        this.weeks = weeks;
        this.startSlot = startSlot;
        this.duration = duration;
        this.penalty = penalty;
    }

    public ClassTimePK getId() {
        return id;
    }

    public ClassUnit getClassUnit() {
        return classUnit;
    }

    public void setClassUnit(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public int getStartSlot() {
        return startSlot;
    }

    public void setStartSlot(int startSlot) {
        this.startSlot = startSlot;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getWeeks() {
        return weeks;
    }

    public void setWeeks(String weeks) {
        this.weeks = weeks;
    }
}
