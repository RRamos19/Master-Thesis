package thesis.model.domain.constraints;

import thesis.model.domain.Constraint;
import thesis.model.domain.exceptions.CheckedIllegalArgumentException;

import java.util.HashMap;

public class ConstraintFactory {
    private static HashMap<String, String> stringPool = new HashMap<>();

    public static Constraint createConstraint(String restrictionType, Integer constraintPenalty, boolean constraintRequired) throws CheckedIllegalArgumentException {
        String[] ConstraintArray = restrictionType.split("[(,)]");

        String str0 = stringPool.computeIfAbsent(ConstraintArray[0], s -> s);

        String str1 = null;
        if(ConstraintArray.length >= 2) {
            str1 = stringPool.computeIfAbsent(ConstraintArray[1], s -> s);
        }

        String str2 = null;
        if(ConstraintArray.length == 3) {
            str2 = stringPool.computeIfAbsent(ConstraintArray[2], s -> s);
        }

        switch(str0) {
            case "SameStart":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have no parameters");
                }
                return new SameStartConstraint(str0, constraintPenalty, constraintRequired);
            case "SameTime":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have no parameters");
                }
                return new SameTimeConstraint(str0, constraintPenalty, constraintRequired);
            case "DifferentTime":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have no parameters");
                }
                return new DifferentTimeConstraint(str0, constraintPenalty, constraintRequired);
            case "SameDays":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have no parameters");
                }
                return new SameDaysConstraint(str0, constraintPenalty, constraintRequired);
            case "DifferentDays":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have no parameters");
                }
                return new DifferentDaysConstraint(str0, constraintPenalty, constraintRequired);
            case "SameWeeks":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have no parameters");
                }
                return new SameWeeksConstraint(str0, constraintPenalty, constraintRequired);
            case "DifferentWeeks":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have no parameters");
                }
                return new DifferentWeeksConstraint(str0, constraintPenalty, constraintRequired);
            case "Overlap":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have no parameters");
                }
                return new OverlapConstraint(str0, constraintPenalty, constraintRequired);
            case "NotOverlap":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have no parameters");
                }
                return new NotOverlapConstraint(str0, constraintPenalty, constraintRequired);
            case "SameRoom":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have no parameters");
                }
                return new SameRoomConstraint(str0, constraintPenalty, constraintRequired);
            case "DifferentRoom":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have no parameters");
                }
                return new DifferentRoomConstraint(str0, constraintPenalty, constraintRequired);
            case "SameAttendees":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have no parameters");
                }
                return new SameAttendeesConstraint(str0, constraintPenalty, constraintRequired);
            case "Precedence":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have no parameters");
                }
                return new PrecedenceConstraint(str0, constraintPenalty, constraintRequired);
            case "WorkDay":
                if(ConstraintArray.length != 2) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have only one parameter");
                }
                assert(str1 != null);
                return new WorkDayConstraint(str0, str1, constraintPenalty, constraintRequired);
            case "MinGap":
                if(ConstraintArray.length != 2) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have only one parameter");
                }
                assert(str1 != null);
                return new MinGapConstraint(str0, str1, constraintPenalty, constraintRequired);
            case "MaxDays":
                if(ConstraintArray.length != 2) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have only one parameter");
                }
                assert(str1 != null);
                return new MaxDaysConstraint(str0, str1, constraintPenalty, constraintRequired);
            case "MaxDayLoad":
                if(ConstraintArray.length != 2) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have only one parameter");
                }
                assert(str1 != null);
                return new MaxDayLoadConstraint(str0, str1, constraintPenalty, constraintRequired);
            case "MaxBreaks":
                if(ConstraintArray.length != 3) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have only two parameters");
                }
                assert(str1 != null);
                assert(str2 != null);
                return new MaxBreaksConstraint(str0, str1, str2, constraintPenalty, constraintRequired);
            case "MaxBlock":
                if(ConstraintArray.length != 3) {
                    throw new CheckedIllegalArgumentException("The " + str0 + " restriction must have only two parameters");
                }
                assert(str1 != null);
                assert(str2 != null);
                return new MaxBlockConstraint(str0, str1, str2, constraintPenalty, constraintRequired);
            default:
                throw new CheckedIllegalArgumentException("The restriction of type: " + restrictionType + " is not supported");
        }
    }
}
