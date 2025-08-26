package thesis.view;

import java.beans.ConstructorProperties;

public class generalConfiguration {
    private final Integer initialSolutionMaxIterations;
    private final double initialTemperature;
    private final double minTemperature;
    private final double coolingRate;
    private final int k;
    private final int maxHour;
    private final int minHour;
    private boolean showTutorial;

    @ConstructorProperties({"showTutorial", "initialSolutionMaxIterations","initialTemperature","minTemperature","coolingRate","k", "maxHour", "minHour"})
    public generalConfiguration(boolean showTutorial, Integer initialSolutionMaxIterations, double initialTemperature, double minTemperature, double coolingRate, int k, int maxHour, int minHour) {
        this.showTutorial = showTutorial;
        this.initialSolutionMaxIterations = initialSolutionMaxIterations;
        this.initialTemperature = initialTemperature;
        this.minTemperature = minTemperature;
        this.coolingRate = coolingRate;
        this.k = k;
        this.maxHour = maxHour;
        this.minHour = minHour;
    }

    public Integer getInitialSolutionMaxIterations() {
        return initialSolutionMaxIterations;
    }

    public double getInitialTemperature() {
        return initialTemperature;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getCoolingRate() {
        return coolingRate;
    }

    public int getK() {
        return k;
    }

    public int getMaxHour() {
        return maxHour;
    }

    public int getMinHour() {
        return minHour;
    }

    public boolean getShowTutorial() {
        return showTutorial;
    }

    public void tutorialShown() {
        showTutorial = false;
    }
}
