package thesis.model.entities.EmbeddableIds;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RoomDistancePK implements Serializable {
    @Column(name = "room_id_1")
    private int roomId1;

    @Column(name = "room_id_2")
    private int roomId2;

    public RoomDistancePK() {}

    public RoomDistancePK(int roomId1, int roomId2) {
        this.roomId1 = roomId1;
        this.roomId2 = roomId2;
    }

    public int getRoomId1() {
        return roomId1;
    }

    public void setRoomId1(int roomId1) {
        this.roomId1 = roomId1;
    }

    public int getRoomId2() {
        return roomId2;
    }

    public void setRoomId2(int roomId2) {
        this.roomId2 = roomId2;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof RoomDistancePK)) return false;
        RoomDistancePK roomDistancePK = (RoomDistancePK) o;
        return roomId1 == roomDistancePK.roomId1 && roomId2 == roomDistancePK.roomId2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId1, roomId2);
    }
}
