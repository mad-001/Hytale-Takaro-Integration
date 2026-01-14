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
        properties.setProperty("TAKARO_WS_URL", "wss://connect.takaro.io/");
        properties.setProperty("IDENTITY_TOKEN", "");
        properties.setProperty("REGISTRATION_TOKEN", "");
        properties.setProperty("HYTALE_API_URL", "https://api.hytale.com");
        properties.setProperty("HYTALE_API_TOKEN", "");

        try {
            configFile.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                properties.store(fos, "Takaro Integration Configuration\n" +
                    "# Takaro Settings\n" +
                    "# IDENTITY_TOKEN and REGISTRATION_TOKEN from Takaro dashboard\n" +
                    "# \n" +
                    "# Hytale API Settings\n" +
                    "# HYTALE_API_TOKEN is your authenticated server token from Hytale");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getWsUrl() {
        return properties.getProperty("TAKARO_WS_URL", "wss://connect.takaro.io/");
    }

    public String getIdentityToken() {
        return properties.getProperty("IDENTITY_TOKEN", "");
    }

    public String getRegistrationToken() {
        return properties.getProperty("REGISTRATION_TOKEN", "");
    }

    public String getHytaleApiUrl() {
        return properties.getProperty("HYTALE_API_URL", "https://api.hytale.com");
    }

    public String getHytaleApiToken() {
        return properties.getProperty("HYTALE_API_TOKEN", "");
    }
}
