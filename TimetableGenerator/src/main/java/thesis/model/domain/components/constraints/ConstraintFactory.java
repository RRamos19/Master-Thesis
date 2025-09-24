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

        Integer str1 = null;
        if(constraintArray.length >= 2) {
            str1 = Integer.parseInt(constraintArray[1]);
        }

        Integer str2 = null;
        if(constraintArray.length == 3) {
            str2 = Integer.parseInt(constraintArray[2]);
        }

        return createConstraint(str0, str1, str2, constraintPenalty, constraintRequired, timetableConfiguration);
    }

    public static Constraint createConstraint(String constraintType, Integer firstParam, Integer secondParam, Integer constraintPenalty, boolean constraintRequired, TimetableConfiguration timetableConfiguration) throws CheckedIllegalArgumentException {
        switch(constraintType) {
            case "SameStart":
                checkForNoParameters(constraintType, firstParam, secondParam);
                return new SameStartConstraint(constraintType, constraintPenalty, constraintRequired, timetableConfiguration);
            case "SameTime":
                checkForNoParameters(constraintType, firstParam, secondParam);
                return new SameTimeConstraint(constraintType, constraintPenalty, constraintRequired, timetableConfiguration);
            case "DifferentTime":
                checkForNoParameters(constraintType, firstParam, secondParam);
                return new DifferentTimeConstraint(constraintType, constraintPenalty, constraintRequired, timetableConfiguration);
            case "SameDays":
                checkForNoParameters(constraintType, firstParam, secondParam);
                return new SameDaysConstraint(constraintType, constraintPenalty, constraintRequired, timetableConfiguration);
            case "DifferentDays":
                checkForNoParameters(constraintType, firstParam, secondParam);
                return new DifferentDaysConstraint(constraintType, constraintPenalty, constraintRequired, timetableConfiguration);
            case "SameWeeks":
                checkForNoParameters(constraintType, firstParam, secondParam);
                return new SameWeeksConstraint(constraintType, constraintPenalty, constraintRequired, timetableConfiguration);
            case "DifferentWeeks":
                checkForNoParameters(constraintType, firstParam, secondParam);
                return new DifferentWeeksConstraint(constraintType, constraintPenalty, constraintRequired, timetableConfiguration);
            case "Overlap":
                checkForNoParameters(constraintType, firstParam, secondParam);
                return new OverlapConstraint(constraintType, constraintPenalty, constraintRequired, timetableConfiguration);
            case "NotOverlap":
                checkForNoParameters(constraintType, firstParam, secondParam);
                return new NotOverlapConstraint(constraintType, constraintPenalty, constraintRequired, timetableConfiguration);
            case "SameRoom":
                checkForNoParameters(constraintType, firstParam, secondParam);
                return new SameRoomConstraint(constraintType, constraintPenalty, constraintRequired, timetableConfiguration);
            case "DifferentRoom":
                checkForNoParameters(constraintType, firstParam, secondParam);
                return new DifferentRoomConstraint(constraintType, constraintPenalty, constraintRequired, timetableConfiguration);
            case "SameAttendees":
                checkForNoParameters(constraintType, firstParam, secondParam);
                return new SameAttendeesConstraint(constraintType, constraintPenalty, constraintRequired, timetableConfiguration);
            case "Precedence":
                checkForNoParameters(constraintType, firstParam, secondParam);
                return new PrecedenceConstraint(constraintType, constraintPenalty, constraintRequired, timetableConfiguration);
            case "WorkDay":
                checkForOnlyOneParameter(constraintType, firstParam, secondParam);
                return new WorkDayConstraint(constraintType, firstParam, constraintPenalty, constraintRequired, timetableConfiguration);
            case "MinGap":
                checkForOnlyOneParameter(constraintType, firstParam, secondParam);
                return new MinGapConstraint(constraintType, firstParam, constraintPenalty, constraintRequired, timetableConfiguration);
            case "MaxDays":
                checkForOnlyOneParameter(constraintType, firstParam, secondParam);
                return new MaxDaysConstraint(constraintType, firstParam, constraintPenalty, constraintRequired, timetableConfiguration);
            case "MaxDayLoad":
                checkForOnlyOneParameter(constraintType, firstParam, secondParam);
                return new MaxDayLoadConstraint(constraintType, firstParam, constraintPenalty, constraintRequired, timetableConfiguration);
            case "MaxBreaks":
                checkForOnlyTwoParameters(constraintType, firstParam, secondParam);
                return new MaxBreaksConstraint(constraintType, firstParam, secondParam, constraintPenalty, constraintRequired, timetableConfiguration);
            case "MaxBlock":
                checkForOnlyTwoParameters(constraintType, firstParam, secondParam);
                return new MaxBlockConstraint(constraintType, firstParam, secondParam, constraintPenalty, constraintRequired, timetableConfiguration);
            default:
                throw new CheckedIllegalArgumentException("The constraint of type: " + constraintType + " is not supported");
        }
    }

    private static void checkForNoParameters(String constraintName, Integer firstParam, Integer secondParam) throws CheckedIllegalArgumentException {
        if(firstParam != null || secondParam != null) {
            throw new CheckedIllegalArgumentException("The " + constraintName + " constraint must have no parameters");
        }
    }

    private static void checkForOnlyOneParameter(String constraintName, Integer firstParam, Integer secondParam) throws CheckedIllegalArgumentException {
        if(firstParam == null || secondParam != null) {
            throw new CheckedIllegalArgumentException("The " + constraintName + " constraint must have only one parameter");
        }
    }

    private static void checkForOnlyTwoParameters(String constraintName, Integer firstParam, Integer secondParam) throws CheckedIllegalArgumentException {
        if(firstParam == null || secondParam == null) {
            throw new CheckedIllegalArgumentException("The " + constraintName + " constraint must have only two parameters");
        }
    }
}
