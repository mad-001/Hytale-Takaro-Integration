package dev.takaro.hytale.config;

import java.io.*;
import java.util.Properties;

public class TakaroConfig {
    private final Properties properties;
    private final File configFile;

    public TakaroConfig(File configFile) {
        this.configFile = configFile;
        this.properties = new Properties();
        load();
    }

    private void load() {
        if (!configFile.exists()) {
            createDefault();
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefault() {
        properties.setProperty("IDENTITY_TOKEN", "MyHytaleServer");
        properties.setProperty("REGISTRATION_TOKEN", "");

        try {
            configFile.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                properties.store(fos, "Takaro Integration Configuration\n" +
                    "# \n" +
                    "# IDENTITY_TOKEN: Choose a name for your server (e.g., MyHytaleServer, SurvivalServer, etc.)\n" +
                    "# REGISTRATION_TOKEN: Get this from Takaro dashboard (Settings → Game Servers → Add Server → Generic)\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getWsUrl() {
        // Hardcoded - users don't configure this
        return "wss://connect.takaro.io/";
    }

    public String getIdentityToken() {
        return properties.getProperty("IDENTITY_TOKEN", "");
    }

    public String getRegistrationToken() {
        return properties.getProperty("REGISTRATION_TOKEN", "");
    }

    // Hidden from default config - will be used when Hytale API is available
    public String getHytaleApiUrl() {
        return properties.getProperty("HYTALE_API_URL", "https://api.hytale.com");
    }

    public String getHytaleApiToken() {
        return properties.getProperty("HYTALE_API_TOKEN", "");
    }
}
