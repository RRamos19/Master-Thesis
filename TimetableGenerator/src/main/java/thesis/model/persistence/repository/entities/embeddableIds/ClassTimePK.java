package thesis.model.persistence.repository.entities.embeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClassTimePK implements Serializable {
    @Column(name = "class_id")
    private Integer classUnitPK;

    @Column(name = "time_block_id")
    private Integer timeBlockPK;

    public ClassTimePK() {}

    public ClassTimePK(Integer classUnitPK, Integer timeBlockPK) {
        this.classUnitPK = classUnitPK;
        this.timeBlockPK = timeBlockPK;
    }

    public Integer getClassUnitPK() {
        return classUnitPK;
    }

    public void setClassUnitPK(int classUnitPK) {
        this.classUnitPK = classUnitPK;
    }

    public Integer getTimeBlockPK() {
        return timeBlockPK;
    }

    public void setTimeBlockPK(int timeBlockPK) {
        this.timeBlockPK = timeBlockPK;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassTimePK)) return false;
        ClassTimePK that = (ClassTimePK) o;
        return Objects.equals(classUnitPK, that.classUnitPK) &&
                Objects.equals(timeBlockPK, that.timeBlockPK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitPK, timeBlockPK);
    }
}
