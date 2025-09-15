package thesis.model.domain.components.constraints;

import thesis.model.domain.components.Constraint;
import thesis.model.domain.components.TimetableConfiguration;
import thesis.model.exceptions.CheckedIllegalArgumentException;

public class ConstraintFactory {
    public static Constraint createConstraint(String constraintType, Integer constraintPenalty, boolean constraintRequired, TimetableConfiguration timetableConfiguration) throws CheckedIllegalArgumentException {
        String[] constraintArray = constraintType.split("[(,)]");

        if(constraintArray.length < 1) {
            throw new CheckedIllegalArgumentException("There is an error in the constraint type provided");
        }

        String str0 = constraintArray[0];

        String str1 = null;
        if(constraintArray.length >= 2) {
            str1 = constraintArray[1];
        }

        String str2 = null;
        if(constraintArray.length == 3) {
            str2 = constraintArray[2];
        }

        switch(str0) {
            case "SameStart":
                checkForNoParameters(str0, constraintArray);
                return new SameStartConstraint(str0, constraintPenalty, constraintRequired, timetableConfiguration);
            case "SameTime":
                checkForNoParameters(str0, constraintArray);
                return new SameTimeConstraint(str0, constraintPenalty, constraintRequired, timetableConfiguration);
            case "DifferentTime":
                checkForNoParameters(str0, constraintArray);
                return new DifferentTimeConstraint(str0, constraintPenalty, constraintRequired, timetableConfiguration);
            case "SameDays":
                checkForNoParameters(str0, constraintArray);
                return new SameDaysConstraint(str0, constraintPenalty, constraintRequired, timetableConfiguration);
            case "DifferentDays":
                checkForNoParameters(str0, constraintArray);
                return new DifferentDaysConstraint(str0, constraintPenalty, constraintRequired, timetableConfiguration);
            case "SameWeeks":
                checkForNoParameters(str0, constraintArray);
                return new SameWeeksConstraint(str0, constraintPenalty, constraintRequired, timetableConfiguration);
            case "DifferentWeeks":
                checkForNoParameters(str0, constraintArray);
                return new DifferentWeeksConstraint(str0, constraintPenalty, constraintRequired, timetableConfiguration);
            case "Overlap":
                checkForNoParameters(str0, constraintArray);
                return new OverlapConstraint(str0, constraintPenalty, constraintRequired, timetableConfiguration);
            case "NotOverlap":
                checkForNoParameters(str0, constraintArray);
                return new NotOverlapConstraint(str0, constraintPenalty, constraintRequired, timetableConfiguration);
            case "SameRoom":
                checkForNoParameters(str0, constraintArray);
                return new SameRoomConstraint(str0, constraintPenalty, constraintRequired, timetableConfiguration);
            case "DifferentRoom":
                checkForNoParameters(str0, constraintArray);
                return new DifferentRoomConstraint(str0, constraintPenalty, constraintRequired, timetableConfiguration);
            case "SameAttendees":
                checkForNoParameters(str0, constraintArray);
                return new SameAttendeesConstraint(str0, constraintPenalty, constraintRequired, timetableConfiguration);
            case "Precedence":
                checkForNoParameters(str0, constraintArray);
                return new PrecedenceConstraint(str0, constraintPenalty, constraintRequired, timetableConfiguration);
            case "WorkDay":
                checkForOnlyOneParameter(str0, constraintArray);
                return new WorkDayConstraint(str0, str1, constraintPenalty, constraintRequired, timetableConfiguration);
            case "MinGap":
                checkForOnlyOneParameter(str0, constraintArray);
                return new MinGapConstraint(str0, str1, constraintPenalty, constraintRequired, timetableConfiguration);
            case "MaxDays":
                checkForOnlyOneParameter(str0, constraintArray);
                return new MaxDaysConstraint(str0, str1, constraintPenalty, constraintRequired, timetableConfiguration);
            case "MaxDayLoad":
                checkForOnlyOneParameter(str0, constraintArray);
                return new MaxDayLoadConstraint(str0, str1, constraintPenalty, constraintRequired, timetableConfiguration);
            case "MaxBreaks":
                checkForOnlyTwoParameters(str0, constraintArray);
                return new MaxBreaksConstraint(str0, str1, str2, constraintPenalty, constraintRequired, timetableConfiguration);
            case "MaxBlock":
                checkForOnlyTwoParameters(str0, constraintArray);
                return new MaxBlockConstraint(str0, str1, str2, constraintPenalty, constraintRequired, timetableConfiguration);
            default:
                throw new CheckedIllegalArgumentException("The constraint of type: " + constraintType + " is not supported");
        }
    }

    private static void checkForNoParameters(String constraintName, String[] constraintArray) throws CheckedIllegalArgumentException {
        if(constraintArray.length != 1) {
            throw new CheckedIllegalArgumentException("The " + constraintName + " constraint must have no parameters");
        }
    }

    private static void checkForOnlyOneParameter(String constraintName, String[] constraintArray) throws CheckedIllegalArgumentException {
        if(constraintArray.length != 2) {
            throw new CheckedIllegalArgumentException("The " + constraintName + " constraint must have only one parameter");
        }
    }

    private static void checkForOnlyTwoParameters(String constraintName, String[] constraintArray) throws CheckedIllegalArgumentException {
        if(constraintArray.length != 3) {
            throw new CheckedIllegalArgumentException("The " + constraintName + " constraint must have only two parameters");
        }
    }
}
