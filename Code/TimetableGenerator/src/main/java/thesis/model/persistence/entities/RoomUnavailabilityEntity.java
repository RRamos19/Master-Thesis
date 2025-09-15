package thesis.model.persistence.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "room_unavailability")
public class RoomUnavailabilityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    private RoomEntity roomEntity;

    @Column(length = 7, nullable = false)
    private String days;

    @Column(length = 16, nullable = false)
    private String weeks;

    @Column(name = "start_slot", nullable = false)
    private int startSlot;

    private int duration;

    public RoomUnavailabilityEntity() {}

    public RoomUnavailabilityEntity(RoomEntity roomEntity, String days, String weeks, int startSlot, int duration) {
        this.roomEntity = roomEntity;
        roomEntity.addRoomUnavailability(this);
        this.days = days;
        this.weeks = weeks;
        this.startSlot = startSlot;
        this.duration = duration;
    }

    public Integer getId() {
        return id;
    }

    public RoomEntity getRoom() {
        return roomEntity;
    }

    public void setRoom(RoomEntity roomEntity) {
        this.roomEntity = roomEntity;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getWeeks() {
        return weeks;
    }

    public void setWeeks(String weeks) {
        this.weeks = weeks;
    }

    public int getStartSlot() {
        return startSlot;
    }

    public void setStartSlot(int startSlot) {
        this.startSlot = startSlot;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
