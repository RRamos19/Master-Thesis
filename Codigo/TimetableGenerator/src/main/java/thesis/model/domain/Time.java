package thesis.model.domain;

import thesis.utils.BitToolkit;

import java.util.Objects;

public class Time {
    // Days and weeks can be stored in smaller types, but doing it this way we can avoid problems
    // with signed values
    private final short days;
    private final int weeks;
    private final int startSlot;
    private final int length;
    private final int endSlot;

    public Time(short days, int weeks, int startSlot, int length) {
        this.days = days;
        this.weeks = weeks;
        this.startSlot = startSlot;
        this.length = length;
        this.endSlot = startSlot + length;
    }

    public short getDays() {
        return days;
    }

    public int getWeeks() {
        return weeks;
    }

    public int getStartSlot() {
        return startSlot;
    }

    public int getLength() {
        return length;
    }

    public int getEndSlot() {
        return endSlot;
    }

    /**
     * Checks if this time block is earlier than another time block.
     * The authors of this method are Edon Gashi and Kadri Sylejmani
     * source: https://github.com/edongashi/itc-2019
     * @param other Another time block of which is to be compared with this one
     * @return True if this time block is earlier than the other one, false otherwise
     */
    public boolean isEarlier(Time other) {
        int msbWeeksThis = BitToolkit.mostSignificantBit(this.weeks);
        int msbWeeksOther = BitToolkit.mostSignificantBit(other.weeks);
        if(msbWeeksThis > msbWeeksOther) {
            return true;
        } else if(msbWeeksOther > msbWeeksThis) {
            return false;
        }

        int msbDaysThis = BitToolkit.mostSignificantBit(this.days);
        int msbDaysOther = BitToolkit.mostSignificantBit(other.days);
        if(msbDaysThis > msbDaysOther) {
            return true;
        } else if(msbDaysOther > msbDaysThis) {
            return false;
        }

        return this.endSlot < other.startSlot;
    }

    /**
     * Checks if there is an overlap between this time block and another time block.
     * The authors of this method are Edon Gashi and Kadri Sylejmani
     * source: https://github.com/edongashi/itc-2019
     * @param other Another time block of which the overlap is to be checked
     * @return True if there is an overlap, false otherwise
     */
    public boolean overlaps(Time other) {
        return other.startSlot < this.endSlot &&
            this.startSlot < other.endSlot &&
            (this.days & other.days) != 0 &&
            (this.weeks & other.weeks) != 0;
    }

    public String toString(byte numDays, short numWeeks) {
        return "days=\"" + BitToolkit.createSpecificSizeBinaryString(numDays, days) + "\" " +
                "start=\"" + startSlot + "\" " +
                "length=\"" + length + "\" " +
                "weeks=\"" + BitToolkit.createSpecificSizeBinaryString(numWeeks, weeks) + "\"";
    }

    @Override
    public String toString() {
        return "days=\"" + Integer.toBinaryString(days) + "\" " +
                "start=\"" + startSlot + "\" " +
                "length=\"" + length + "\" " +
                "weeks=\"" + Integer.toBinaryString(weeks) + "\"";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Time)) return false;
        Time time = (Time) o;
        return days == time.days &&
                weeks == time.weeks &&
                startSlot == time.startSlot &&
                length == time.length &&
                endSlot == time.endSlot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(days, weeks, startSlot, length, endSlot);
    }
}
