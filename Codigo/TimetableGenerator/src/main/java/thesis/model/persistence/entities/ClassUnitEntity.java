package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "class_unit")
public class ClassUnitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @MapsId("subpartPK")
    @JoinColumn(name = "subpart_id", referencedColumnName = "id", nullable = false)
    private SubpartEntity subpartEntity;

    @OneToOne
    @JoinColumn(name = "parent_class_id", referencedColumnName = "id")
    private ClassUnitEntity parentClass;

    @Column(length = 10)
    private String name;

    //@OneToMany(mappedBy = "classUnit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "teacher_class",
            joinColumns = { @JoinColumn(name = "class_id") },
            inverseJoinColumns = { @JoinColumn(name = "teacher_id") }
    )
    private List<TeacherEntity> teacherEntityClassList = new ArrayList<>();

    @OneToMany(mappedBy = "classUnit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ClassTimeEntity> classTimeEntityList = new ArrayList<>();

    @OneToMany(mappedBy = "classUnit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ClassRoomEntity> classRoomEntityList = new ArrayList<>();

    @OneToMany(mappedBy = "classUnit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ClassRestrictionEntity> classRestrictionEntityList = new ArrayList<>();

    @OneToMany(mappedBy = "classUnit", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ScheduledLessonEntity> scheduledLessonEntityList = new ArrayList<>();

    public ClassUnitEntity() {}

    public ClassUnitEntity(SubpartEntity subpartEntity, String name) {
        subpartEntity.addClassUnit(this);
        this.subpartEntity = subpartEntity;
        this.name = name;
    }

    public UUID getId() {
        return id;
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

    public List<ClassRestrictionEntity> getClassRestrictionList() {
        return classRestrictionEntityList;
    }

    public SubpartEntity getSubpart() {
        return subpartEntity;
    }

    public void addScheduledLesson(ScheduledLessonEntity scheduledLessonEntity) {
        scheduledLessonEntityList.add(scheduledLessonEntity);
        scheduledLessonEntity.setClassUnit(this);
    }

//    public void addTeacherClass(TeacherClass teacherClass) {
//        teacherClassList.add(teacherClass);
//        teacherClass.setClassUnit(this);
//    }
    public void addTeacherClass(TeacherEntity teacherEntity) {
        teacherEntityClassList.add(teacherEntity);
    }

    public void addClassTime(ClassTimeEntity classTimeEntity) {
        classTimeEntityList.add(classTimeEntity);
    }

    public void addClassRoom(ClassRoomEntity classRoomEntity) {
        classRoomEntityList.add(classRoomEntity);
    }

    public void addClassRestriction(ClassRestrictionEntity classRestrictionEntity) {
        classRestrictionEntityList.add(classRestrictionEntity);
    }
}
