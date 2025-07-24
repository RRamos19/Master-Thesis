package thesis.solver.initialsolutiongenerator.core;

import java.util.List;

public interface ISGValueList<Val> {
    List<Val> values();
    Val random();
}
