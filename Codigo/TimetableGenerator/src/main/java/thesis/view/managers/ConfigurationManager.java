package thesis.view.managers;

import com.fasterxml.jackson.databind.ObjectMapper;
import thesis.view.managers.components.GeneralConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigurationManager {
    private static final String CONFIG_FILE = "config.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    private ConfigurationManager() {}

    public static GeneralConfiguration loadConfig() throws IOException {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            return mapper.readValue(file, GeneralConfiguration.class);
        }

        // Default values
        GeneralConfiguration defaultValues = new GeneralConfiguration();
        saveConfig(defaultValues);
        return defaultValues;
    }

    public static void saveConfig(GeneralConfiguration config) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(CONFIG_FILE), config);
    }
}
