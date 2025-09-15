package thesis.model.persistence.entities;

import jakarta.persistence.*;
import thesis.model.persistence.entities.embeddableids.ClassTimePK;

import java.util.UUID;

@Entity
@Table(name = "class_time")
public class ClassTimeEntity {
    @EmbeddedId
    private ClassTimePK id;

    @ManyToOne
    @MapsId("classUnitPK")
    @JoinColumn(name = "class_id", referencedColumnName = "id", nullable = false)
    private ClassUnitEntity classUnitEntity;

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

    public ClassTimeEntity() {}

    public ClassTimeEntity(ClassUnitEntity classUnitEntity, String days, String weeks, int startSlot, int duration, int penalty) {
        this.id = new ClassTimePK(classUnitEntity.getId(), UUID.randomUUID());
        this.classUnitEntity = classUnitEntity;
        classUnitEntity.addClassTime(this);
        this.days = days;
        this.weeks = weeks;
        this.startSlot = startSlot;
        this.duration = duration;
        this.penalty = penalty;
    }

    public ClassTimePK getId() {
        return id;
    }

    public ClassUnitEntity getClassUnit() {
        return classUnitEntity;
    }

    public void setClassUnit(ClassUnitEntity classUnitEntity) {
        classUnitEntity.addClassTime(this);
        this.classUnitEntity = classUnitEntity;
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
