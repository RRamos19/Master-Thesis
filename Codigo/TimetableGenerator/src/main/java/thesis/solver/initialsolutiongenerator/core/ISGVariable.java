package thesis.solver.initialsolutiongenerator.core;

import java.util.List;

public interface ISGVariable<Var, Val extends ISGValue<?, ?>, Const> {
    Var variable();
    ISGVirtualValueList<Val> getValues();
    void unassign();
    void assign(Val value);
}
