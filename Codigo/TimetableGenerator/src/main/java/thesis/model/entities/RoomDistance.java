package thesis.model.entities;

import jakarta.persistence.*;
import thesis.model.entities.EmbeddableIds.RoomDistancePK;

@Entity
@Table(name = "room_distance")
public class RoomDistance {
    @EmbeddedId
    private RoomDistancePK id;

    @ManyToOne
    @MapsId("roomId1")
    @JoinColumn(name = "room_id_1", referencedColumnName = "id")
    private Room room1;

    @ManyToOne
    @MapsId("roomId2")
    @JoinColumn(name = "room_id_2", referencedColumnName = "id")
    private Room room2;

    public RoomDistance() {}

    public RoomDistance(Room room1, Room room2) {
        this.id = new RoomDistancePK(room1.getId(), room2.getId());
        this.room1 = room1;
        this.room2 = room2;
    }

    public RoomDistancePK getId() {
        return id;
    }

    public void setId(RoomDistancePK id) {
        this.id = id;
    }

    public Room getRoom1() {
        return room1;
    }

    public void setRoom1(Room room1) {
        this.room1 = room1;
        this.id.setRoomId1(room1.getId());
    }

    public Room getRoom2() {
        return room2;
    }

    public void setRoom2(Room room2) {
        this.room2 = room2;
        this.id.setRoomId2(room2.getId());
    }
}
