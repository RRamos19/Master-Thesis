package thesis.model.solver.core;

import thesis.model.domain.components.ScheduledLesson;

import java.util.Objects;

public class DefaultISGValue implements ISGValue<DefaultISGValue, DefaultISGVariable> {
    private final ScheduledLesson scheduledLesson;
    private final DefaultISGVariable variable;

    public DefaultISGValue(DefaultISGVariable variable, ScheduledLesson scheduledLesson) {
        this.variable = variable;
        this.scheduledLesson = scheduledLesson;
    }

    public DefaultISGValue(DefaultISGVariable newVar, DefaultISGValue other) {
        this.scheduledLesson = other.scheduledLesson;
        this.variable = newVar;
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
    public int getRemovals() {
        return variable.getRemovals(this);
    }

    @Override
    public int toInt() {
        return scheduledLesson.toInt().getTotalPenalty();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultISGValue)) return false;
        DefaultISGValue that = (DefaultISGValue) o;
        return Objects.equals(variable, that.variable) &&
                Objects.equals(scheduledLesson, that.scheduledLesson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, scheduledLesson);
    }
}
