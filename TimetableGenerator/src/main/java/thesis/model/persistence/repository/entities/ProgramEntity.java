package thesis.model.persistence.repository.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "tb_program")
public class ProgramEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 30, unique = true, nullable = false)
    private String name;

    @Column(name = "number_days", nullable = false)
    private short numberDays;

    @Column(name = "number_weeks", nullable = false)
    private int numberWeeks;

    @Column(name = "slots_per_day", nullable = false)
    private short slotsPerDay;

    @Column(name = "time_weight", nullable = false)
    private short timeWeight;

    @Column(name = "room_weight", nullable = false)
    private short roomWeight;

    @Column(name = "distribution_weight", nullable = false)
    private short distributionWeight;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @OneToMany(mappedBy = "programEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<CourseEntity> courseEntitySet = new HashSet<>();

    @OneToMany(mappedBy = "programEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<TimetableEntity> timetableEntitySet = new HashSet<>();

    public ProgramEntity() {}

    public ProgramEntity(String name, short numberDays, int numberWeeks, short slotsPerDay, short timeWeight, short roomWeight, short distributionWeight, LocalDateTime lastUpdatedAt) {
        this.name = name;
        this.numberDays = numberDays;
        this.numberWeeks = numberWeeks;
        this.slotsPerDay = slotsPerDay;
        this.timeWeight = timeWeight;
        this.roomWeight = roomWeight;
        this.distributionWeight = distributionWeight;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getNumberDays() {
        return numberDays;
    }

    public void setNumberDays(short numberDays) {
        this.numberDays = numberDays;
    }

    public int getNumberWeeks() {
        return numberWeeks;
    }

    public void setNumberWeeks(int numberWeeks) {
        this.numberWeeks = numberWeeks;
    }

    public short getSlotsPerDay() {
        return slotsPerDay;
    }

    public void setSlotsPerDay(short slotsPerDay) {
        this.slotsPerDay = slotsPerDay;
    }

    public short getTimeWeight() {
        return timeWeight;
    }

    public void setTimeWeight(short timeWeight) {
        this.timeWeight = timeWeight;
    }

    public short getRoomWeight() {
        return roomWeight;
    }

    public void setRoomWeight(short roomWeight) {
        this.roomWeight = roomWeight;
    }

    public short getDistributionWeight() {
        return distributionWeight;
    }

    public void setDistributionWeight(short distributionWeight) {
        this.distributionWeight = distributionWeight;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public void addCourse(CourseEntity courseEntity) {
        courseEntitySet.add(courseEntity);
    }

    public Set<CourseEntity> getCourseEntitySet() {
        return courseEntitySet;
    }

    public void addTimetable(TimetableEntity timetableEntity) {
        timetableEntitySet.add(timetableEntity);
    }

    public Set<TimetableEntity> getTimetableEntitySet() {
        return timetableEntitySet;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProgramEntity)) return false;
        ProgramEntity that = (ProgramEntity) o;
        return numberDays == that.numberDays &&
                numberWeeks == that.numberWeeks &&
                slotsPerDay == that.slotsPerDay &&
                timeWeight == that.timeWeight &&
                roomWeight == that.roomWeight &&
                distributionWeight == that.distributionWeight &&
                Objects.equals(name, that.name) &&
                Objects.equals(lastUpdatedAt, that.lastUpdatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, numberDays, numberWeeks, slotsPerDay, timeWeight, roomWeight, distributionWeight, lastUpdatedAt);
    }
}
