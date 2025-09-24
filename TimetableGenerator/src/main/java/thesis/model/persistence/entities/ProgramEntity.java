package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_program")
public class ProgramEntity {
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

    @OneToMany(mappedBy = "programEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<CourseEntity> courseEntityList = new ArrayList<>();

    public ProgramEntity() {}

    public ProgramEntity(String name, short numberDays, int numberWeeks, short slotsPerDay, short timeWeight, short roomWeight, short distributionWeight) {
        this.name = name;
        this.numberDays = numberDays;
        this.numberWeeks = numberWeeks;
        this.slotsPerDay = slotsPerDay;
        this.timeWeight = timeWeight;
        this.roomWeight = roomWeight;
        this.distributionWeight = distributionWeight;
    }

    public Integer getId() {
        return id;
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

    public void setNumberDays(byte numberDays) {
        this.numberDays = numberDays;
    }

    public int getNumberWeeks() {
        return numberWeeks;
    }

    public void setNumberWeeks(short numberWeeks) {
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

    public void addCourse(CourseEntity courseEntity) {
        courseEntityList.add(courseEntity);
    }
}
