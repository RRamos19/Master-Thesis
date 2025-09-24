package thesis.model.persistence.entities;

import java.time.LocalDateTime;
import java.util.*;

import jakarta.persistence.*;

@Entity
@Table(name = "timetable")
public class TimetableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private LocalDateTime creationDate; // Value created by the database

    @ManyToOne
    @JoinColumn(name = "program_id", referencedColumnName = "id", nullable = false)
    private ProgramEntity programEntity;

    @OneToMany(mappedBy = "timetableEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<ScheduledLessonEntity> scheduledLessonEntityList = new ArrayList<>();

    public TimetableEntity() {}

    public TimetableEntity(ProgramEntity programEntity){
        this.programEntity = programEntity;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public ProgramEntity getProgramEntity() {
        return programEntity;
    }

    public List<ScheduledLessonEntity> getScheduledLessonEntityList() {
        return scheduledLessonEntityList;
    }

    public void setProgramEntity(ProgramEntity programEntity) {
        this.programEntity = programEntity;
    }

    public void addScheduledLesson(ScheduledLessonEntity scheduledLessonEntity) {
        scheduledLessonEntityList.add(scheduledLessonEntity);
        scheduledLessonEntity.setTimetable(this);
    }
}
