package thesis.controller;

import thesis.model.ModelInterface;
import thesis.model.domain.DataRepository;
import thesis.model.domain.Timetable;
import thesis.model.domain.exceptions.ParsingException;
import thesis.solver.initialsolutiongenerator.InitialSolutionGenerator;
import thesis.solver.initialsolutiongenerator.MullerSolutionGenerator;
import thesis.solver.solutionoptimizer.SimulatedAnnealing;
import thesis.view.ViewInterface;

import java.io.File;

public class Controller implements ControllerInterface {
    private ModelInterface<DataRepository> model;
    private ViewInterface view;

    public void setModel(ModelInterface<DataRepository> model) {
        this.model = model;
        this.model.setController(this);
    }

    public void setView(ViewInterface view) {
        this.view = view;
        this.view.setController(this);
    }

    public Timetable generateTimetable(DataRepository data, double initialTemperature, double minTemperature, double coolingRate, int k) {
        long start = System.currentTimeMillis();
        InitialSolutionGenerator<Timetable> initialSolutionGen = new MullerSolutionGenerator(data);
        Timetable initialSolution = initialSolutionGen.generate(null);

        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(initialSolution, initialTemperature, minTemperature, coolingRate, k);
        Timetable generatedSolution = simulatedAnnealing.execute();
        generatedSolution.setRuntime((System.currentTimeMillis() - start)/1000);

        return generatedSolution;
    }

    public void importITCData(File file) {
        try {
            model.importITCData(file);
        } catch (Exception e) {
            view.showExceptionMessage(e);
        }
    }

    public void exportSolutionsToITC(DataRepository data) {

    }

    public void exportDomainToITC(DataRepository data) {

    }

    public void exportToCSV(DataRepository data) {

    }

    public void exportToPDF(DataRepository data) {

    }

    public void exportToPNG(DataRepository data) {

    }
}
