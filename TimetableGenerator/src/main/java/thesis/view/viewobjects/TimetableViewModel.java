package thesis.view.viewobjects;

import javafx.beans.property.*;
import thesis.model.domain.components.Timetable;

import java.time.LocalDateTime;

public class TimetableViewModel implements ViewModel {
    private final StringProperty dateOfCreation;
    private final LongProperty runtime;
    private final IntegerProperty cost;
    private final IntegerProperty nScheduledClasses;
    private final BooleanProperty isValid;
    private final Timetable timetable;

    public TimetableViewModel(String dateOfCreation, long runtime, int cost, int nScheduledClasses, boolean isValid, Timetable timetable) {
        this.dateOfCreation = new SimpleStringProperty(dateOfCreation);
        this.runtime = new SimpleLongProperty(runtime);
        this.cost = new SimpleIntegerProperty(cost);
        this.nScheduledClasses = new SimpleIntegerProperty(nScheduledClasses);
        this.isValid = new SimpleBooleanProperty(isValid);
        this.timetable = timetable;
    }

    public String getDateOfCreation() {
        return dateOfCreation.get();
    }

    public StringProperty dateOfCreationProperty() {
        return dateOfCreation;
    }

    public long getRuntime() {
        return runtime.get();
    }

    public LongProperty runtimeProperty() {
        return runtime;
    }

    public int getCost() {
        return cost.get();
    }

    public IntegerProperty costProperty() {
        return cost;
    }

    public int getnScheduledClasses() {
        return nScheduledClasses.get();
    }

    public IntegerProperty nScheduledClassesProperty() {
        return nScheduledClasses;
    }

    public boolean isIsValid() {
        return isValid.get();
    }

    public BooleanProperty isValidProperty() {
        return isValid;
    }

    public Timetable getTimetable() {
        return timetable;
    }
}
