package thesis.view.managers.components;

import java.beans.ConstructorProperties;

public class GeneralConfiguration {
    // Flag which indicates if the config file must be updated
    private boolean updateConfigFile;

    private Integer initialSolutionMaxIterations;
    private double initialTemperature;
    private double minTemperature;
    private double coolingRate;
    private int k;
    private int maxHour;
    private int minHour;
    private boolean showTutorial;

    @ConstructorProperties({"showTutorial", "initialSolutionMaxIterations","initialTemperature","minTemperature","coolingRate","k", "maxHour", "minHour"})
    public GeneralConfiguration(boolean showTutorial, Integer initialSolutionMaxIterations, double initialTemperature, double minTemperature, double coolingRate, int k, int maxHour, int minHour) {
        this.showTutorial = showTutorial;
        this.initialSolutionMaxIterations = initialSolutionMaxIterations;
        this.initialTemperature = initialTemperature;
        this.minTemperature = minTemperature;
        this.coolingRate = coolingRate;
        this.k = k;
        this.maxHour = maxHour;
        this.minHour = minHour;
    }

    public boolean getUpdateConfigFile() {
        return updateConfigFile;
    }

    public Integer getInitialSolutionMaxIterations() {
        return initialSolutionMaxIterations;
    }

    public void setInitialSolutionMaxIterations(Integer maxIterations) {
        initialSolutionMaxIterations = maxIterations;
        setUpdateConfigFile();
    }

    public double getInitialTemperature() {
        return initialTemperature;
    }

    public void setInitialTemperature(double initialTemperature) {
        this.initialTemperature = initialTemperature;
        setUpdateConfigFile();
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(double minTemperature) {
        this.minTemperature = minTemperature;
        setUpdateConfigFile();
    }

    public double getCoolingRate() {
        return coolingRate;
    }

    public void setCoolingRate(double coolingRate) {
        this.coolingRate = coolingRate;
        setUpdateConfigFile();
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
        setUpdateConfigFile();
    }

    public int getMaxHour() {
        return maxHour;
    }

    public void setMaxHour(int maxHour) {
        this.maxHour = maxHour;
        setUpdateConfigFile();
    }

    public int getMinHour() {
        return minHour;
    }

    public void setMinHour(int minHour) {
        this.minHour = minHour;
        setUpdateConfigFile();
    }

    public boolean getShowTutorial() {
        return showTutorial;
    }

    public void setTutorialShown() {
        showTutorial = false;
        setUpdateConfigFile();
    }

    private void setUpdateConfigFile() {
        updateConfigFile = true;
    }
}
