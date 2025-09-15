package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "constraint_type")
public class ConstraintTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 30, unique = true)
    private String name;

    @OneToMany(mappedBy = "constraintTypeEntity", orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<ConstraintEntity> constraintEntityList = new ArrayList<>();

    public ConstraintTypeEntity() {}

    public ConstraintTypeEntity(String name) {
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

    public List<ConstraintEntity> getConstraintEntityList() {
        return constraintEntityList;
    }

    public void addConstraintEntity(ConstraintEntity constraintEntity) {
        constraintEntityList.add(constraintEntity);
    }
}
