package thesis.solver.initialsolutiongenerator.core;

import thesis.model.domain.ScheduledLesson;

import java.util.Objects;

public class DefaultISGValue implements ISGValue<ScheduledLesson, DefaultISGVariable> {
    private final ScheduledLesson scheduledLesson;
    private DefaultISGVariable variable;

    public DefaultISGValue(DefaultISGVariable variable, ScheduledLesson scheduledLesson) {
        this.variable = variable;
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

    public boolean isAvailable() {
        return scheduledLesson.isAvailable();
    }

    public int getRemovals() {
        return variable.getRemovals(this);
    }

    @Override
    public int toInt() {
        return scheduledLesson.toInt();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultISGValue)) return false;
        DefaultISGValue that = (DefaultISGValue) o;
        return Objects.equals(scheduledLesson, that.scheduledLesson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduledLesson);
    }
}
