package thesis.model.domain.restrictions;

import thesis.model.domain.Timetable;

public class MaxDaysRestriction extends Restriction {
    public MaxDaysRestriction(String type, Integer penalty, boolean required) {
        super(type, penalty, required);
    }

    @Override
    public int evaluate(Timetable solution) {
        return 0;
    }
}
