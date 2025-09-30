package thesis.view.viewobjects;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CourseViewModel implements ViewModel {
    private final StringProperty id;
    private final IntegerProperty nConfigs;

    public CourseViewModel(String id, int nConfigs) {
        this.id = new SimpleStringProperty(id);
        this.nConfigs = new SimpleIntegerProperty(nConfigs);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public int getnConfigs() {
        return nConfigs.get();
    }

    public IntegerProperty nConfigsProperty() {
        return nConfigs;
    }
}
