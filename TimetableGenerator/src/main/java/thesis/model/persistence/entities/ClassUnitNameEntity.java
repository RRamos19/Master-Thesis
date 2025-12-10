package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "class_unit_name")
public class ClassUnitNameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;

    public ClassUnitNameEntity() {}

    public ClassUnitNameEntity(String name) {
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
        if (!(o instanceof ClassUnitNameEntity)) return false;
        ClassUnitNameEntity that = (ClassUnitNameEntity) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
