package thesis.controller;

import thesis.model.ModelInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.elements.TableDisplayable;
import thesis.model.domain.elements.Timetable;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.parser.XmlResult;
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
    public double getGenerationProgress(String programName) throws InvalidConfigurationException, ExecutionException, InterruptedException {
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
    public void cancelGeneration(String programName) {
        model.cancelTimetableGeneration(programName);
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
        XmlResult result;

        try {
            result = model.readFile(file);
        } catch (Exception e) {
            view.showExceptionMessage(e);
            return;
        }

        if(result instanceof InMemoryRepository) {
            InMemoryRepository dataRepository = (InMemoryRepository) result;

            InMemoryRepository storedData = model.getDataRepository(dataRepository.getProgramName());

            if(storedData == null) {
                model.importRepository(dataRepository);
            } else {
                if(view.showConfirmationAlert("There is already a program stored which is equal to the program of the file provided. Overwrite (while retaining the solutions if possible) ?")) {
                    for(Timetable solution : storedData.getTimetableList()) {
                        try {
                            dataRepository.addTimetable(solution);
                        } catch (InvalidConfigurationException e) {
                            view.showExceptionMessage(e);
                        }
                    }
                    model.importRepository(dataRepository);
                }
            }
        } else if(result instanceof Timetable) {
            Timetable solution = (Timetable) result;


            try {
                model.importSolution(solution);
            } catch (InvalidConfigurationException e) {
                view.showExceptionMessage(e);
            }
        } else {
            throw new IllegalStateException("The resulting type of the parsing is unsupported");
        }
    }

    @Override
    public void startGeneratingSolution(String programName, Integer initSolutionMaxIter, double initialTemperature, double minTemperature, double coolingRate, int k) {
        model.startGeneratingSolution(programName, initSolutionMaxIter, initialTemperature, minTemperature, coolingRate, k);
    }

    private void export(String programName, ModelInterface.ExportType type) {
        try {
            model.export(programName, type);
        } catch(IOException e) {
            view.showExceptionMessage(e);
        }

        view.showInformationAlert("The data was exported successfully to the following location: " + model.getExportLocation());
    }

    @Override
    public void exportSolutionsToITC(String programName) {
        InMemoryRepository data = model.getDataRepository(programName);

        if(!data.getTimetableList().isEmpty()) {
            export(programName, ModelInterface.ExportType.SOLUTIONS_ITC);
        } else {
            view.showErrorAlert("There are no solutions to export!");
        }
    }

    @Override
    public void exportDataToITC(String programName) {
        export(programName, ModelInterface.ExportType.DATA_ITC);
    }

    @Override
    public void exportToCSV(String programName) {
        export(programName, ModelInterface.ExportType.CSV);
    }

    @Override
    public void exportToPDF(String programName) {
        export(programName, ModelInterface.ExportType.PDF);
    }

    @Override
    public void exportToPNG(String programName) {
        export(programName, ModelInterface.ExportType.PNG);
    }
}
