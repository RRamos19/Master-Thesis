package thesis.model.domain;

import org.junit.jupiter.api.Test;
import thesis.model.domain.components.Time;
import thesis.model.domain.components.TimeFactory;
import thesis.model.exceptions.CheckedIllegalArgumentException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeTest {
    @Test
    public void testRelationsBetweenTimeBlocks() throws CheckedIllegalArgumentException {
        String days = "0010100";
        String weeks = "0011111111";
        Time time1 = TimeFactory.create(days, weeks, (short) 52, (short) 30);
        Time time2 = TimeFactory.create(days, weeks, (short) 59, (short) 5);

        assertTrue(time1.overlaps(time2));

        Time time3 = TimeFactory.create(days, weeks, (short) 20, (short) 10);

        assertFalse(time1.overlaps(time3));
        assertTrue(time3.isEarlier(time1));

        Time time4 = TimeFactory.create("1100000", weeks, (short) 20, (short) 10);
        Time time5 = TimeFactory.create("1100000", "1100000000", (short) 20, (short) 10);

        assertTrue(time4.isEarlier(time1));
        assertTrue(time5.isEarlier(time4));
    }
}
