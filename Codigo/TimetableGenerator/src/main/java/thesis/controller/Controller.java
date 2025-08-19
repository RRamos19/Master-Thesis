package thesis.controller;

import thesis.model.ModelInterface;
import thesis.model.domain.DataRepository;
import thesis.model.domain.Timetable;
import thesis.view.ViewInterface;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class Controller implements ControllerInterface {
    private ModelInterface model;
    private ViewInterface view;

    @Override
    public void setModel(ModelInterface model) {
        this.model = model;
        this.model.setController(this);
    }

    @Override
    public void setView(ViewInterface view) {
        this.view = view;
        this.view.setController(this);
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
    public void startGeneratingSolution(String programName, Integer initSolutionMaxIter, double initialTemperature, double minTemperature, double coolingRate, int k) {
        model.startGeneratingSolution(programName, initSolutionMaxIter, initialTemperature, minTemperature, coolingRate, k);
    }

    @Override
    public double getGenerationProgress(String programName) {
        return model.getGenerationProgress(programName);
    }

    @Override
    public Timetable getGeneratedTimetable(String programName) throws ExecutionException, InterruptedException {
        return model.getGeneratedTimetable(programName);
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

    public void cleanup() {
        model.cleanup();
    }
}
