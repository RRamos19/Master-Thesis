package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;
import thesis.model.persistence.repository.entities.embeddableIds.ClassTimePK;

import java.util.Objects;

@Entity
@Table(name = "class_time")
public class ClassTimeEntity {
    @EmbeddedId
    private ClassTimePK id;

    @ManyToOne
    @MapsId("classUnitPK")
    @JoinColumn(name = "class_id", referencedColumnName = "id")
    private ClassUnitEntity classUnitEntity;

    @ManyToOne
    @MapsId("timeBlockPK")
    @JoinColumn(name = "time_block_id", referencedColumnName = "id")
    private TimeBlockEntity timeBlockEntity;

    @Column(nullable = false)
    private Integer penalty;

    public ClassTimeEntity() {}

    public ClassTimeEntity(ClassUnitEntity classUnitEntity, TimeBlockEntity timeBlockEntity, int penalty) {
        this.id = new ClassTimePK(classUnitEntity.getId(), timeBlockEntity.getId());
        this.classUnitEntity = classUnitEntity;
        classUnitEntity.addClassTime(this);
        this.timeBlockEntity = timeBlockEntity;
        this.penalty = penalty;
    }

    public ClassTimePK getId() {
        return id;
    }

    public void setId(ClassTimePK id) {
        this.id = id;
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

    public short getDays() {
        return timeBlockEntity.getDays();
    }

    public short getStartSlot() {
        return timeBlockEntity.getStartSlot();
    }

    public short getDuration() {
        return timeBlockEntity.getDuration();
    }

    public int getWeeks() {
        return timeBlockEntity.getWeeks();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassTimeEntity)) return false;
        ClassTimeEntity that = (ClassTimeEntity) o;
        return Objects.equals(classUnitEntity, that.classUnitEntity) &&
                Objects.equals(timeBlockEntity, that.timeBlockEntity) &&
                Objects.equals(penalty, that.penalty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitEntity, timeBlockEntity, penalty);
    }
}
