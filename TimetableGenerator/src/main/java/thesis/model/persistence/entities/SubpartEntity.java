package thesis.model.persistence.entities;

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

    @ManyToOne
    @JoinColumn(name = "name_id", referencedColumnName = "id", nullable = false)
    private SubpartNameEntity subpartNameEntity;

    @OneToMany(mappedBy = "subpartEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<ClassUnitEntity> classUnitSet = new HashSet<>();

    public SubpartEntity() {}

    public SubpartEntity(ConfigEntity configEntity, SubpartNameEntity subpartNameEntity) {
        configEntity.addSubpart(this);
        this.configEntity = configEntity;
        this.subpartNameEntity = subpartNameEntity;
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

    public SubpartNameEntity getSubpartNameEntity() {
        return subpartNameEntity;
    }

    public void setSubpartNameEntity(SubpartNameEntity subpartNameEntity) {
        this.subpartNameEntity = subpartNameEntity;
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
        return Objects.equals(subpartNameEntity, that.subpartNameEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subpartNameEntity);
    }
}
