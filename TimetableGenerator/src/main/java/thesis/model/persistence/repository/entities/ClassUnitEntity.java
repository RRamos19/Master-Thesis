package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "class_unit")
public class ClassUnitEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "subpart_id", referencedColumnName = "id", nullable = false)
    private SubpartEntity subpartEntity;

    @OneToOne
    @JoinColumn(name = "parent_class_id", referencedColumnName = "id")
    private ClassUnitEntity parentClass;

    @Column(length = 10)
    private String name;

    @OneToMany(mappedBy = "classUnitEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<TeacherClassEntity> teacherClassEntitySet = new HashSet<>();

    @OneToMany(mappedBy = "classUnitEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<ClassTimeEntity> classTimeEntitySet = new HashSet<>();

    @OneToMany(mappedBy = "classUnitEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<ClassRoomEntity> classRoomEntitySet = new HashSet<>();

    @OneToMany(mappedBy = "classUnitEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<ClassConstraintEntity> classConstraintEntitySet = new HashSet<>();

    public ClassUnitEntity() {}

    public ClassUnitEntity(SubpartEntity subpartEntity, String name) {
        subpartEntity.addClassUnit(this);
        this.subpartEntity = subpartEntity;
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

    public void setSubpart(SubpartEntity subpartEntity) {
        this.subpartEntity = subpartEntity;
    }

    public void setParentClass(ClassUnitEntity classUnitEntity) {
        this.parentClass = classUnitEntity;
    }

    public ClassUnitEntity getParentClass() {
        return parentClass;
    }

    public SubpartEntity getSubpart() {
        return subpartEntity;
    }

    public Set<TeacherClassEntity> getTeacherClassEntitySet() {
        return teacherClassEntitySet;
    }

    public Set<ClassTimeEntity> getClassTimeEntitySet() {
        return classTimeEntitySet;
    }

    public Set<ClassRoomEntity> getClassRoomEntitySet() {
        return classRoomEntitySet;
    }

    public Set<ClassConstraintEntity> getClassConstraintEntitySet() {
        return classConstraintEntitySet;
    }

    public void addTeacherClass(TeacherClassEntity teacherClassEntity) {
        teacherClassEntitySet.add(teacherClassEntity);
    }

    public void addClassTime(ClassTimeEntity classTimeEntity) {
        classTimeEntitySet.add(classTimeEntity);
    }

    public void addClassRoom(ClassRoomEntity classRoomEntity) {
        classRoomEntitySet.add(classRoomEntity);
    }

    public void addClassConstraint(ClassConstraintEntity classConstraintEntity) {
        classConstraintEntitySet.add(classConstraintEntity);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassUnitEntity)) return false;
        ClassUnitEntity that = (ClassUnitEntity) o;
        return Objects.equals(parentClass, that.parentClass) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentClass, name);
    }
}
