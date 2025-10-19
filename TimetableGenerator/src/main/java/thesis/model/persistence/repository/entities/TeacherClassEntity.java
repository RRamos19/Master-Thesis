package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;
import thesis.model.persistence.repository.entities.embeddableIds.TeacherClassPK;

import java.io.Serializable;

@Entity
@Table(name = "teacher_class")
public class TeacherClassEntity implements Serializable {
    @EmbeddedId
    private TeacherClassPK id = new TeacherClassPK();

    @ManyToOne
    @MapsId("classUnitId")
    @JoinColumn(name = "class_id")
    private ClassUnitEntity classUnitEntity;

    @ManyToOne
    @MapsId("teacherId")
    @JoinColumn(name = "teacher_id")
    private TeacherEntity teacherEntity;

    public TeacherClassEntity() {}

    public TeacherClassEntity(ClassUnitEntity classUnitEntity, TeacherEntity teacherEntity) {
        this.classUnitEntity = classUnitEntity;
        this.teacherEntity = teacherEntity;

        classUnitEntity.addTeacherClass(this);
    }

    public TeacherClassPK getId() {
        return id;
    }

    public void setId(TeacherClassPK id) {
        this.id = id;
    }

    public ClassUnitEntity getClassUnitEntity() {
        return classUnitEntity;
    }

    public void setClassUnitEntity(ClassUnitEntity classUnitEntity) {
        this.classUnitEntity = classUnitEntity;
    }

    public TeacherEntity getTeacherEntity() {
        return teacherEntity;
    }

    public void setTeacherEntity(TeacherEntity teacherEntity) {
        this.teacherEntity = teacherEntity;
    }
}
