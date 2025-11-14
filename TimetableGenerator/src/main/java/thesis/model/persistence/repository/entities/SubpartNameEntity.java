package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "subpart_name")
public class SubpartNameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 10, nullable = false)
    private String name;

    public SubpartNameEntity() {}

    public SubpartNameEntity(String name) {
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
        if (!(o instanceof SubpartNameEntity)) return false;
        SubpartNameEntity that = (SubpartNameEntity) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
