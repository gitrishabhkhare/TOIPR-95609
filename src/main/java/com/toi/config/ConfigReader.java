package com.toi.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton config reader — loads config.properties once and caches values.
 * Usage: ConfigReader.get("platform.name")
 */
public class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);
    private static final String CONFIG_FILE = "config.properties";
    private static Properties properties;

    private ConfigReader() {}

    public static synchronized String get(String key) {
        if (properties == null) {
            load();
        }
        String value = System.getProperty(key);          // CLI overrides take priority
        if (value == null) {
            value = properties.getProperty(key);
        }
        if (value == null) {
            throw new RuntimeException("Config key not found: " + key);
        }
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        try {
            return get(key);
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    private static void load() {
        properties = new Properties();
        try (InputStream in = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new RuntimeException("Cannot find " + CONFIG_FILE + " on classpath");
            }
            properties.load(in);
            log.info("Loaded configuration from {}", CONFIG_FILE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + e.getMessage(), e);
        }
    }
}
