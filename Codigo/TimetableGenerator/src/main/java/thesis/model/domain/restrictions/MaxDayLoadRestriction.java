package thesis.model.domain.restrictions;

import thesis.model.domain.Timetable;

public class MaxDayLoadRestriction extends Restriction {
    public MaxDayLoadRestriction(String type, Integer penalty, boolean required) {
        super(type, penalty, required);
    }

    @Override
    public int evaluate(Timetable solution) {
        return 0;
    }
}
