package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;
import thesis.model.persistence.repository.entities.embeddableIds.ClassRoomPK;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "class_room")
public class ClassRoomEntity implements Serializable {
    @EmbeddedId
    private ClassRoomPK id = new ClassRoomPK();

    @ManyToOne
    @MapsId("classUnitId")
    @JoinColumn(name = "class_id")
    private ClassUnitEntity classUnitEntity;

    @ManyToOne
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    private RoomEntity roomEntity;

    @Column(nullable = false)
    private Integer penalty;

    public ClassRoomEntity() {}

    public ClassRoomEntity(ClassUnitEntity classUnitEntity, RoomEntity roomEntity, Integer penalty) {
        this.classUnitEntity = classUnitEntity;
        this.roomEntity = roomEntity;
        this.penalty = penalty;

        classUnitEntity.addClassRoom(this);
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
    }

    public RoomEntity getRoom() {
        return roomEntity;
    }

    public void setRoom(RoomEntity roomEntity) {
        this.roomEntity = roomEntity;
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
