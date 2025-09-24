package thesis.model.persistence.entities.embeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ClassRoomPK implements Serializable {
    @Column(name = "class_id")
    private UUID classUnitPK;

    @Column(name = "room_id")
    private Integer roomId;

    public ClassRoomPK() {}

    public ClassRoomPK(UUID classUnitPK, Integer roomId) {
        this.classUnitPK = classUnitPK;
        this.roomId = roomId;
    }

    public UUID getClassUnitPK() {
        return classUnitPK;
    }

    public void setClassUnitPK(UUID classUnitPK) {
        this.classUnitPK = classUnitPK;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassRoomPK)) return false;
        ClassRoomPK that = (ClassRoomPK) o;
        return Objects.equals(roomId, that.roomId) && Objects.equals(classUnitPK, that.classUnitPK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitPK, roomId);
    }
}
