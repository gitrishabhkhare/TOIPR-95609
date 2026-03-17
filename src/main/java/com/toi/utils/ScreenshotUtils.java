package com.toi.utils;

import com.toi.config.ConfigReader;
import com.toi.config.DriverManager;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ScreenshotUtils — captures screenshots and saves them to the configured directory.
 */
public class ScreenshotUtils {

    private static final Logger log = LogManager.getLogger(ScreenshotUtils.class);
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ScreenshotUtils() {}

    /**
     * Captures a screenshot and returns the absolute file path.
     *
     * @param testName name used in the file name
     * @return absolute path to the saved screenshot, or null on failure
     */
    public static String capture(String testName) {
        try {
            String dir = ConfigReader.get("screenshots.dir", "screenshots");
            File destDir = new File(dir);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            String fileName = sanitize(testName) + "_" + LocalDateTime.now().format(FORMATTER) + ".png";
            File destFile = new File(destDir, fileName);

            File srcFile = ((TakesScreenshot) DriverManager.getDriver())
                    .getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, destFile);

            log.info("Screenshot saved: {}", destFile.getAbsolutePath());
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            log.error("Failed to capture screenshot: {}", e.getMessage());
            return null;
        }
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
