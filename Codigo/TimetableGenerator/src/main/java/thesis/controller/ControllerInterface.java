package thesis.controller;

import thesis.model.ModelInterface;
import thesis.model.domain.DataRepository;
import thesis.model.domain.Timetable;
import thesis.view.ViewInterface;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface ControllerInterface {
    void setModel(ModelInterface<DataRepository> model);
    void setView(ViewInterface view);
    Timetable generateTimetable(DataRepository data, double initialTemperature, double minTemperature, double coolingRate, int k);
    DataRepository getDataRepository(String programName);
    Set<String> getStoredPrograms();
    void importITCData(File file);
    void exportSolutionsToITC(String programName) throws IOException;
    void exportDataToITC(String programName) throws IOException;
    void exportToCSV(String programName) throws IOException;
    void exportToPDF(String programName) throws IOException;
    void exportToPNG(String programName) throws IOException;
}
