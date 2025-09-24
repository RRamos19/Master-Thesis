package thesis.model.persistence.entities;

import jakarta.persistence.*;
import thesis.model.persistence.entities.embeddableIds.ScheduledLessonPK;

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
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    private RoomEntity roomEntity;

    @ManyToOne
    @MapsId("timetableId")
    @JoinColumn(name = "timetable_id", referencedColumnName = "id", nullable = false)
    private TimetableEntity timetableEntity;

    @OneToMany(mappedBy = "scheduledLessonEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<ScheduledLessonTeacherEntity> scheduledLessonTeacherList = new ArrayList<>();

    @Column(nullable = false)
    private short days;

    @Column(nullable = false)
    private int weeks;

    @Column(name = "start_slot", nullable = false)
    private short startSlot;

    @Column(nullable = false)
    private short duration;

    public ScheduledLessonEntity() {}

    public ScheduledLessonEntity(TimetableEntity timetableEntity, ClassUnitEntity classUnitEntity, RoomEntity roomEntity, short days, int weeks, short startSlot, short duration) {
        this.id = new ScheduledLessonPK(timetableEntity.getId(), UUID.randomUUID());
        timetableEntity.addScheduledLesson(this);
        this.timetableEntity = timetableEntity;
        classUnitEntity.addScheduledLesson(this);
        this.classUnitEntity = classUnitEntity;
        if(roomEntity != null) {
            roomEntity.addScheduledLesson(this);
        }
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
        this.classUnitEntity = classUnitEntity;
    }

    public RoomEntity getRoom() {
        return roomEntity;
    }

    public void setRoom(RoomEntity roomEntity) {
        this.roomEntity = roomEntity;
    }

    public TimetableEntity getTimetable() {
        return timetableEntity;
    }

    public List<ScheduledLessonTeacherEntity> getScheduledLessonTeacherList() {
        return scheduledLessonTeacherList;
    }

    public void setTimetable(TimetableEntity timetableEntity) {
        this.timetableEntity = timetableEntity;
        this.id.setTimetableId(timetableEntity.getId());
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

    public void addScheduledLessonTeacherEntity(ScheduledLessonTeacherEntity scheduledLessonTeacherEntity) {
        scheduledLessonTeacherList.add(scheduledLessonTeacherEntity);
    }
}
