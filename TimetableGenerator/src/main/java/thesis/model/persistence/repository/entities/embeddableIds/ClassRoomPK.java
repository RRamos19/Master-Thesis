package thesis.model.persistence.repository.entities.embeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClassRoomPK implements Serializable {
    @Column(name = "class_id")
    private Integer classUnitPK;

    @Column(name = "room_id")
    private Integer roomId;

    public ClassRoomPK() {}

    public ClassRoomPK(Integer classUnitPK, Integer roomId) {
        this.classUnitPK = classUnitPK;
        this.roomId = roomId;
    }

    public Integer getClassUnitPK() {
        return classUnitPK;
    }

    public void setClassUnitPK(int classUnitPK) {
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
        return Objects.equals(roomId, that.roomId) &&
                Objects.equals(classUnitPK, that.classUnitPK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitPK, roomId);
    }
}
