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

    @Column(length = 10, nullable = false)
    private String program;

    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ScheduledLessonEntity> scheduledLessonEntityList = new ArrayList<>();

    public TimetableEntity() {}

    public TimetableEntity(String program){
        this.program = program;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public void addScheduledLesson(ScheduledLessonEntity scheduledLessonEntity) {
        scheduledLessonEntityList.add(scheduledLessonEntity);
        scheduledLessonEntity.setTimetable(this);
    }
}
