package thesis.model;

import thesis.controller.ControllerInterface;
import thesis.model.domain.DataRepository;
import thesis.model.domain.Timetable;
import thesis.model.domain.exceptions.ParsingException;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public interface ModelInterface<T> {
    void setController(ControllerInterface controller);

    DataRepository getDataRepository(String programName);
    Set<String> getStoredPrograms();

    void startGeneratingSolution(String programName, Integer initSolutionMaxIter, double initialTemperature, double minTemperature, double coolingRate, int k);
    double getGenerationProgress(String programName);
    Timetable getGeneratedTimetable(String programName) throws ExecutionException, InterruptedException;

    void importITCData(File file) throws ParsingException;

    void exportToCSV(String programName) throws IOException;
    void exportToPDF(String programName) throws IOException;
    void exportToPNG(String programName) throws IOException;
    void exportDataToITC(String programName) throws IOException;
    void exportSolutionsToITC(String programName) throws IOException;

    void cleanup();
}
