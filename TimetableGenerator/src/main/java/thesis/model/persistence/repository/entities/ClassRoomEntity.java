package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;
import thesis.model.persistence.repository.entities.embeddableIds.ClassRoomPK;

import java.util.Objects;

@Entity
@Table(name = "class_room")
public class ClassRoomEntity {
    @EmbeddedId
    private ClassRoomPK id;

    @ManyToOne
    @MapsId("classUnitPK")
    @JoinColumn(name = "class_id", referencedColumnName = "id")
    private ClassUnitEntity classUnitEntity;

    @ManyToOne
    @MapsId("roomId")
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    private RoomEntity roomEntity;

    @Column(nullable = false)
    private Integer penalty;

    public ClassRoomEntity() {}

    public ClassRoomEntity(ClassUnitEntity classUnitEntity, RoomEntity roomEntity, Integer penalty) {
        this.id = new ClassRoomPK(classUnitEntity.getId(), roomEntity.getId());
        classUnitEntity.addClassRoom(this);
        this.classUnitEntity = classUnitEntity;
        this.roomEntity = roomEntity;
        this.penalty = penalty;
    }

    public ClassRoomPK getId() {
        return id;
    }

    public void setId(ClassRoomPK id) {
        this.id = id;
    }

    public Integer getPenalty() {
        return penalty;
    }

    public void setPenalty(Integer penalty) {
        this.penalty = penalty;
    }

    public ClassUnitEntity getClassUnit() {
        return classUnitEntity;
    }

    public void setClassUnit(ClassUnitEntity classUnitEntity) {
        this.classUnitEntity = classUnitEntity;
        this.id.setClassUnitPK(classUnitEntity.getId());
    }

    public RoomEntity getRoom() {
        return roomEntity;
    }

    public void setRoom(RoomEntity roomEntity) {
        this.roomEntity = roomEntity;
        this.id.setRoomId(roomEntity.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassRoomEntity)) return false;
        ClassRoomEntity that = (ClassRoomEntity) o;
        return Objects.equals(classUnitEntity, that.classUnitEntity) &&
                Objects.equals(roomEntity, that.roomEntity) &&
                Objects.equals(penalty, that.penalty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitEntity, roomEntity, penalty);
    }
}
