package thesis.model.persistence.entities.embeddableIds;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RoomDistancePK implements Serializable {
    @Column(name = "room_id_1")
    private Integer roomId1;

    @Column(name = "room_id_2")
    private Integer roomId2;

    public RoomDistancePK() {}

    public RoomDistancePK(Integer roomId1, Integer roomId2) {
        this.roomId1 = roomId1;
        this.roomId2 = roomId2;
    }

    public Integer getRoomId1() {
        return roomId1;
    }

    public void setRoomId1(Integer roomId1) {
        this.roomId1 = roomId1;
    }

    public Integer getRoomId2() {
        return roomId2;
    }

    public void setRoomId2(Integer roomId2) {
        this.roomId2 = roomId2;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof RoomDistancePK)) return false;
        RoomDistancePK roomDistancePK = (RoomDistancePK) o;
        return Objects.equals(roomId1, roomDistancePK.roomId1) && Objects.equals(roomId2, roomDistancePK.roomId2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId1, roomId2);
    }
}
