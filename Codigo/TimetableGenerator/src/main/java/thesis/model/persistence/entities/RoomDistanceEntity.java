package thesis.model.persistence.entities;

import jakarta.persistence.*;
import thesis.model.persistence.entities.embeddableids.RoomDistancePK;

@Entity
@Table(name = "room_distance")
public class RoomDistanceEntity {
    @EmbeddedId
    private RoomDistancePK id;

    @ManyToOne
    @MapsId("roomId1")
    @JoinColumn(name = "room_id_1", referencedColumnName = "id")
    private RoomEntity roomEntity1;

    @ManyToOne
    @MapsId("roomId2")
    @JoinColumn(name = "room_id_2", referencedColumnName = "id")
    private RoomEntity roomEntity2;

    @Column(nullable = false)
    private int distance;

    public RoomDistanceEntity() {}

    public RoomDistanceEntity(RoomEntity roomEntity1, RoomEntity roomEntity2, int travelDistance) {
        this.id = new RoomDistancePK(roomEntity1.getId(), roomEntity2.getId());
        roomEntity1.addRoom1Distance(this);
        roomEntity2.addRoom2Distance(this);
        this.roomEntity1 = roomEntity1;
        this.roomEntity2 = roomEntity2;
        this.distance = travelDistance;
    }

    public RoomDistancePK getId() {
        return id;
    }

    public void setId(RoomDistancePK id) {
        this.id = id;
    }

    public RoomEntity getRoom1() {
        return roomEntity1;
    }

    public int getDistance() {
        return distance;
    }

    public void setRoom1(RoomEntity roomEntity1) {
        this.roomEntity1 = roomEntity1;
        this.id.setRoomId1(roomEntity1.getId());
    }

    public RoomEntity getRoom2() {
        return roomEntity2;
    }

    public void setRoom2(RoomEntity roomEntity2) {
        this.roomEntity2 = roomEntity2;
        this.id.setRoomId2(roomEntity2.getId());
    }
}
