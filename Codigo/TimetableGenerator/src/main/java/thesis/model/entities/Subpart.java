package thesis.model.entities;

import jakarta.persistence.*;
import thesis.model.entities.EmbeddableIds.SubpartPK;

import java.util.UUID;

@Entity
@Table(name = "subpart")
public class Subpart {
    @EmbeddedId
    private SubpartPK id;

    @ManyToOne
    @MapsId("configPK")
    @JoinColumns({
            @JoinColumn(name = "course_id", referencedColumnName = "course_id"),
            @JoinColumn(name = "config_id", referencedColumnName = "config_id")
    })
    private Config config;

    @Column(length = 10, unique = true, nullable = false)
    private String name;

    public Subpart() {}

    public Subpart(Config config, String name) {
        this.id = new SubpartPK(config.getId(), UUID.randomUUID());
        this.config = config;
        this.name = name;
    }

    public SubpartPK getId() {
        return id;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
