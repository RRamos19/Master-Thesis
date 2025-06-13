package thesis.model.domain.restrictions;

import thesis.model.domain.Timetable;

public class MaxBreaksRestriction extends Restriction {
    public MaxBreaksRestriction(String type, Integer penalty, boolean required) {
        super(type, penalty, required);
    }

    @Override
    public int evaluate(Timetable solution) {
        return 0;
    }
}
