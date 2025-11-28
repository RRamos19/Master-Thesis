package thesis.model.domain.components;

public class PenaltySum {
    private final int timePenalty;
    private final int roomPenalty;
    private final int commonPenalty;
    private final int totalPenalty;

    public PenaltySum(int roomPenalty, int timePenalty, int commonPenalty) {
        this.roomPenalty = roomPenalty;
        this.timePenalty = timePenalty;
        this.commonPenalty = commonPenalty;
        this.totalPenalty = roomPenalty + timePenalty + commonPenalty;
    }

    public PenaltySum(int roomPenalty, int timePenalty) {
        this(roomPenalty, timePenalty, 0);
    }

    public int getTimePenalty() {
        return timePenalty;
    }

    public int getRoomPenalty() {
        return roomPenalty;
    }

    public int getCommonPenalty() {
        return commonPenalty;
    }

    public int getTotalPenalty() {
        return totalPenalty;
    }
}
