package thesis.solver.core;

import org.junit.jupiter.api.Test;
import thesis.model.domain.components.ClassUnit;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Time;
import thesis.model.domain.components.TimeFactory;
import thesis.model.exceptions.CheckedIllegalArgumentException;
import thesis.model.solver.core.DefaultISGValue;
import thesis.model.solver.core.DefaultISGVariable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ValueTest {
    @Test
    public void testEquals() throws CheckedIllegalArgumentException {
        String classId1 = "1";
        ClassUnit cls1 = new ClassUnit(classId1);
        String roomId1 = "1";
        String roomId2 = "2";
        Time time1 = TimeFactory.create("1111111", "111111111111", (short) 20, (short) 10);
        Time time2 = TimeFactory.create("1111111", "111111111111", (short) 10, (short) 12);
        ScheduledLesson scheduledLesson1 = new ScheduledLesson(classId1, roomId1, time1);
        ScheduledLesson scheduledLesson2 = new ScheduledLesson(classId1, roomId1, time1);
        ScheduledLesson scheduledLesson3 = new ScheduledLesson(classId1, roomId2, time1);
        ScheduledLesson scheduledLesson4 = new ScheduledLesson(classId1, roomId1, time2);
        ScheduledLesson scheduledLesson5 = new ScheduledLesson(classId1, roomId2, time2);

        DefaultISGVariable variable = new DefaultISGVariable(cls1);
        DefaultISGValue value1 = new DefaultISGValue(variable, scheduledLesson1);
        DefaultISGValue value2 = new DefaultISGValue(variable, scheduledLesson2);
        DefaultISGValue value3 = new DefaultISGValue(variable, scheduledLesson3);
        DefaultISGValue value4 = new DefaultISGValue(variable, scheduledLesson4);
        DefaultISGValue value5 = new DefaultISGValue(variable, scheduledLesson5);

        assertEquals(value1, value2);

        List<DefaultISGValue> valueList = List.of(value1, value3, value4, value5);
        for(int i=0; i < valueList.size() - 1; i++) {
            for(int j=i+1; j < valueList.size(); j++) {
                assertNotEquals(valueList.get(i), valueList.get(j));
            }
        }
    }
}
