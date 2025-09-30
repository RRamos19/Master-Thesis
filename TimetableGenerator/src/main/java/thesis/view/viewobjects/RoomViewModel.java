package thesis.view.viewobjects;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RoomViewModel implements ViewModel {
    private final StringProperty id;
    private final IntegerProperty nUnavailabilities;
    private final IntegerProperty nDistances;

    public RoomViewModel(String id, int nUnavailabilities, int nDistances) {
        this.id = new SimpleStringProperty(id);
        this.nUnavailabilities = new SimpleIntegerProperty(nUnavailabilities);
        this.nDistances = new SimpleIntegerProperty(nDistances);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public int getnUnavailabilities() {
        return nUnavailabilities.get();
    }

    public IntegerProperty nUnavailabilitiesProperty() {
        return nUnavailabilities;
    }

    public int getnDistances() {
        return nDistances.get();
    }

    public IntegerProperty nDistancesProperty() {
        return nDistances;
    }
}
