package thesis.model.domain.components;

import java.util.List;

public class TimetableConfiguration {
    private short timeWeight;
    private short roomWeight;
    private short distribWeight;
    private short numDays;
    private int numWeeks;
    private short slotsPerDay;

    public TimetableConfiguration(short numDays, int numWeeks, short slotsPerDay, short timeWeight, short roomWeight, short distribWeight) {
        this.numDays = numDays;
        this.numWeeks = numWeeks;
        this.slotsPerDay = slotsPerDay;
        this.timeWeight = timeWeight;
        this.roomWeight = roomWeight;
        this.distribWeight = distribWeight;
    }

    public TimetableConfiguration() {}

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

    public short getNumDays() {
        return numDays;
    }

    public void setNumDays(byte numDays) {
        this.numDays = numDays;
    }

    public int getNumWeeks() {
        return numWeeks;
    }

    public void setNumWeeks(int numWeeks) {
        this.numWeeks = numWeeks;
    }

    public short getSlotsPerDay() {
        return slotsPerDay;
    }

    public void setSlotsPerDay(short slotsPerDay) {
        this.slotsPerDay = slotsPerDay;
    }
}
