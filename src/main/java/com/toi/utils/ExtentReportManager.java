package com.toi.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.toi.config.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ExtentReportManager — thread-safe Extent Reports wrapper.
 * Generates an HTML report in the configured reports directory.
 */
public class ExtentReportManager {

    private static final Logger log = LogManager.getLogger(ExtentReportManager.class);
    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();

    private ExtentReportManager() {}

    public static synchronized void initReport() {
        String dir = ConfigReader.get("reports.dir", "reports");
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String reportPath = dir + "/SanityReport.html";

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("TOI iOS Automation Report");
        spark.config().setReportName("TOI iOS — Test Execution Report");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Platform", ConfigReader.get("platform.name", "iOS"));
        extent.setSystemInfo("Device", ConfigReader.get("device.name", "N/A"));
        extent.setSystemInfo("iOS Version", ConfigReader.get("platform.version", "N/A"));
        log.info("Extent Report initialised at: {}", reportPath);
    }

    public static void createTest(String testName) {
        ExtentTest test = extent.createTest(testName);
        testThread.set(test);
    }

    public static void logPass(String message) {
        getTest().pass(message);
    }

    public static void logFail(String message) {
        getTest().fail(message);
    }

    public static void logSkip(String message) {
        getTest().skip(message);
    }

    public static void logInfo(String message) {
        getTest().info(message);
    }

    public static void addScreenshot(String screenshotPath) {
        if (screenshotPath != null) {
            try {
                getTest().fail("Screenshot on failure",
                        MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
            } catch (Exception e) {
                log.warn("Could not attach screenshot to report: {}", e.getMessage());
            }
        }
    }

    public static synchronized void flushReport() {
        if (extent != null) {
            extent.flush();
            log.info("Extent Report flushed.");
        }
    }

    private static ExtentTest getTest() {
        ExtentTest test = testThread.get();
        if (test == null) {
            throw new IllegalStateException("ExtentTest not initialised for current thread. Call createTest() first.");
        }
        return test;
    }
}
