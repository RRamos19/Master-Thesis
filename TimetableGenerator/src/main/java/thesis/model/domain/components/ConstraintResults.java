package thesis.model.domain.components;

import java.util.HashSet;
import java.util.Set;

public class ConstraintResults {
    public int penalty = 0;
    public Set<String> conflictingClasses = new HashSet<>();
}
