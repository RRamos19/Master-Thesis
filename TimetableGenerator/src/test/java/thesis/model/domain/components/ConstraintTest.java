package thesis.model.domain.components;

import org.junit.jupiter.api.Test;
import thesis.model.domain.components.constraints.ConstraintFactory;
import thesis.model.exceptions.CheckedIllegalArgumentException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConstraintTest {
    @Test
    public void testConstraintCreation() throws CheckedIllegalArgumentException {
        TimetableConfiguration timetableConfiguration = new TimetableConfiguration((short) 7, 8, (short) 288, (short) 1, (short) 1, (short) 1);
        Constraint constraint1 = ConstraintFactory.createConstraint("SameStart", 2, false, timetableConfiguration);
        Constraint constraint2 = ConstraintFactory.createConstraint("SameTime", 2, false, timetableConfiguration);
        Constraint constraint3 = ConstraintFactory.createConstraint("SameDays", 2, false, timetableConfiguration);
        Constraint constraint4 = ConstraintFactory.createConstraint("SameWeeks", 2, false, timetableConfiguration);
        Constraint constraint5 = ConstraintFactory.createConstraint("SameRoom", 2, false, timetableConfiguration);
        Constraint constraint6 = ConstraintFactory.createConstraint("DifferentTime", 2, false, timetableConfiguration);
        Constraint constraint7 = ConstraintFactory.createConstraint("DifferentDays", 2, false, timetableConfiguration);
        Constraint constraint8 = ConstraintFactory.createConstraint("DifferentWeeks", 2, false, timetableConfiguration);
        Constraint constraint9 = ConstraintFactory.createConstraint("DifferentRoom", 2, false, timetableConfiguration);

        Constraint constraint10 = ConstraintFactory.createConstraint("Overlap", 2, false, timetableConfiguration);
        Constraint constraint11 = ConstraintFactory.createConstraint("SameAttendees", 2, false, timetableConfiguration);
        Constraint constraint12 = ConstraintFactory.createConstraint("Precedence", 2, false, timetableConfiguration);
        Constraint constraint13 = ConstraintFactory.createConstraint("WorkDay(8)", 2, false, timetableConfiguration);
        Constraint constraint14 = ConstraintFactory.createConstraint("MinGap(3)", 2, false, timetableConfiguration);
        Constraint constraint15 = ConstraintFactory.createConstraint("NotOverlap", 2, false, timetableConfiguration);

        Constraint constraint16 = ConstraintFactory.createConstraint("MaxDays(4)", 2, false, timetableConfiguration);
        Constraint constraint17 = ConstraintFactory.createConstraint("MaxDayLoad(20)", 2, false, timetableConfiguration);
        Constraint constraint18 = ConstraintFactory.createConstraint("MaxBreaks(3,10)", 2, false, timetableConfiguration);
        Constraint constraint19 = ConstraintFactory.createConstraint("MaxBlock(4,5)", 2, false, timetableConfiguration);

        assertTrue(constraint13.getFirstParameter() != null && constraint13.getFirstParameter() == 8 && constraint13.getSecondParameter() == null);
        assertTrue(constraint14.getFirstParameter() != null && constraint14.getFirstParameter() == 3 && constraint14.getSecondParameter() == null);

        assertTrue(constraint16.getFirstParameter() != null && constraint16.getFirstParameter() == 4 && constraint16.getSecondParameter() == null);
        assertTrue(constraint17.getFirstParameter() != null && constraint17.getFirstParameter() == 20 && constraint17.getSecondParameter() == null);
        assertTrue(constraint18.getFirstParameter() != null && constraint18.getFirstParameter() == 3 && constraint18.getSecondParameter() != null && constraint18.getSecondParameter() == 10);
        assertTrue(constraint19.getFirstParameter() != null && constraint19.getFirstParameter() == 4 && constraint19.getSecondParameter() != null && constraint19.getSecondParameter() == 5);
    }
}
