package com.toi.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

/**
 * TestDataUtils — loads JSON test-data files from the classpath.
 *
 * Usage:
 *   JsonNode data = TestDataUtils.load("testdata/login.json");
 *   String email = data.get("validUser").get("email").asText();
 */
public class TestDataUtils {

    private static final Logger log = LogManager.getLogger(TestDataUtils.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private TestDataUtils() {}

    public static JsonNode load(String classpathResource) {
        try (InputStream in = TestDataUtils.class
                .getClassLoader()
                .getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new RuntimeException("Test data file not found: " + classpathResource);
            }
            JsonNode node = mapper.readTree(in);
            log.debug("Loaded test data: {}", classpathResource);
            return node;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data [" + classpathResource + "]: " + e.getMessage(), e);
        }
    }

    public static String getString(String classpathResource, String... keys) {
        JsonNode node = load(classpathResource);
        for (String key : keys) {
            node = node.get(key);
            if (node == null) {
                throw new RuntimeException("Key path not found in " + classpathResource + ": " + String.join(" -> ", keys));
            }
        }
        return node.asText();
    }
}
