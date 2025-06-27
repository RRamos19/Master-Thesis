package thesis.model.persistence.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "optimization_parameters")
public class OptimizationParametersEntity {
    @Id
    private int id;

    @Column(name = "time_weight")
    private short timeWeight;

    @Column(name = "room_weight")
    private short roomWeight;

    @Column(name = "distribution_weight")
    private short distributionWeight;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public OptimizationParametersEntity() {}

    public OptimizationParametersEntity(short timeWeight, short roomWeight, short distributionWeight) {
        this.id = 0;
        this.timeWeight = timeWeight;
        this.roomWeight = roomWeight;
        this.distributionWeight = distributionWeight;
    }

    public int getId() {
        return id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
