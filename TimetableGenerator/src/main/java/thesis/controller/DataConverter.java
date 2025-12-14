package thesis.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import thesis.model.ModelInterface;
import thesis.model.domain.components.*;
import thesis.model.exceptions.CheckedIllegalStateException;
import thesis.view.viewobjects.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DataConverter {
    private DataConverter() {}

    public static ObservableList<ViewModel> getCourses(String progName, ControllerInterface controller, ModelInterface model) {
        Collection<Course> courseList;
        try {
            courseList = model.getCourses(progName);
        } catch (CheckedIllegalStateException e) {
            controller.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> courseViewModels = courseList.stream()
                .map(c -> new CourseViewModel(c.getCourseId(), c.getConfigList().size()))
                .collect(Collectors.toList());

        return FXCollections.observableList(courseViewModels);
    }

    public static ObservableList<ViewModel> getConfigs(String progName, ControllerInterface controller, ModelInterface model) {
        Collection<Config> configList;
        try {
            configList = model.getConfigs(progName);
        } catch (CheckedIllegalStateException e) {
            controller.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> configViewModels = configList.stream()
                .map(c -> new ConfigViewModel(c.getConfigId(), c.getSubpartList().size()))
                .collect(Collectors.toList());

        return FXCollections.observableList(configViewModels);
    }

    public static ObservableList<ViewModel> getSubparts(String progName, ControllerInterface controller, ModelInterface model) {
        Collection<Subpart> subpartList;
        try {
            subpartList = model.getSubparts(progName);
        } catch (CheckedIllegalStateException e) {
            controller.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> subpartViewModels = subpartList.stream()
                .map(c -> new SubpartViewModel(c.getSubpartId(), c.getClassUnitList().size()))
                .collect(Collectors.toList());

        return FXCollections.observableList(subpartViewModels);
    }

    public static ObservableList<ViewModel> getClassUnits(String progName, ControllerInterface controller, ModelInterface model) {
        Collection<ClassUnit> classUnitList;
        try {
            classUnitList = model.getClassUnits(progName);
        } catch (CheckedIllegalStateException e) {
            controller.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> classUnitViewModels = classUnitList.stream()
                .map(c -> new ClassUnitViewModel(c.getClassId(), c.getParentClassId()))
                .collect(Collectors.toList());

        return FXCollections.observableList(classUnitViewModels);
    }

    public static ObservableList<ViewModel> getConfiguration(String progName, ControllerInterface controller, ModelInterface model) {
        TimetableConfiguration timetableConfiguration;
        try {
            timetableConfiguration = model.getConfiguration(progName);
        } catch (CheckedIllegalStateException e) {
            controller.showExceptionMessage(e);
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

    public static ObservableList<ViewModel> getConstraints(String progName, ControllerInterface controller, ModelInterface model) {
        Collection<Constraint> constraintList;
        try {
            constraintList = model.getConstraints(progName);
        } catch (CheckedIllegalStateException e) {
            controller.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> constraintViewModels = constraintList.stream()
                .map(c -> new ConstraintViewModel(c.getType(),
                        c.getFirstParameter(), c.getSecondParameter(), c.getPenalty(),
                        c.getRequired(), c.getClassUnitIdList().size()))
                .collect(Collectors.toList());

        return FXCollections.observableList(constraintViewModels);
    }

    public static ObservableList<ViewModel> getRooms(String progName, ControllerInterface controller, ModelInterface model) {
        Collection<Room> roomList;
        try {
            roomList = model.getRooms(progName);
        } catch (CheckedIllegalStateException e) {
            controller.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> roomViewModels = roomList.stream()
                .map(r -> new RoomViewModel(r.getRoomId(), r.getRoomUnavailabilities().size(), r.getRoomDistances().size()))
                .collect(Collectors.toList());

        return FXCollections.observableList(roomViewModels);
    }

    public static ObservableList<ViewModel> getTimetables(String progName, ControllerInterface controller, ModelInterface model) {
        Collection<Timetable> timetableList;
        try {
            timetableList = model.getTimetables(progName);
        } catch (CheckedIllegalStateException e) {
            controller.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> timetableViewModels = timetableList.stream()
                .map(t -> new TimetableViewModel(t.getDateOfCreationString(), t.getRuntime(), t.cost().getTotalPenalty(), t.getScheduledLessonList().size(), t.isValid(), t))
                .collect(Collectors.toList());

        return FXCollections.observableList(timetableViewModels);
    }

    public static ObservableList<ViewModel> getTeachers(String progName, ControllerInterface controller, ModelInterface model) {
        Collection<Teacher> teacherList;
        try {
            teacherList = model.getTeachers(progName);
        } catch (CheckedIllegalStateException e) {
            controller.showExceptionMessage(e);
            return null;
        }

        List<ViewModel> timetableViewModels = teacherList.stream()
                .map(t -> new TeacherViewModel(t.getId(), t.getName(), t.getTeacherUnavailabilities().size(), t.getTeacherClassList().size()))
                .collect(Collectors.toList());

        return FXCollections.observableList(timetableViewModels);
    }
}
