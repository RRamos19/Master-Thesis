package thesis.model.domain.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.model.exceptions.CheckedIllegalArgumentException;

import java.lang.ref.SoftReference;
import java.util.*;

/**
 * Creates time objects when its the first time they are created. If an equal object has been created before, that same object is returned instead.
 * Has the objective of reusing objects to reduce memory consumption.
 */
public class TimeFactory {
    private static final Logger logger = LoggerFactory.getLogger(TimeFactory.class);
    // Static pool to avoid multiple clones of Time
    private static final Map<List<Integer>, SoftReference<Time>> timePool = new WeakHashMap<>();

    // Prevents instantiation
    private TimeFactory() {}

    public static Time create(String days, String weeks, short startSlot, short length) throws CheckedIllegalArgumentException {
        int daysLength = days.length();
        if(daysLength == 0 || daysLength > 7) {
            String message = "The days string must have a length between 1 and 7 but it has a length of " + days.length();
            logger.error(message);
            throw new CheckedIllegalArgumentException(message);
        }
        if(weeks.isEmpty()) {
            String message = "The weeks string must have a length bigger than 0";
            logger.error(message);
            throw new CheckedIllegalArgumentException(message);
        }

        return create(Short.parseShort(days, 2), Integer.parseInt(weeks, 2), startSlot, length);
    }

    public static Time create(short days, int weeks, short startSlot, short length) throws CheckedIllegalArgumentException {
        // days must have a lower value than the maximum value for 7 bits
        if(days <= 0) {
            String message = "The days value must be between 1 and 127 but it has a value of " + days;
            logger.error(message);
            throw new CheckedIllegalArgumentException(message);
        }
        if(weeks <= 0) {
            String message = "The weeks value must be bigger than 0 but it has a value of " + weeks;
            logger.error(message);
            throw new CheckedIllegalArgumentException(message);
        }

        List<Integer> key = List.of((int) days, weeks, (int) startSlot, (int) length);

        SoftReference<Time> ref = timePool.computeIfAbsent(key, t -> new SoftReference<>(new Time(days, weeks, startSlot, length)));
        return ref.get();
    }
}
