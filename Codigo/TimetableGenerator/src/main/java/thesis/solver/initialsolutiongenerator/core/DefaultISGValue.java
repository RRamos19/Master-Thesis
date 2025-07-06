package thesis.solver.initialsolutiongenerator.core;

import thesis.model.domain.ScheduledLesson;

public class DefaultISGValue implements ISGValue<ScheduledLesson, DefaultISGVariable> {
    private final ScheduledLesson scheduledLesson;
    private DefaultISGVariable variable;

    public DefaultISGValue(ScheduledLesson scheduledLesson) {
        this.scheduledLesson = scheduledLesson;
    }

    @Override
    public ScheduledLesson value() {
        return scheduledLesson;
    }

    @Override
    public DefaultISGVariable variable() {
        return variable;
    }

    @Override
    public void assign(DefaultISGVariable variable) {
        this.variable = variable;
    }

    @Override
    public void unassign() {
        variable = null;
    }

    public boolean isAvailable() {
        return scheduledLesson.isAvailable();
    }

    @Override
    public boolean valueEquals(ISGValue<ScheduledLesson, DefaultISGVariable> value) {
        if(!(value instanceof DefaultISGValue))
            return false;

        DefaultISGValue defaultValue = (DefaultISGValue)value;

        if(!variable.equals(defaultValue.variable))
            return false;

        return scheduledLesson.equals(defaultValue.scheduledLesson);
    }

    @Override
    public int toInt() {
        return scheduledLesson.toInt();
    }
}
