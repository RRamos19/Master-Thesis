package thesis.model.persistence.repository.entities.embeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClassConstraintPK implements Serializable {
    @Column(name = "class_id")
    private Integer classUnitId;

    @Column(name = "constraint_id")
    private Integer constraintId;

    public ClassConstraintPK() {}

    public ClassConstraintPK(Integer classUnitId, Integer constraintId) {
        this.classUnitId = classUnitId;
        this.constraintId = constraintId;
    }

    public Integer getClassUnitId() {
        return classUnitId;
    }

    public void setClassUnitId(Integer classUnitId) {
        this.classUnitId = classUnitId;
    }

    public Integer getConstraintId() {
        return constraintId;
    }

    public void setConstraintId(Integer constraintId) {
        this.constraintId = constraintId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassConstraintPK)) return false;
        ClassConstraintPK that = (ClassConstraintPK) o;
        return Objects.equals(classUnitId, that.classUnitId) &&
                Objects.equals(constraintId, that.constraintId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitId, constraintId);
    }
}
