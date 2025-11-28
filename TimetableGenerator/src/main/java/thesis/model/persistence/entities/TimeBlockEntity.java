package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "time_block")
public class TimeBlockEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "start_slot", nullable = false)
    private Short startSlot;

    @Column(nullable = false)
    private Short duration;

    @Column(nullable = false)
    private Short days;

    @Column(nullable = false)
    private Integer weeks;

    public TimeBlockEntity() {}

    public TimeBlockEntity(short startSlot, short duration, short days, int weeks) {
        this.startSlot = startSlot;
        this.duration = duration;
        this.days = days;
        this.weeks = weeks;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Short getStartSlot() {
        return startSlot;
    }

    public Short getDuration() {
        return duration;
    }

    public Short getDays() {
        return days;
    }

    public Integer getWeeks() {
        return weeks;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TimeBlockEntity)) return false;
        TimeBlockEntity that = (TimeBlockEntity) o;
        return Objects.equals(startSlot, that.startSlot) &&
                Objects.equals(duration, that.duration) &&
                Objects.equals(days, that.days) &&
                Objects.equals(weeks, that.weeks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startSlot, duration, days, weeks);
    }
}
