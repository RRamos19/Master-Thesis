package thesis.controller;

import thesis.model.ModelInterface;
import thesis.model.domain.DataRepository;
import thesis.model.domain.Timetable;
import thesis.view.ViewInterface;

import java.io.File;

public interface ControllerInterface {
    void setModel(ModelInterface<DataRepository> model);
    void setView(ViewInterface view);
    Timetable generateTimetable(DataRepository data, double initialTemperature, double minTemperature, double coolingRate, int k);
    void importITCData(File file);
    void exportSolutionsToITC(DataRepository data);
    void exportDomainToITC(DataRepository data);
    void exportToCSV(DataRepository data);
    void exportToPDF(DataRepository data);
    void exportToPNG(DataRepository data);
}
