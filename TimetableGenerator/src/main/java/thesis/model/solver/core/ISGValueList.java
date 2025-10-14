package thesis.model.solver.core;

import java.util.List;

public interface ISGValueList<Val extends ISGValue<Val, ?>> extends Iterable<Val> {
    List<Val> values();
    Val random();
}
