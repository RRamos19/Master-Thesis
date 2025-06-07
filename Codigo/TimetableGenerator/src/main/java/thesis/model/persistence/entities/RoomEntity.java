package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room")
public class RoomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 6, unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "room1", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RoomDistance> room1DistanceList = new ArrayList<>();

    @OneToMany(mappedBy = "room2", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RoomDistance> room2DistanceList = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RoomUnavailabilityEntity> roomUnavailabilityEntityList = new ArrayList<>();

    @OneToMany(mappedBy = "room", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ScheduledLessonEntity> scheduledLessonEntityList = new ArrayList<>();

    @OneToMany(mappedBy = "room", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ClassRoomEntity> classRoomEntityList = new ArrayList<>();

    public RoomEntity() {}

    public RoomEntity(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<RoomDistance> getRoom1DistanceList() {
        return room1DistanceList;
    }

    public List<RoomDistance> getRoom2DistanceList() {
        return room2DistanceList;
    }

    public List<RoomUnavailabilityEntity> getRoomUnavailabilityList() {
        return roomUnavailabilityEntityList;
    }

    public List<ScheduledLessonEntity> getScheduledLessonList() {
        return scheduledLessonEntityList;
    }

    public void addRoom1Distance(RoomDistance roomDistance) {
        room1DistanceList.add(roomDistance);
        roomDistance.setRoom1(this);
    }

    public void addRoom2Distance(RoomDistance roomDistance) {
        room2DistanceList.add(roomDistance);
        roomDistance.setRoom2(this);
    }

    public void addRoomUnavailability(RoomUnavailabilityEntity roomUnavailabilityEntity) {
        roomUnavailabilityEntityList.add(roomUnavailabilityEntity);
        roomUnavailabilityEntity.setRoom(this);
    }

    public void addScheduledLesson(ScheduledLessonEntity scheduledLessonEntity) {
        scheduledLessonEntityList.add(scheduledLessonEntity);
        scheduledLessonEntity.setRoom(this);
    }

    public void addClassRoom(ClassRoomEntity cr) {
        this.classRoomEntityList.add(cr);
    }
}
