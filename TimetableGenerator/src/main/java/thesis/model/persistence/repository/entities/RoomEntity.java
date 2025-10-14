package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "room")
public class RoomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 6, unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "roomEntity1", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<RoomDistanceEntity> room1DistanceSet = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "room_unavailability",
            joinColumns = { @JoinColumn(name = "room_id") },
            inverseJoinColumns = { @JoinColumn(name = "time_block_id") }
    )
    private final Set<TimeBlockEntity> roomUnavailabilityEntitySet = new HashSet<>();

    public RoomEntity() {}

    public RoomEntity(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<RoomDistanceEntity> getRoom1DistanceSet() {
        return room1DistanceSet;
    }

    public Set<TimeBlockEntity> getRoomUnavailabilityList() {
        return roomUnavailabilityEntitySet;
    }

    public void addRoom1Distance(RoomDistanceEntity roomDistanceEntity) {
        room1DistanceSet.add(roomDistanceEntity);
    }

    public void addRoomUnavailability(TimeBlockEntity roomUnavailability) {
        roomUnavailabilityEntitySet.add(roomUnavailability);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RoomEntity)) return false;
        RoomEntity that = (RoomEntity) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
