package thesis.model.entities;

import jakarta.persistence.*;
import thesis.model.entities.EmbeddableIds.ScheduledLessonPK;

import java.util.UUID;

@Entity
@Table(name = "scheduled_lesson")
public class ScheduledLesson {
    @EmbeddedId
    private ScheduledLessonPK id;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false),
            @JoinColumn(name = "config_id", referencedColumnName = "config_id", nullable = false),
            @JoinColumn(name = "subpart_id", referencedColumnName = "subpart_id", nullable = false),
            @JoinColumn(name = "class_id", referencedColumnName = "class_id", nullable = false)
    })
    private ClassUnit classUnit;

    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id", nullable = false)
    private Room room;

    @ManyToOne
    @MapsId("timetableId")
    @JoinColumn(name = "timetable_id", referencedColumnName = "id", nullable = false)
    private Timetable timetable;

    @Column(length = 7)
    private String days;

    @Column(length = 16)
    private String weeks;

    @Column(name = "start_slot")
    private int startSlot;

    private int duration;

    public ScheduledLesson() {}

    public ScheduledLesson(Timetable timetable, ClassUnit classUnit, Room room, String days, String weeks, int startSlot, int duration) {
        this.id = new ScheduledLessonPK(timetable.getId(), UUID.randomUUID());
        this.timetable = timetable;
        this.classUnit = classUnit;
        this.room = room;
        this.days = days;
        this.weeks = weeks;
        this.startSlot = startSlot;
        this.duration = duration;
    }

    public ScheduledLessonPK getId() {
        return id;
    }

    public ClassUnit getClassUnit() {
        return classUnit;
    }

    public void setClassUnit(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Timetable getTimetable() {
        return timetable;
    }

    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
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
