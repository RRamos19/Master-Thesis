package thesis.view;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ConfigurationManager {
    private static final String CONFIG_FILE = "config.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    private ConfigurationManager() {}

    public static generalConfiguration loadConfig() {
        try {
            File file = new File(CONFIG_FILE);
            if (file.exists()) {
                return mapper.readValue(file, generalConfiguration.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Default values
        generalConfiguration defaultValues = new generalConfiguration(
                true,
                null,
                1000,
                0.01,
                0.001,
                5,
                23,
                8);
        saveConfig(defaultValues);
        return defaultValues;
    }

    public static void saveConfig(generalConfiguration config) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(CONFIG_FILE), config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
