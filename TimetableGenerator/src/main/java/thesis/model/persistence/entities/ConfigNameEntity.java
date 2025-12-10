package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "config_name")
public class ConfigNameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;

    public ConfigNameEntity() {}

    public ConfigNameEntity(String name) {
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
        if (!(o instanceof ConfigNameEntity)) return false;
        ConfigNameEntity that = (ConfigNameEntity) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
