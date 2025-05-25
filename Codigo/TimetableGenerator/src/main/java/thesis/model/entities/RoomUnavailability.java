package thesis.model.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "room_unavailability")
public class RoomUnavailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    private Room room;

    @Column(length = 7, nullable = false)
    private String days;

    @Column(length = 16, nullable = false)
    private String weeks;

    @Column(name = "start_slot", nullable = false)
    private int startSlot;

    private int duration;

    public RoomUnavailability() {}

    public RoomUnavailability(Room room, String days, String weeks, int startSlot, int duration) {
        this.room = room;
        this.days = days;
        this.weeks = weeks;
        this.startSlot = startSlot;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
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
