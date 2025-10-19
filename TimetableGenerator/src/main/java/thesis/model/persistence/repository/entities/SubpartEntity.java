package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "subpart")
public class SubpartEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "config_id", referencedColumnName = "id")
    private ConfigEntity configEntity;

    @Column(length = 10, unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "subpartEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<ClassUnitEntity> classUnitSet = new HashSet<>();

    public SubpartEntity() {}

    public SubpartEntity(ConfigEntity configEntity, String name) {
        configEntity.addSubpart(this);
        this.configEntity = configEntity;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ConfigEntity getConfig() {
        return configEntity;
    }

    public void setConfig(ConfigEntity configEntity) {
        this.configEntity = configEntity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addClassUnit(ClassUnitEntity classUnitEntity) {
        classUnitSet.add(classUnitEntity);
        classUnitEntity.setSubpart(this);
    }

    public Set<ClassUnitEntity> getClassUnitSet() {
        return classUnitSet;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SubpartEntity)) return false;
        SubpartEntity that = (SubpartEntity) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
