package thesis.model.persistence.entities.embeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClassRoomPK implements Serializable {
    @Column(name = "class_id")
    private Integer classUnitId;

    @Column(name = "room_id")
    private Integer roomId;

    public ClassRoomPK() {}

    public ClassRoomPK(Integer classUnitId, Integer roomId) {
        this.classUnitId = classUnitId;
        this.roomId = roomId;
    }

    public Integer getClassUnitId() {
        return classUnitId;
    }

    public void setClassUnitId(int classUnitId) {
        this.classUnitId = classUnitId;
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
        return Objects.equals(roomId, that.roomId) &&
                Objects.equals(classUnitId, that.classUnitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitId, roomId);
    }
}
