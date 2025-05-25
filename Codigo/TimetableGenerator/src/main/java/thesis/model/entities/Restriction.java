package thesis.model.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restriction")
public class Restriction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 30, unique = true)
    private String name;

    @OneToMany(mappedBy = "restriction")
    private List<ClassRestriction> classRestrictionList = new ArrayList<>();

    public Restriction() {}

    public Restriction(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addRestrictionSubject(ClassRestriction classRestriction) {
        classRestrictionList.add(classRestriction);
        classRestriction.setRestriction(this);
    }
}
