package thesis.view.viewobjects;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ClassUnitViewModel implements ViewModel {
    private final StringProperty id;
    private final StringProperty parentId;

    public ClassUnitViewModel(String id, String parentId) {
        this.id = new SimpleStringProperty(id);
        this.parentId = new SimpleStringProperty(parentId);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public String getParentId() {
        return parentId.get();
    }

    public StringProperty parentIdProperty() {
        return parentId;
    }
}
