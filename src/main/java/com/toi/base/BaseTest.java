package com.toi.base;

import com.toi.config.ConfigReader;
import com.toi.config.DriverManager;
import com.toi.utils.ExtentReportManager;
import com.toi.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.*;

/**
 * BaseTest — parent of all test classes.
 * Handles driver lifecycle, reporting, and screenshot capture on failure.
 */
public abstract class BaseTest {

    protected static final Logger log = LogManager.getLogger(BaseTest.class);

    private static final java.util.concurrent.atomic.AtomicInteger passed  = new java.util.concurrent.atomic.AtomicInteger();
    private static final java.util.concurrent.atomic.AtomicInteger failed  = new java.util.concurrent.atomic.AtomicInteger();
    private static final java.util.concurrent.atomic.AtomicInteger skipped = new java.util.concurrent.atomic.AtomicInteger();

    // ── Suite-level hooks ─────────────────────────────────────────────────────

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        passed.set(0); failed.set(0); skipped.set(0);
        ExtentReportManager.initReport();
        log.info("=== Test Suite started ===");
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        ExtentReportManager.flushReport();
        log.info("=== Test Suite finished — Passed:{} Failed:{} Skipped:{} ===",
                passed.get(), failed.get(), skipped.get());
        String reportPath = ConfigReader.get("reports.dir", "reports") + "/SanityReport.html";
        com.toi.utils.EmailUtils.sendReport(reportPath, passed.get(), failed.get(), skipped.get());
    }

    // ── Method-level hooks ────────────────────────────────────────────────────

    @BeforeMethod(alwaysRun = true)
    public void setUp(java.lang.reflect.Method method) {
        log.info("--- Starting test: {} ---", method.getName());
        DriverManager.initDriver();
        // Restart the app to a clean state without clearing data (noReset=true means
        // terminate+activate gives us a fresh launch while keeping login sessions).
        String bundleId = ConfigReader.get("app.bundle.id");
        try {
            DriverManager.getDriver().terminateApp(bundleId);
            Thread.sleep(1000);
            DriverManager.getDriver().activateApp(bundleId);
            Thread.sleep(2000);
        } catch (Exception e) {
            log.warn("App restart skipped: {}", e.getMessage());
        }
        ExtentReportManager.createTest(method.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            failed.incrementAndGet();
            log.error("Test FAILED: {}", result.getName());
            String screenshotPath = ScreenshotUtils.capture(result.getName());
            ExtentReportManager.addScreenshot(screenshotPath);
            ExtentReportManager.logFail(result.getThrowable().getMessage());
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            passed.incrementAndGet();
            log.info("Test PASSED: {}", result.getName());
            ExtentReportManager.logPass("Test passed");
        } else {
            skipped.incrementAndGet();
            log.warn("Test SKIPPED: {}", result.getName());
            ExtentReportManager.logSkip("Test skipped");
        }
        DriverManager.quitDriver();
    }
}
