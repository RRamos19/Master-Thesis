package thesis.model.domain;

import thesis.model.domain.exceptions.CheckedIllegalArgumentException;

public class Time {
    private final byte days;
    private final int weeks;
    private final int startSlot;
    private final int length;

    public Time(byte days, int weeks, int startSlot, int length) {
        this.days = days;
        this.weeks = weeks;
        this.startSlot = startSlot;
        this.length = length;
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

    /**
     * Finds the most significant bit in a value.
     * The authors of this method are Edon Gashi and Kadri Sylejmani
     * source: https://github.com/edongashi/itc-2019
     * @param value The value from which we want to discover the most significant bit
     * @return Only the most significant bit
     */
    private int mostSignificantBit(int value) {
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        return value & ~(value >> 1);
    }

    public boolean isEarlier(Time other) {
        int msbWeeksThis = mostSignificantBit(this.weeks);
        int msbWeeksOther = mostSignificantBit(other.weeks);
        if(msbWeeksThis > msbWeeksOther) {
            return true;
        } else if(msbWeeksOther > msbWeeksThis) {
            return false;
        }

        int msbDaysThis = mostSignificantBit(this.days);
        int msbDaysOther = mostSignificantBit(other.days);
        if(msbDaysThis > msbDaysOther) {
            return true;
        } else if(msbDaysOther > msbDaysThis) {
            return false;
        }

        return this.startSlot + this.length < other.startSlot;
    }
}
