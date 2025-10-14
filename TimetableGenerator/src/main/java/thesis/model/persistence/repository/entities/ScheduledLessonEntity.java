package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;
import thesis.model.persistence.repository.entities.embeddableIds.ScheduledLessonPK;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "scheduled_lesson")
public class ScheduledLessonEntity {
    @EmbeddedId
    private ScheduledLessonPK id;

    @ManyToOne
    @MapsId("classId")
    @JoinColumn(name = "class_id", referencedColumnName = "id", nullable = false)
    private ClassUnitEntity classUnitEntity;

    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    private RoomEntity roomEntity;

    @ManyToOne
    @MapsId("timetableId")
    @JoinColumn(name = "timetable_id", referencedColumnName = "id", nullable = false)
    private TimetableEntity timetableEntity;

    @ManyToOne
    @MapsId("timeBlockId")
    @JoinColumn(name = "time_block_id", referencedColumnName = "id", nullable = false)
    private TimeBlockEntity timeBlockEntity;

    @OneToMany(mappedBy = "scheduledLessonEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<ScheduledLessonTeacherEntity> scheduledLessonTeacherList = new ArrayList<>();

    public ScheduledLessonEntity() {}

    public ScheduledLessonEntity(TimetableEntity timetableEntity, ClassUnitEntity classUnitEntity, RoomEntity roomEntity, TimeBlockEntity timeBlockEntity) {
        this.id = new ScheduledLessonPK(timetableEntity.getId(), classUnitEntity.getId(), timeBlockEntity.getId());
        timetableEntity.addScheduledLesson(this);
        this.timetableEntity = timetableEntity;
        this.classUnitEntity = classUnitEntity;
        this.roomEntity = roomEntity;
        this.timeBlockEntity = timeBlockEntity;
    }

    public ScheduledLessonPK getId() {
        return id;
    }

    public void setId(ScheduledLessonPK id) {
        this.id = id;
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
        return timeBlockEntity.getDays();
    }

    public int getWeeks() {
        return timeBlockEntity.getWeeks();
    }

    public short getStartSlot() {
        return timeBlockEntity.getStartSlot();
    }

    public short getDuration() {
        return timeBlockEntity.getDuration();
    }

    public void addScheduledLessonTeacherEntity(ScheduledLessonTeacherEntity scheduledLessonTeacherEntity) {
        scheduledLessonTeacherList.add(scheduledLessonTeacherEntity);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ScheduledLessonEntity)) return false;
        ScheduledLessonEntity that = (ScheduledLessonEntity) o;
        return Objects.equals(classUnitEntity, that.classUnitEntity) &&
                Objects.equals(roomEntity, that.roomEntity) &&
                Objects.equals(timetableEntity, that.timetableEntity) &&
                Objects.equals(timeBlockEntity, that.timeBlockEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUnitEntity, roomEntity, timetableEntity, timeBlockEntity);
    }
}
