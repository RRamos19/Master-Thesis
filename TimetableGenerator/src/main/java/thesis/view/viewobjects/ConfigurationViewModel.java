package thesis.view.viewobjects;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class ConfigurationViewModel implements ViewModel {
    private final IntegerProperty nDays;
    private final IntegerProperty nWeeks;
    private final IntegerProperty nSlots;
    private final IntegerProperty timeWeight;
    private final IntegerProperty roomWeight;
    private final IntegerProperty distributionWeight;

    public ConfigurationViewModel(int nDays, int nWeeks, int nSlots, int timeWeight, int roomWeight, int distributionWeight) {
        this.nDays = new SimpleIntegerProperty(nDays);
        this.nWeeks = new SimpleIntegerProperty(nWeeks);
        this.nSlots = new SimpleIntegerProperty(nSlots);
        this.timeWeight = new SimpleIntegerProperty(timeWeight);
        this.roomWeight = new SimpleIntegerProperty(roomWeight);
        this.distributionWeight = new SimpleIntegerProperty(distributionWeight);
    }

    public int getnDays() {
        return nDays.get();
    }

    public IntegerProperty nDaysProperty() {
        return nDays;
    }

    public int getnWeeks() {
        return nWeeks.get();
    }

    public IntegerProperty nWeeksProperty() {
        return nWeeks;
    }

    public int getnSlots() {
        return nSlots.get();
    }

    public IntegerProperty nSlotsProperty() {
        return nSlots;
    }

    public int getTimeWeight() {
        return timeWeight.get();
    }

    public IntegerProperty timeWeightProperty() {
        return timeWeight;
    }

    public int getRoomWeight() {
        return roomWeight.get();
    }

    public IntegerProperty roomWeightProperty() {
        return roomWeight;
    }

    public int getDistributionWeight() {
        return distributionWeight.get();
    }

    public IntegerProperty distributionWeightProperty() {
        return distributionWeight;
    }
}
