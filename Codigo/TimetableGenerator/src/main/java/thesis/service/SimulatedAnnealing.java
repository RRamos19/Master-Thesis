package thesis.service;

import thesis.model.entities.Timetable;

public class SimulatedAnnealing implements HeuristicAlgorithm<Timetable> {
    Timetable initialSolution;
    int initialTemperature;
    double coolingRate;

    public SimulatedAnnealing(Timetable initialSolution, int initialTemperature, double c){
        this.initialSolution = initialSolution;
        this.initialTemperature = initialTemperature;
        this.coolingRate = coolingRate;
    }

    @Override
    public Timetable execute(){
        return null;
    }

    private int costFunction(){
        return 0;
    }

    private Timetable neighborhoodFunction(){
        return null;
    }
}
