package thesis.model.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "optimization_parameters")
public class OptimizationParameters {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "time_weight")
    private int timeWeight;

    @Column(name = "room_weight")
    private int roomWeight;

    @Column(name = "distribution_weight")
    private int distributionWeight;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public OptimizationParameters() {}

    public OptimizationParameters(int timeWeight, int roomWeight, int distributionWeight) {
        this.timeWeight = timeWeight;
        this.roomWeight = roomWeight;
        this.distributionWeight = distributionWeight;
    }

    public int getId() {
        return id;
    }

    public int getTimeWeight() {
        return timeWeight;
    }

    public void setTimeWeight(int timeWeight) {
        this.timeWeight = timeWeight;
    }

    public int getRoomWeight() {
        return roomWeight;
    }

    public void setRoomWeight(int roomWeight) {
        this.roomWeight = roomWeight;
    }

    public int getDistributionWeight() {
        return distributionWeight;
    }

    public void setDistributionWeight(int distributionWeight) {
        this.distributionWeight = distributionWeight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
