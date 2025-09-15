package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "program")
public class ProgramEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 30, unique = true, nullable = false)
    private String name;

    @Column(name = "number_days")
    private byte numberDays;

    @Column(name = "number_weeks")
    private short numberWeeks;

    @Column(name = "slots_per_day")
    private int slotsPerDay;

    @Column(name = "time_weight")
    private short timeWeight;

    @Column(name = "room_weight")
    private short roomWeight;

    @Column(name = "distribution_weight")
    private short distributionWeight;

    @OneToMany(mappedBy = "programEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<CourseEntity> courseEntityList = new ArrayList<>();

    public ProgramEntity() {}

    public ProgramEntity(String name) {
        this.name = name;
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

    public byte getNumberDays() {
        return numberDays;
    }

    public void setNumberDays(byte numberDays) {
        this.numberDays = numberDays;
    }

    public short getNumberWeeks() {
        return numberWeeks;
    }

    public void setNumberWeeks(short numberWeeks) {
        this.numberWeeks = numberWeeks;
    }

    public int getSlotsPerDay() {
        return slotsPerDay;
    }

    public void setSlotsPerDay(int slotsPerDay) {
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
