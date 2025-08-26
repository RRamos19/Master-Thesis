package thesis.controller;

import thesis.model.ModelInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.elements.TableDisplayable;
import thesis.model.domain.elements.exceptions.ParsingException;
import thesis.view.ViewInterface;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
    public InMemoryRepository getDataRepository(String programName) {
        return model.getDataRepository(programName);
    }

    @Override
    public double getGenerationProgress(String programName) throws ParsingException, ExecutionException, InterruptedException {
        try {
            return model.getGenerationProgress(programName);
        } catch (Exception e) {
            // This should only happen if the process of storing the result is interrupted
            // but that only happens if the progress is 100%, so it should be impossible
            view.showExceptionMessage(e);

            throw e;
        }
    }

    @Override
    public Set<String> getStoredPrograms() {
        return model.getStoredPrograms();
    }

    @Override
    public Map<String, List<TableDisplayable>> getAllDisplayableData(String progName) {
        return model.getAllDisplayableData(progName);
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
    public void exportSolutionsToITC(String programName) {
        try {
            model.exportSolutionsToITC(programName);
        } catch (IOException e) {
            view.showExceptionMessage(e);
        }
    }

    @Override
    public void exportDataToITC(String programName) {
        try {
            model.exportDataToITC(programName);
        } catch (IOException e) {
            view.showExceptionMessage(e);
        }
    }

    @Override
    public void exportToCSV(String programName) {
        try {
            model.exportToCSV(programName);
        } catch (IOException e) {
            view.showExceptionMessage(e);
        }
    }

    @Override
    public void exportToPDF(String programName) {
        try {
            model.exportToPDF(programName);
        } catch (IOException e) {
            view.showExceptionMessage(e);
        }
    }

    @Override
    public void exportToPNG(String programName) {
        try {
            model.exportToPNG(programName);
        } catch (IOException e) {
            view.showExceptionMessage(e);
        }
    }

    public void cleanup() {
        try {
            model.cleanup();
        } catch (InterruptedException e) {
            view.showExceptionMessage(e);
        }
    }
}
