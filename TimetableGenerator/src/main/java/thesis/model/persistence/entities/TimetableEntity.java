package thesis.model.persistence.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import jakarta.persistence.*;

@Entity
@Table(name = "timetable")
public class TimetableEntity implements Serializable {
    @Id
    private UUID id;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "program_id", referencedColumnName = "id", nullable = false)
    private ProgramEntity programEntity;

    @OneToMany(mappedBy = "timetableEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<ScheduledLessonEntity> scheduledLessonEntityList = new ArrayList<>();

    public TimetableEntity() {}

    public TimetableEntity(UUID id, ProgramEntity programEntity, LocalDateTime creationDate){
        this.id = id;
        this.programEntity = programEntity;
        this.creationDate = creationDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setProgramEntity(ProgramEntity programEntity) {
        this.programEntity = programEntity;
    }

    public ProgramEntity getProgramEntity() {
        return programEntity;
    }

    public List<ScheduledLessonEntity> getScheduledLessonEntityList() {
        return scheduledLessonEntityList;
    }

    public void addScheduledLesson(ScheduledLessonEntity scheduledLessonEntity) {
        scheduledLessonEntityList.add(scheduledLessonEntity);
        scheduledLessonEntity.setTimetableEntity(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TimetableEntity)) return false;
        TimetableEntity that = (TimetableEntity) o;
        return Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(programEntity, that.programEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creationDate, programEntity);
    }
}
