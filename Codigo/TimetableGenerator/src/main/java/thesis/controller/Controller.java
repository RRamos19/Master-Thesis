package thesis.controller;

import thesis.model.ModelInterface;
import thesis.model.domain.DataRepository;
import thesis.model.domain.Timetable;
import thesis.solver.initialsolutiongenerator.InitialSolutionGenerator;
import thesis.solver.initialsolutiongenerator.MullerSolutionGenerator;
import thesis.solver.solutionoptimizer.SimulatedAnnealing;
import thesis.view.ViewInterface;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class Controller implements ControllerInterface {
    private ModelInterface<DataRepository> model;
    private ViewInterface view;

    @Override
    public void setModel(ModelInterface<DataRepository> model) {
        this.model = model;
        this.model.setController(this);
    }

    @Override
    public void setView(ViewInterface view) {
        this.view = view;
        this.view.setController(this);
    }

    @Override
    public Timetable generateTimetable(DataRepository data, double initialTemperature, double minTemperature, double coolingRate, int k) {
        long start = System.currentTimeMillis();
        InitialSolutionGenerator<Timetable> initialSolutionGen = new MullerSolutionGenerator(data);
        Timetable initialSolution = initialSolutionGen.generate(null);

        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(initialSolution, initialTemperature, minTemperature, coolingRate, k);
        Timetable generatedSolution = simulatedAnnealing.execute();
        generatedSolution.setRuntime((System.currentTimeMillis() - start)/1000);

        return generatedSolution;
    }

    @Override
    public DataRepository getDataRepository(String programName) {
        return model.getDataRepository(programName);
    }

    @Override
    public void importITCData(File file) {
        try {
            model.importITCData(file);
        } catch (Exception e) {
            view.showExceptionMessage(e);
        }
    }

    @Override
    public Set<String> getStoredPrograms() {
        return model.getStoredPrograms();
    }

    @Override
    public void exportSolutionsToITC(String programName) throws IOException {
        model.exportSolutionsToITC(programName);
    }

    @Override
    public void exportDataToITC(String programName) throws IOException {
        model.exportDataToITC(programName);
    }

    @Override
    public void exportToCSV(String programName) throws IOException {
        model.exportToCSV(programName);
    }

    @Override
    public void exportToPDF(String programName) throws IOException {
        model.exportToPDF(programName);
    }

    @Override
    public void exportToPNG(String programName) throws IOException {
        model.exportToPNG(programName);
    }
}
