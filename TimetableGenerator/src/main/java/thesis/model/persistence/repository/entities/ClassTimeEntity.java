package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;
import thesis.model.persistence.repository.entities.embeddableIds.ClassTimePK;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "class_time")
public class ClassTimeEntity implements Serializable {
    @EmbeddedId
    private ClassTimePK id = new ClassTimePK();

    @ManyToOne
    @MapsId("classUnitPK")
    @JoinColumn(name = "class_id")
    private ClassUnitEntity classUnitEntity;

    @ManyToOne
    @MapsId("timeBlockPK")
    @JoinColumn(name = "time_block_id")
    private TimeBlockEntity timeBlockEntity;

    @Column(nullable = false)
    private Integer penalty;

    public ClassTimeEntity() {}

    public ClassTimeEntity(ClassUnitEntity classUnitEntity, TimeBlockEntity timeBlockEntity, int penalty) {
        this.classUnitEntity = classUnitEntity;
        this.timeBlockEntity = timeBlockEntity;
        this.penalty = penalty;

        classUnitEntity.addClassTime(this);
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
        this.classUnitEntity.getClassTimeEntitySet().remove(this);
        this.classUnitEntity = classUnitEntity;
        classUnitEntity.addClassTime(this);
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public TimeBlockEntity getTimeBlockEntity() {
        return timeBlockEntity;
    }

    public void setTimeBlockEntity(TimeBlockEntity timeBlockEntity) {
        this.timeBlockEntity = timeBlockEntity;
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
