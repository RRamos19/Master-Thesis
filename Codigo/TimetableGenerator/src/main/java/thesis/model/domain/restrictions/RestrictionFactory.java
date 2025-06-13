package thesis.model.domain.restrictions;

public class RestrictionFactory {
    public static Restriction createRestriction(String restrictionType, Integer restrictionPenalty, boolean restrictionRequired) throws Exception {
        switch(restrictionType) {
            case "SameStart":
                return new SameStartRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "SameTime":
                return new SameTimeRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "DifferentTime":
                return new DifferentTimeRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "SameDays":
                return new SameDaysRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "DifferentDays":
                return new DifferentDaysRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "SameWeeks":
                return new SameWeeksRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "DifferentWeeks":
                return new DifferentWeeksRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "Overlap":
                return new OverlapRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "NotOverlap":
                return new NotOverlapRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "SameRoom":
                return new SameRoomRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "DifferentRoom":
                return new DifferentRoomRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "SameAttendees":
                return new SameAttendeesRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "Precedence":
                return new PrecedenceRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "WorkDay":
                return new WorkDayRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "MinGap":
                return new MinGapRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "MaxDays":
                return new MaxDaysRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "MaxDayLoad":
                return new MaxDayLoadRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "MaxBreaks":
                return new MaxBreaksRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            case "MaxBlock":
                return new MaxBlockRestriction(restrictionType, restrictionPenalty, restrictionRequired);
            default:
                throw new Exception("The restriction of type: " + restrictionType + " is not supported");
        }
    }
}
