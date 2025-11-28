package thesis.model.persistence.entities.embeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class ConstraintPK {
    @Column(name = "constraint_id")
    private Integer constraintPK;

    @Column(name = "program_id")
    private Integer programPK;

    public ConstraintPK() {}

    public ConstraintPK(Integer constraintPK, Integer programPK) {
        this.constraintPK = constraintPK;
        this.programPK = programPK;
    }

    public Integer getConstraintPK() {
        return constraintPK;
    }

    public void setConstraintPK(Integer constraintPK) {
        this.constraintPK = constraintPK;
    }

    public Integer getProgramPK() {
        return programPK;
    }

    public void setProgramPK(Integer programPK) {
        this.programPK = programPK;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstraintPK)) return false;
        ConstraintPK that = (ConstraintPK) o;
        return Objects.equals(constraintPK, that.constraintPK) &&
                Objects.equals(programPK, that.programPK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constraintPK, programPK);
    }
}
