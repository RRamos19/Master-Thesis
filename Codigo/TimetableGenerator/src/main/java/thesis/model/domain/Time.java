package thesis.model.domain;

public class Time {
    private final String days;
    private final String weeks;
    private final int startSlot;
    private final int length;

    public Time(String days, String weeks, int startSlot, int length) {
        this.days = days;
        this.weeks = weeks;
        this.startSlot = startSlot;
        this.length = length;
    }

    public String getDays() {
        return days;
    }

    public String getWeeks() {
        return weeks;
    }

    public int getStartSlot() {
        return startSlot;
    }

    public int getLength() {
        return length;
    }

    public boolean checkOverlap(Time other) {
        return this.startSlot < other.startSlot + other.length &&
                other.startSlot < this.startSlot + this.length;
    }
}
