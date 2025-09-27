package thesis.model.domain.components;

import org.junit.jupiter.api.Test;
import thesis.model.domain.DataRepository;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.constraints.ConstraintFactory;
import thesis.model.exceptions.CheckedIllegalArgumentException;
import thesis.model.exceptions.InvalidConfigurationException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TimetableTest {
    @Test
    public void testValidateInvolvedConstraints() throws CheckedIllegalArgumentException, InvalidConfigurationException {
        InMemoryRepository repository = new DataRepository();
        repository.setProgramName("test");
        repository.setConfiguration((short) 7, 8, (short) 288);
        repository.setOptimizationParameters((short) 1, (short) 1, (short) 1);

        TimetableConfiguration timetableConfiguration = repository.getTimetableConfiguration();

        ClassUnit class1 = new ClassUnit("1");
        ClassUnit class2 = new ClassUnit("2");
        ClassUnit class3 = new ClassUnit("3");
        ClassUnit class4 = new ClassUnit("4");

        repository.addClassUnit(class1);
        repository.addClassUnit(class2);
        repository.addClassUnit(class3);
        repository.addClassUnit(class4);

        Constraint constraint1 = ConstraintFactory.createConstraint("SameStart", 2, false, timetableConfiguration);
        Constraint constraint2 = ConstraintFactory.createConstraint("SameWeeks", 2, false, timetableConfiguration);
        Constraint constraint3 = ConstraintFactory.createConstraint("SameAttendees", 2, false, timetableConfiguration);
        Constraint constraint4 = ConstraintFactory.createConstraint("Overlap", 2, false, timetableConfiguration);

        repository.addConstraint(constraint1);
        repository.addConstraint(constraint2);
        repository.addConstraint(constraint3);
        repository.addConstraint(constraint4);

        class1.addConstraint(constraint1);
        class1.addConstraint(constraint2);
        constraint1.addClassUnitId(class1.getClassId());
        constraint2.addClassUnitId(class1.getClassId());

        class2.addConstraint(constraint2);
        constraint2.addClassUnitId(class2.getClassId());

        class3.addConstraint(constraint3);
        class3.addConstraint(constraint4);
        constraint3.addClassUnitId(class3.getClassId());
        constraint4.addClassUnitId(class3.getClassId());

        class4.addConstraint(constraint1);
        class4.addConstraint(constraint3);
        constraint3.addClassUnitId(class4.getClassId());
        constraint4.addClassUnitId(class4.getClassId());

        Time time1 = TimeFactory.create((short) 3, 255, (short) 10, (short) 10);
        Time time2 = TimeFactory.create((short) 5, 255, (short) 10, (short) 10);
        Time time3 = TimeFactory.create((short) 7, 255, (short) 10, (short) 10);

        ScheduledLesson scheduledLesson1 = new ScheduledLesson(class1.getClassId(), "1", time1);
        ScheduledLesson scheduledLesson2 = new ScheduledLesson(class2.getClassId(), "2", time2);
        ScheduledLesson scheduledLesson3 = new ScheduledLesson(class3.getClassId(), "3", time3);
        ScheduledLesson scheduledLesson4 = new ScheduledLesson(class4.getClassId(), "3", time3);

        Timetable timetable = new Timetable();
        repository.addTimetable(timetable);
        timetable.addScheduledLesson(scheduledLesson1);
        timetable.addScheduledLesson(scheduledLesson2);

        Set<Constraint> constraintSet = timetable.getInvolvedConstraints();
        assertEquals(2, constraintSet.size());
        assertTrue(constraintSet.contains(constraint1));
        assertTrue(constraintSet.contains(constraint2));
        assertFalse(constraintSet.contains(constraint3));
        assertFalse(constraintSet.contains(constraint4));

        // Test class 3 temporary

        timetable.addTemporaryScheduledLesson(scheduledLesson3);
        assertEquals(4, constraintSet.size());
        assertTrue(constraintSet.contains(constraint1));
        assertTrue(constraintSet.contains(constraint2));
        assertTrue(constraintSet.contains(constraint3));
        assertTrue(constraintSet.contains(constraint4));

        timetable.removeTemporaryScheduledLesson(scheduledLesson3);
        assertEquals(2, constraintSet.size());
        assertTrue(constraintSet.contains(constraint1));
        assertTrue(constraintSet.contains(constraint2));
        assertFalse(constraintSet.contains(constraint3));
        assertFalse(constraintSet.contains(constraint4));

        // Test class 4 temporary

        timetable.addTemporaryScheduledLesson(scheduledLesson4);
        assertEquals(3, constraintSet.size());
        assertTrue(constraintSet.contains(constraint1));
        assertTrue(constraintSet.contains(constraint2));
        assertTrue(constraintSet.contains(constraint3));
        assertFalse(constraintSet.contains(constraint4));

        timetable.removeTemporaryScheduledLesson(scheduledLesson4);
        assertEquals(2, constraintSet.size());
        assertTrue(constraintSet.contains(constraint1));
        assertTrue(constraintSet.contains(constraint2));
        assertFalse(constraintSet.contains(constraint3));
        assertFalse(constraintSet.contains(constraint4));
    }
}
