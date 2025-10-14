package thesis.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import thesis.model.ModelInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.exceptions.CheckedIllegalStateException;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.exceptions.ParsingException;
import thesis.model.parser.XmlResult;
import thesis.view.ViewInterface;
import thesis.view.viewobjects.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
    public void connectToDatabase(String ip, String port, String userName, String password) throws Exception {
        model.connectToDatabase(ip, port, userName, password);
    }

    @Override
    public void disconnectFromDatabase() {
        model.disconnectFromDatabase();
    }

    @Override
    public InMemoryRepository getDataRepository(String programName) {
        return model.getDataRepository(programName);
    }

    @Override
    public double getGenerationProgress(UUID progressUUID) throws InvalidConfigurationException, ExecutionException, InterruptedException {
        return model.getGenerationProgress(progressUUID);
    }

    @Override
    public void cancelGeneration(UUID progressUUID) {
        model.cancelTimetableGeneration(progressUUID);
    }

    @Override
    public Set<String> getStoredPrograms() {
        return model.getStoredPrograms();
    }

    @Override
    public ObservableList<ViewModel> getCourses(String progName) {
        Collection<Course> courseList;
        try {
            courseList = model.getCourses(progName);
        } catch (CheckedIllegalStateException e) {
            view.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> courseViewModels = courseList.stream()
                .map(c -> new CourseViewModel(c.getCourseId(), c.getConfigList().size()))
                .collect(Collectors.toList());

        return FXCollections.observableList(courseViewModels);
    }

    @Override
    public ObservableList<ViewModel> getConfigs(String progName) {
        Collection<Config> configList;
        try {
            configList = model.getConfigs(progName);
        } catch (CheckedIllegalStateException e) {
            view.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> configViewModels = configList.stream()
                .map(c -> new ConfigViewModel(c.getConfigId(), c.getSubpartList().size()))
                .collect(Collectors.toList());

        return FXCollections.observableList(configViewModels);
    }

    @Override
    public ObservableList<ViewModel> getSubparts(String progName) {
        Collection<Subpart> subpartList;
        try {
            subpartList = model.getSubparts(progName);
        } catch (CheckedIllegalStateException e) {
            view.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> subpartViewModels = subpartList.stream()
                .map(c -> new SubpartViewModel(c.getSubpartId(), c.getClassUnitList().size()))
                .collect(Collectors.toList());

        return FXCollections.observableList(subpartViewModels);
    }

    @Override
    public ObservableList<ViewModel> getClassUnits(String progName) {
        Collection<ClassUnit> classUnitList;
        try {
            classUnitList = model.getClassUnits(progName);
        } catch (CheckedIllegalStateException e) {
            view.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> classUnitViewModels = classUnitList.stream()
                .map(c -> new ClassUnitViewModel(c.getClassId(), c.getParentClassId()))
                .collect(Collectors.toList());

        return FXCollections.observableList(classUnitViewModels);
    }

    @Override
    public ObservableList<ViewModel> getConfiguration(String progName) {
        TimetableConfiguration timetableConfiguration;
        try {
            timetableConfiguration = model.getConfiguration(progName);
        } catch (CheckedIllegalStateException e) {
            view.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> configurationViewModelList = new ArrayList<>();
        configurationViewModelList.add(new ConfigurationViewModel(
                timetableConfiguration.getNumDays(),
                timetableConfiguration.getNumWeeks(),
                timetableConfiguration.getSlotsPerDay(),
                timetableConfiguration.getTimeWeight(),
                timetableConfiguration.getRoomWeight(),
                timetableConfiguration.getDistribWeight()));

        return FXCollections.observableList(configurationViewModelList);
    }

    @Override
    public ObservableList<ViewModel> getConstraints(String progName) {
        Collection<Constraint> constraintList;
        try {
            constraintList = model.getConstraints(progName);
        } catch (CheckedIllegalStateException e) {
            view.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> constraintViewModels = constraintList.stream()
                .map(c -> new ConstraintViewModel(c.getType(),
                        c.getFirstParameter(), c.getSecondParameter(), c.getPenalty(),
                        c.getRequired(), c.getClassUnitIdList().size()))
                .collect(Collectors.toList());

        return FXCollections.observableList(constraintViewModels);
    }

    @Override
    public ObservableList<ViewModel> getRooms(String progName) {
        Collection<Room> roomList;
        try {
            roomList = model.getRooms(progName);
        } catch (CheckedIllegalStateException e) {
            view.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> roomViewModels = roomList.stream()
                .map(c -> new RoomViewModel(c.getRoomId(), c.getRoomUnavailabilities().size(), c.getRoomDistances().size()))
                .collect(Collectors.toList());

        return FXCollections.observableList(roomViewModels);
    }

    @Override
    public ObservableList<ViewModel> getTimetables(String progName) {
        Collection<Timetable> timetableList;
        try {
            timetableList = model.getTimetables(progName);
        } catch (CheckedIllegalStateException e) {
            view.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> timetableViewModels = timetableList.stream()
                .map(c -> new TimetableViewModel(c.getDateOfCreationString(), c.getRuntime(), c.cost(), c.getScheduledLessonList().size(), c.isValid(), c))
                .collect(Collectors.toList());

        return FXCollections.observableList(timetableViewModels);
    }

    @Override
    public void importITCData(File file) throws CheckedIllegalStateException, InvalidConfigurationException, ParsingException {
        XmlResult result;

        result = model.readFile(file);

        if(result instanceof InMemoryRepository) {
            InMemoryRepository dataRepository = (InMemoryRepository) result;

            InMemoryRepository storedData = model.getDataRepository(dataRepository.getProgramName());

            if(storedData == null) {
                model.importRepository(dataRepository);
            } else {
                if(view.showConfirmationAlert("There is already a program stored which is equal to the program of the file provided. Overwrite (while retaining the solutions if possible) ?")) {
                    for(Timetable solution : storedData.getTimetableList()) {
                        dataRepository.addTimetable(solution);
                    }

                    model.importRepository(dataRepository);
                }
            }
        } else if(result instanceof Timetable) {
            Timetable solution = (Timetable) result;

            model.importSolution(solution);

        } else {
            throw new IllegalStateException("The resulting type of the parsing is unsupported");
        }
    }

    @Override
    public void startGeneratingSolution(String programName, UUID progressUUID, double initialTemperature, double minTemperature, double coolingRate, int k) {
        model.startGeneratingSolution(programName, progressUUID, initialTemperature, minTemperature, coolingRate, k);
    }

    @Override
    public void removeTimetable(Timetable timetable) {
        model.removeTimetable(timetable);
    }

    private void export(String programName, ModelInterface.ExportType type) {
        try {
            model.export(programName, type);
        } catch(IOException e) {
            view.showExceptionMessage(e);
            return;
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
