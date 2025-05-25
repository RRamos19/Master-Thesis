package thesis.model.entities;

import jakarta.persistence.*;
import thesis.model.entities.EmbeddableIds.ClassRestrictionPK;

@Entity
@Table(name = "class_restriction")
public class ClassRestriction {
    @EmbeddedId
    private ClassRestrictionPK id;

    @ManyToOne
    @MapsId("classUnitId")
    @JoinColumns({
            @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false),
            @JoinColumn(name = "config_id", referencedColumnName = "config_id", nullable = false),
            @JoinColumn(name = "subpart_id", referencedColumnName = "subpart_id", nullable = false),
            @JoinColumn(name = "class_id", referencedColumnName = "class_id", nullable = false)
    })
    private ClassUnit classUnit;

    @ManyToOne
    @MapsId("restrictionId")
    @JoinColumn(name = "restriction_id", referencedColumnName = "id")
    private Restriction restriction;

    private int penalty;

    @Column(nullable = false)
    private boolean required;

    public ClassRestriction() {}

    public ClassRestriction(ClassUnit classUnit, Restriction restriction, int penalty, boolean required) {
        this.id = new ClassRestrictionPK(classUnit.getId(), restriction.getId());
        this.classUnit = classUnit;
        this.restriction = restriction;
        this.penalty = penalty;
        this.required = required;
    }

    public ClassRestrictionPK getId() {
        return id;
    }

    public void setId(ClassRestrictionPK id) {
        this.id = id;
    }

    public ClassUnit getClassUnit() {
        return classUnit;
    }

    public void setClassUnit(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.id.setClassUnitId(classUnit.getId());
    }

    public Restriction getRestriction() {
        return restriction;
    }

    public void setRestriction(Restriction restriction) {
        this.restriction = restriction;
        this.id.setRestrictionId(restriction.getId());
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean getRequired() {
        return required;
    }
}
