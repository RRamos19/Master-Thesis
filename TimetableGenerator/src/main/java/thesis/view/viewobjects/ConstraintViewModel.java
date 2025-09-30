package thesis.view.viewobjects;

import javafx.beans.property.*;

public class ConstraintViewModel implements ViewModel {
    private final StringProperty type;
    private final ObjectProperty<Integer> firstParameter;
    private final ObjectProperty<Integer> secondParameter;
    private final ObjectProperty<Integer> penalty;
    private final BooleanProperty required;
    private final IntegerProperty nClasses;

    public ConstraintViewModel(String type, Integer firstParameter, Integer secondParameter, Integer penalty, boolean required, int nClasses) {
        this.type = new SimpleStringProperty(type);
        this.firstParameter = new SimpleObjectProperty<>(firstParameter);
        this.secondParameter = new SimpleObjectProperty<>(secondParameter);
        this.penalty = new SimpleObjectProperty<>(penalty);
        this.required = new SimpleBooleanProperty(required);
        this.nClasses = new SimpleIntegerProperty(nClasses);
    }

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public int getFirstParameter() {
        return firstParameter.get();
    }

    public ObjectProperty<Integer> firstParameterProperty() {
        return firstParameter;
    }

    public int getSecondParameter() {
        return secondParameter.get();
    }

    public ObjectProperty<Integer> secondParameterProperty() {
        return secondParameter;
    }

    public int getPenalty() {
        return penalty.get();
    }

    public ObjectProperty<Integer> penaltyProperty() {
        return penalty;
    }

    public boolean isRequired() {
        return required.get();
    }

    public BooleanProperty requiredProperty() {
        return required;
    }

    public int getnClasses() {
        return nClasses.get();
    }

    public IntegerProperty nClassesProperty() {
        return nClasses;
    }
}
