package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restriction")
public class RestrictionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 30, unique = true)
    private String name;

    @OneToMany(mappedBy = "restrictionEntity", orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<ClassRestrictionEntity> classRestrictionEntityList = new ArrayList<>();

    public RestrictionEntity() {}

    public RestrictionEntity(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addClassRestriction(ClassRestrictionEntity classRestrictionEntity) {
        classRestrictionEntityList.add(classRestrictionEntity);
        classRestrictionEntity.setRestriction(this);
    }

    public List<ClassRestrictionEntity> getClassRestrictionEntityList() {
        return classRestrictionEntityList;
    }
}
