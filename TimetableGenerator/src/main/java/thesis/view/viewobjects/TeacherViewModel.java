package thesis.view.viewobjects;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TeacherViewModel implements ViewModel {
    private final IntegerProperty id;
    private final StringProperty name;
    private final IntegerProperty nUnavailabilities;
    private final IntegerProperty nClasses;

    public TeacherViewModel(int id, String name, int nUnavailabilities, int nClasses) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.nUnavailabilities = new SimpleIntegerProperty(nUnavailabilities);
        this.nClasses = new SimpleIntegerProperty(nClasses);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public int getnUnavailabilities() {
        return nUnavailabilities.get();
    }

    public IntegerProperty nUnavailabilitiesProperty() {
        return nUnavailabilities;
    }

    public int getnClasses() {
        return nClasses.get();
    }

    public IntegerProperty nClassesProperty() {
        return nClasses;
    }
}
