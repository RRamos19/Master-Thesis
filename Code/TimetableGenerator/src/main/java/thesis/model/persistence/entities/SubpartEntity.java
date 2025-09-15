package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "subpart")
public class SubpartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "config_id", referencedColumnName = "id")
    private ConfigEntity configEntity;

    @Column(length = 10, unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "subpartEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<ClassUnitEntity> classUnitEntities = new ArrayList<>();

    public SubpartEntity() {}

    public SubpartEntity(ConfigEntity configEntity, String name) {
        configEntity.addSubpart(this);
        this.configEntity = configEntity;
        this.name = name;
    }

    public UUID getId() {
        return id;
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
        classUnitEntities.add(classUnitEntity);
        classUnitEntity.setSubpart(this);
    }

    public List<ClassUnitEntity> getClassUnits() {
        return classUnitEntities;
    }
}
