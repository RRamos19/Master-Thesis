package thesis.model.entities.EmbeddableIds;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClassRoomPK implements Serializable {
    @Embedded
    private ClassUnitPK classUnitPK;

    @Column(name = "room_id")
    private int roomId;

    public ClassRoomPK() {}

    public ClassRoomPK(ClassUnitPK classUnitPK, int roomId) {
        this.classUnitPK = classUnitPK;
        this.roomId = roomId;
    }

    public ClassUnitPK getClassUnitPK() {
        return classUnitPK;
    }

    public void setClassUnitPK(ClassUnitPK classUnitPK) {
        this.classUnitPK = classUnitPK;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassRoomPK)) return false;
        ClassRoomPK that = (ClassRoomPK) o;
        return roomId == that.roomId && Objects.equals(classUnitPK, that.classUnitPK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitPK, roomId);
    }
}
