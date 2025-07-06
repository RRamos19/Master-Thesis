package thesis.solver.initialsolutiongenerator.core;

import thesis.model.domain.Constraint;

import java.util.List;
import java.util.Set;

public interface ISGModel<Val extends ISGValue<?, ?>, Var extends ISGVariable<?, ?, ?>, Constr extends Constraint> {
    //variables
    List<Var> variables();
    void addVariable(Var variable);
    void removeVariable(Var variable);

    //constraints
    Set<Val> conflictValues(Val value);

    void saveBest();
    void restoreBest();
}
