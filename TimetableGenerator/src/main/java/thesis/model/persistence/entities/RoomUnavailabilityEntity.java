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

    @Column(nullable = false)
    private short days;

    @Column(nullable = false)
    private int weeks;

    @Column(name = "start_slot", nullable = false)
    private short startSlot;

    @Column(nullable = false)
    private short duration;

    public RoomUnavailabilityEntity() {}

    public RoomUnavailabilityEntity(RoomEntity roomEntity, short days, int weeks, short startSlot, short duration) {
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

    public short getDays() {
        return days;
    }

    public void setDays(short days) {
        this.days = days;
    }

    public int getWeeks() {
        return weeks;
    }

    public void setWeeks(int weeks) {
        this.weeks = weeks;
    }

    public short getStartSlot() {
        return startSlot;
    }

    public void setStartSlot(short startSlot) {
        this.startSlot = startSlot;
    }

    public short getDuration() {
        return duration;
    }

    public void setDuration(short duration) {
        this.duration = duration;
    }
}
