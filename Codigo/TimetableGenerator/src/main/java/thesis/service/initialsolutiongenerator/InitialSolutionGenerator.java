package thesis.service.initialsolutiongenerator;

import java.util.List;

public interface InitialSolutionGenerator<T, S> {
    /**
     * Generates an initial solution
     *
     * @return The solution generated
     */
    T generate(List<S> unscheduled, int maxIterations);
}
