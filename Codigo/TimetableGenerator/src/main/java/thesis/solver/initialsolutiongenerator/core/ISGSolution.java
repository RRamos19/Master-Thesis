package thesis.solver.initialsolutiongenerator.core;

public interface ISGSolution<Model> {
    long getIteration(); //current iteration
    double getTime(); //current solution time
    Model getModel(); //model
    //store and restore the best solution
    void saveBest();
    void restoreBest();
}
