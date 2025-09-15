package thesis.model.domain.components;

import thesis.model.exceptions.CheckedIllegalArgumentException;

import java.lang.ref.SoftReference;
import java.util.*;

public class TimeFactory {
    // Static pool to avoid multiple clones of Time
    private static final Map<List<Integer>, SoftReference<Time>> timePool = new WeakHashMap<>();

    // Prevents instantiation
    private TimeFactory() {}

    public static Time create(String days, String weeks, int startSlot, int length) throws CheckedIllegalArgumentException {
        int daysLength = days.length();
        if(daysLength == 0 || daysLength > 7) {
            throw new CheckedIllegalArgumentException("The days string must have a length between 1 and 7 but it has a length of " + days.length());
        }
        if(weeks.isEmpty()) {
            throw new CheckedIllegalArgumentException("The weeks string must have a length bigger than 0");
        }

        return create(Short.parseShort(days, 2), Integer.parseInt(weeks, 2), startSlot, length);
    }

    public static Time create(short days, int weeks, int startSlot, int length) throws CheckedIllegalArgumentException {
        // days must have a lower value than the maximum value for 7 bits
        if(days <= 0) {
            throw new CheckedIllegalArgumentException("The days value must be between 1 and 127 but it has a value of " + days);
        }
        if(weeks <= 0) {
            throw new CheckedIllegalArgumentException("The weeks value must be bigger than 0 but it has a value of " + weeks);
        }

        List<Integer> key = Arrays.asList((int) days, weeks, startSlot, length);

        SoftReference<Time> ref = timePool.computeIfAbsent(key, t -> new SoftReference<>(new Time(days, weeks, startSlot, length)));
        return ref.get();
    }
}
