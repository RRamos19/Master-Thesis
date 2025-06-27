package thesis.model.domain.constraints;

import thesis.model.domain.Constraint;
import thesis.model.domain.exceptions.CheckedIllegalArgumentException;

public class ConstraintFactory {
    public static Constraint createConstraint(String restrictionType, Integer constraintPenalty, boolean constraintRequired) throws CheckedIllegalArgumentException {
        String[] ConstraintArray = restrictionType.split("[(,)]");

        switch(ConstraintArray[0]) {
            case "SameStart":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have no parameters");
                }
                return new SameStartConstraint(ConstraintArray[0], constraintPenalty, constraintRequired);
            case "SameTime":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have no parameters");
                }
                return new SameTimeConstraint(ConstraintArray[0], constraintPenalty, constraintRequired);
            case "DifferentTime":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have no parameters");
                }
                return new DifferentTimeConstraint(ConstraintArray[0], constraintPenalty, constraintRequired);
            case "SameDays":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have no parameters");
                }
                return new SameDaysConstraint(ConstraintArray[0], constraintPenalty, constraintRequired);
            case "DifferentDays":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have no parameters");
                }
                return new DifferentDaysConstraint(ConstraintArray[0], constraintPenalty, constraintRequired);
            case "SameWeeks":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have no parameters");
                }
                return new SameWeeksConstraint(ConstraintArray[0], constraintPenalty, constraintRequired);
            case "DifferentWeeks":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have no parameters");
                }
                return new DifferentWeeksConstraint(ConstraintArray[0], constraintPenalty, constraintRequired);
            case "Overlap":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have no parameters");
                }
                return new OverlapConstraint(ConstraintArray[0], constraintPenalty, constraintRequired);
            case "NotOverlap":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have no parameters");
                }
                return new NotOverlapConstraint(ConstraintArray[0], constraintPenalty, constraintRequired);
            case "SameRoom":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have no parameters");
                }
                return new SameRoomConstraint(ConstraintArray[0], constraintPenalty, constraintRequired);
            case "DifferentRoom":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have no parameters");
                }
                return new DifferentRoomConstraint(ConstraintArray[0], constraintPenalty, constraintRequired);
            case "SameAttendees":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have no parameters");
                }
                return new SameAttendeesConstraint(ConstraintArray[0], constraintPenalty, constraintRequired);
            case "Precedence":
                if(ConstraintArray.length != 1) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have no parameters");
                }
                return new PrecedenceConstraint(ConstraintArray[0], constraintPenalty, constraintRequired);
            case "WorkDay":
                if(ConstraintArray.length != 2) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have only one parameter");
                }
                return new WorkDayConstraint(ConstraintArray, constraintPenalty, constraintRequired);
            case "MinGap":
                if(ConstraintArray.length != 2) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have only one parameter");
                }
                return new MinGapConstraint(ConstraintArray, constraintPenalty, constraintRequired);
            case "MaxDays":
                if(ConstraintArray.length != 2) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have only one parameter");
                }
                return new MaxDaysConstraint(ConstraintArray, constraintPenalty, constraintRequired);
            case "MaxDayLoad":
                if(ConstraintArray.length != 2) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have only one parameter");
                }
                return new MaxDayLoadConstraint(ConstraintArray, constraintPenalty, constraintRequired);
            case "MaxBreaks":
                if(ConstraintArray.length != 3) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have only two parameters");
                }
                return new MaxBreaksConstraint(ConstraintArray, constraintPenalty, constraintRequired);
            case "MaxBlock":
                if(ConstraintArray.length != 3) {
                    throw new CheckedIllegalArgumentException("The " + ConstraintArray[0] + " restriction must have only two parameters");
                }
                return new MaxBlockConstraint(ConstraintArray, constraintPenalty, constraintRequired);
            default:
                throw new CheckedIllegalArgumentException("The restriction of type: " + restrictionType + " is not supported");
        }
    }
}
