package thesis.view.viewobjects;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ConfigViewModel implements ViewModel {
    private final StringProperty id;
    private final IntegerProperty nSubparts;

    public ConfigViewModel(String id, int nSubparts) {
        this.id = new SimpleStringProperty(id);
        this.nSubparts = new SimpleIntegerProperty(nSubparts);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public int getnSubparts() {
        return nSubparts.get();
    }

    public IntegerProperty nSubpartsProperty() {
        return nSubparts;
    }
}
