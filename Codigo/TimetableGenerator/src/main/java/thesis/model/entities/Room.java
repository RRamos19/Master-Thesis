package thesis.model.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 6, unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "room1", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RoomDistance> room1DistanceList = new ArrayList<>();

    @OneToMany(mappedBy = "room2", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RoomDistance> room2DistanceList = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RoomUnavailability> roomUnavailabilityList = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ScheduledLesson> scheduledLessonList = new ArrayList<>();

    public Room() {}

    public Room(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addRoom1Distance(RoomDistance roomDistance) {
        room1DistanceList.add(roomDistance);
        roomDistance.setRoom1(this);
    }

    public void addRoom2Distance(RoomDistance roomDistance) {
        room2DistanceList.add(roomDistance);
        roomDistance.setRoom2(this);
    }

    public void addRoomUnavailability(RoomUnavailability roomUnavailability) {
        roomUnavailabilityList.add(roomUnavailability);
        roomUnavailability.setRoom(this);
    }

    public void addScheduledLesson(ScheduledLesson scheduledLesson) {
        scheduledLessonList.add(scheduledLesson);
        scheduledLesson.setRoom(this);
    }
}
