package thesis.model.domain;

public class TimetableConfiguration {
    private short timeWeight;
    private short roomWeight;
    private short distribWeight;
    private short numDays;
    private int numWeeks;
    private int slotsPerDay;

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

    public short getDistribWeight() {
        return distribWeight;
    }

    public void setDistribWeight(short distribWeight) {
        this.distribWeight = distribWeight;
    }

    public int getNumDays() {
        return numDays;
    }

    public void setNumDays(short numDays) {
        this.numDays = numDays;
    }

    public int getNumWeeks() {
        return numWeeks;
    }

    public void setNumWeeks(int numWeeks) {
        this.numWeeks = numWeeks;
    }

    public int getSlotsPerDay() {
        return slotsPerDay;
    }

    public void setSlotsPerDay(int slotsPerDay) {
        this.slotsPerDay = slotsPerDay;
    }
}
