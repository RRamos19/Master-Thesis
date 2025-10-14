package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "constraint_type")
public class ConstraintTypeEntity {
    @Id
    private Integer id;

    @Column(length = 30, unique = true)
    private String name;

    public ConstraintTypeEntity() {}

    public ConstraintTypeEntity(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstraintTypeEntity)) return false;
        ConstraintTypeEntity that = (ConstraintTypeEntity) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
