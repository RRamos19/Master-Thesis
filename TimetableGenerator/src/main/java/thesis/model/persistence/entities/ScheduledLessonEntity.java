package thesis.model.persistence.entities;

import jakarta.persistence.*;
import thesis.model.persistence.entities.embeddableIds.ScheduledLessonPK;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "scheduled_lesson")
public class ScheduledLessonEntity implements Serializable {
    @EmbeddedId
    private ScheduledLessonPK id = new ScheduledLessonPK();

    @ManyToOne
    @MapsId("classId")
    @JoinColumn(name = "class_id")
    private ClassUnitEntity classUnitEntity;

    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    private RoomEntity roomEntity;

    @ManyToOne
    @MapsId("timetableId")
    @JoinColumn(name = "timetable_id")
    private TimetableEntity timetableEntity;

    @ManyToOne
    @MapsId("timeBlockId")
    @JoinColumn(name = "time_block_id")
    private TimeBlockEntity timeBlockEntity;

    @OneToMany(mappedBy = "scheduledLessonEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<ScheduledLessonTeacherEntity> scheduledLessonTeacherList = new ArrayList<>();

    public ScheduledLessonEntity() {}

    public ScheduledLessonEntity(TimetableEntity timetableEntity, ClassUnitEntity classUnitEntity, RoomEntity roomEntity, TimeBlockEntity timeBlockEntity) {
        this.timetableEntity = timetableEntity;
        this.classUnitEntity = classUnitEntity;
        this.roomEntity = roomEntity;
        this.timeBlockEntity = timeBlockEntity;

        timetableEntity.addScheduledLesson(this);
    }

    public ScheduledLessonPK getId() {
        return id;
    }

    public void setId(ScheduledLessonPK id) {
        this.id = id;
    }

    public ClassUnitEntity getClassUnitEntity() {
        return classUnitEntity;
    }

    public void setClassUnitEntity(ClassUnitEntity classUnitEntity) {
        this.classUnitEntity = classUnitEntity;
    }

    public RoomEntity getRoomEntity() {
        return roomEntity;
    }

    public void setRoomEntity(RoomEntity roomEntity) {
        this.roomEntity = roomEntity;
    }

    public TimetableEntity getTimetableEntity() {
        return timetableEntity;
    }

    public void setTimetableEntity(TimetableEntity timetableEntity) {
        this.timetableEntity = timetableEntity;
    }

    public TimeBlockEntity getTimeBlockEntity() {
        return timeBlockEntity;
    }

    public void setTimeBlockEntity(TimeBlockEntity timeBlockEntity) {
        this.timeBlockEntity = timeBlockEntity;
    }

    public List<ScheduledLessonTeacherEntity> getScheduledLessonTeacherList() {
        return scheduledLessonTeacherList;
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
