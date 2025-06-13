package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuration")
public class ConfigurationEntity {
    @Id
    private int id;

    @Column(name = "number_days")
    private int numberDays;

    @Column(name = "number_weeks")
    private int numberWeeks;

    @Column(name = "slots_per_day")
    private int slotsPerDay;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public ConfigurationEntity() {}

    public ConfigurationEntity(int numberDays, int numberWeeks, int slotsPerDay) {
        this.id = 0;
        this.numberDays = numberDays;
        this.numberWeeks = numberWeeks;
        this.slotsPerDay = slotsPerDay;
    }

    public int getId() {
        return id;
    }

    public int getNumberDays() {
        return numberDays;
    }

    public void setNumberDays(int numberDays) {
        this.numberDays = numberDays;
    }

    public int getNumberWeeks() {
        return numberWeeks;
    }

    public void setNumberWeeks(int numberWeeks) {
        this.numberWeeks = numberWeeks;
    }

    public int getSlotsPerDay() {
        return slotsPerDay;
    }

    public void setSlotsPerDay(int slotsPerDay) {
        this.slotsPerDay = slotsPerDay;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
