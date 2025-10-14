package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "teacher")
public class TeacherEntity {
    @Id
    private Integer id;

    @Column(length = 30, nullable = false)
    private String name;

    @OneToMany(mappedBy = "teacherEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<TeacherUnavailabilityEntity> teacherUnavailabilityEntitySet = new HashSet<>();

    public TeacherEntity() {}

    public TeacherEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addUnavailability(TeacherUnavailabilityEntity teacherUnav) {
        teacherUnavailabilityEntitySet.add(teacherUnav);
        teacherUnav.setTeacher(this);
    }

    public Set<TeacherUnavailabilityEntity> getTeacherUnavailabilityEntitySet() {
        return teacherUnavailabilityEntitySet;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TeacherEntity)) return false;
        TeacherEntity that = (TeacherEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
