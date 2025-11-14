package thesis.model.domain.components;

import org.junit.jupiter.api.Test;
import thesis.model.exceptions.CheckedIllegalArgumentException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeTest {
    @Test
    public void testRelationsBetweenTimeBlocks() throws CheckedIllegalArgumentException {
        String days = "0010100";
        String weeks = "0011111111";
        Time time1 = TimeFactory.create(days, weeks, "52", "30");
        Time time2 = TimeFactory.create(days, weeks, "59", "5");

        assertTrue(time1.overlaps(time2));

        Time time3 = TimeFactory.create(days, weeks, "20", "10");

        assertFalse(time1.overlaps(time3));
        assertTrue(time3.isEarlier(time1));

        Time time4 = TimeFactory.create("1100000", weeks, "20", "10");
        Time time5 = TimeFactory.create("1100000", "1100000000", "20", "10");

        assertTrue(time4.isEarlier(time1));
        assertTrue(time5.isEarlier(time4));
    }
}
