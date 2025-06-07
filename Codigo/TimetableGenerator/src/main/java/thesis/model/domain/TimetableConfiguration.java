package thesis.model.domain;

public class TimetableConfiguration {
    private int timeWeight;
    private int roomWeight;
    private int distribWeight;
    private int numDays;
    private int numWeeks;
    private int slotPerDay;

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

    public int getDistribWeight() {
        return distribWeight;
    }

    public void setDistribWeight(int distribWeight) {
        this.distribWeight = distribWeight;
    }

    public int getNumDays() {
        return numDays;
    }

    public void setNumDays(int numDays) {
        this.numDays = numDays;
    }

    public int getNumWeeks() {
        return numWeeks;
    }

    public void setNumWeeks(int numWeeks) {
        this.numWeeks = numWeeks;
    }

    public int getSlotPerDay() {
        return slotPerDay;
    }

    public void setSlotPerDay(int slotPerDay) {
        this.slotPerDay = slotPerDay;
    }
}
