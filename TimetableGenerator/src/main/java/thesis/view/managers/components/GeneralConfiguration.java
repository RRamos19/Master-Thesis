package thesis.view.managers.components;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"updateConfigFile"})
public class GeneralConfiguration {
    // Flag which indicates if the config file must be updated
    private boolean updateConfigFile = false;

    private double initialTemperature = 200;
    private double minTemperature = 1e-4;
    private double coolingRate = 0.05;
    private int k = 5;
    private int maxHour = 23;
    private int minHour = 8;
    private boolean showInstructions = true;
    private String ipField = "";
    private String portField = "";
    private String usernameField = "";

    public GeneralConfiguration() {}

    public boolean getUpdateConfigFile() {
        return updateConfigFile;
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

    public boolean getShowInstructions() {
        return showInstructions;
    }

    public void setShowInstructions(boolean showInstructions) {
        this.showInstructions = showInstructions;
        setUpdateConfigFile();
    }

    public String getIpField() {
        return ipField;
    }

    public void setIpField(String ipField) {
        this.ipField = ipField;
        setUpdateConfigFile();
    }

    public String getPortField() {
        return portField;
    }

    public void setPortField(String portField) {
        this.portField = portField;
        setUpdateConfigFile();
    }

    public String getUsernameField() {
        return usernameField;
    }

    public void setUsernameField(String usernameField) {
        this.usernameField = usernameField;
        setUpdateConfigFile();
    }

    private void setUpdateConfigFile() {
        updateConfigFile = true;
    }
}
