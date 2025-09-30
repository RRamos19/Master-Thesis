package thesis.view.viewobjects;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SubpartViewModel implements ViewModel {
    private final StringProperty id;
    private final IntegerProperty nClasses;

    public SubpartViewModel(String id, int nClasses) {
        this.id = new SimpleStringProperty(id);
        this.nClasses = new SimpleIntegerProperty(nClasses);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public int getnClasses() {
        return nClasses.get();
    }

    public IntegerProperty nClassesProperty() {
        return nClasses;
    }
}
