package thesis.model.domain.components;

import java.util.List;

public class TimetableConfiguration implements TableDisplayable {
    private short timeWeight;
    private short roomWeight;
    private short distribWeight;
    private short numDays;
    private int numWeeks;
    private short slotsPerDay;

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

    @Override
    public String getTableName() {
        return "Configuration";
    }

    @Override
    public List<String> getColumnNames() {
        return List.of("Nº of days", "Nº of weeks", "Slots per day", "Time Weight", "Room Weight", "Distribution Weight");
    }

    @Override
    public List<Object> getColumnValues() {
        return List.of(numDays, numWeeks, slotsPerDay, timeWeight, roomWeight, distribWeight);
    }

    @Override
    public boolean isOptimizable() {
        return false;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }
}
