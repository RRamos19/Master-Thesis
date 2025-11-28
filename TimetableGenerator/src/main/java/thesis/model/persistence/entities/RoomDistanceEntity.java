package thesis.model.persistence.entities;

import jakarta.persistence.*;
import thesis.model.persistence.entities.embeddableIds.RoomDistancePK;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "room_distance")
public class RoomDistanceEntity implements Serializable {
    @EmbeddedId
    private RoomDistancePK id = new RoomDistancePK();

    @ManyToOne
    @MapsId("roomId1")
    @JoinColumn(name = "room_id_1")
    private RoomEntity roomEntity1;

    @ManyToOne
    @MapsId("roomId2")
    @JoinColumn(name = "room_id_2")
    private RoomEntity roomEntity2;

    @Column(nullable = false)
    private int distance;

    public RoomDistanceEntity() {}

    public RoomDistanceEntity(RoomEntity roomEntity1, RoomEntity roomEntity2, int travelDistance) {
        this.roomEntity1 = roomEntity1;
        this.roomEntity2 = roomEntity2;
        this.distance = travelDistance;

        roomEntity1.addRoom1Distance(this);
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

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setRoom1(RoomEntity roomEntity1) {
        this.roomEntity1 = roomEntity1;
    }

    public RoomEntity getRoom2() {
        return roomEntity2;
    }

    public void setRoom2(RoomEntity roomEntity2) {
        this.roomEntity2 = roomEntity2;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RoomDistanceEntity)) return false;
        RoomDistanceEntity that = (RoomDistanceEntity) o;
        return distance == that.distance &&
                Objects.equals(roomEntity1, that.roomEntity1) &&
                Objects.equals(roomEntity2, that.roomEntity2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomEntity1, roomEntity2, distance);
    }
}
