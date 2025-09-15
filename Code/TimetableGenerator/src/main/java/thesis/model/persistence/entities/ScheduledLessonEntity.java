package thesis.model.persistence.entities;

import jakarta.persistence.*;
import thesis.model.persistence.entities.embeddableids.ScheduledLessonPK;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "scheduled_lesson")
public class ScheduledLessonEntity {
    @EmbeddedId
    private ScheduledLessonPK id;

    @ManyToOne
    @JoinColumn(name = "class_id", referencedColumnName = "id", nullable = false)
    private ClassUnitEntity classUnitEntity;

    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id", nullable = false)
    private RoomEntity roomEntity;

    @ManyToOne
    @MapsId("timetableId")
    @JoinColumn(name = "timetable_id", referencedColumnName = "id", nullable = false)
    private TimetableEntity timetableEntity;

    @OneToMany(mappedBy = "scheduledLessonEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<ScheduledLessonTeacherEntity> scheduledLessonTeacherList = new ArrayList<>();

    @Column(length = 7)
    private String days;

    @Column(length = 16)
    private String weeks;

    @Column(name = "start_slot")
    private int startSlot;

    private int duration;

    public ScheduledLessonEntity() {}

    public ScheduledLessonEntity(TimetableEntity timetableEntity, ClassUnitEntity classUnitEntity, RoomEntity roomEntity, String days, String weeks, int startSlot, int duration) {
        this.id = new ScheduledLessonPK(timetableEntity.getId(), UUID.randomUUID());
        timetableEntity.addScheduledLesson(this);
        this.timetableEntity = timetableEntity;
        classUnitEntity.addScheduledLesson(this);
        this.classUnitEntity = classUnitEntity;
        roomEntity.addScheduledLesson(this);
        this.roomEntity = roomEntity;
        this.days = days;
        this.weeks = weeks;
        this.startSlot = startSlot;
        this.duration = duration;
    }

    public ScheduledLessonPK getId() {
        return id;
    }

    public ClassUnitEntity getClassUnit() {
        return classUnitEntity;
    }

    public void setClassUnit(ClassUnitEntity classUnitEntity) {
        classUnitEntity.addScheduledLesson(this);
        this.classUnitEntity = classUnitEntity;
    }

    public RoomEntity getRoom() {
        return roomEntity;
    }

    public void setRoom(RoomEntity roomEntity) {
        roomEntity.addScheduledLesson(this);
        this.roomEntity = roomEntity;
    }

    public TimetableEntity getTimetable() {
        return timetableEntity;
    }

    public List<ScheduledLessonTeacherEntity> getScheduledLessonTeacherList() {
        return scheduledLessonTeacherList;
    }

    public void setTimetable(TimetableEntity timetableEntity) {
        timetableEntity.addScheduledLesson(this);
        this.timetableEntity = timetableEntity;
        this.id.setTimetableId(timetableEntity.getId());
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

    public void addScheduledLessonTeacherEntity(ScheduledLessonTeacherEntity scheduledLessonTeacherEntity) {
        scheduledLessonTeacherList.add(scheduledLessonTeacherEntity);
    }
}
