package com.toi.config;

import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.net.URL;
import java.time.Duration;

/**
 * Thread-safe driver manager using ThreadLocal — supports parallel test execution.
 */
public class DriverManager {

    private static final Logger log = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<IOSDriver> driverThread = new ThreadLocal<>();

    private DriverManager() {}

    public static void initDriver() {
        if (driverThread.get() != null) {
            log.warn("Driver already initialized for this thread — skipping re-init.");
            return;
        }
        try {
            XCUITestOptions options = buildOptions();
            String serverUrl = ConfigReader.get("appium.server.url");
            IOSDriver driver = new IOSDriver(new URL(serverUrl), options);
            driver.manage().timeouts()
                    .implicitlyWait(Duration.ofSeconds(ConfigReader.getInt("implicit.wait")));
            driverThread.set(driver);
            log.info("IOSDriver initialized. Device: {}, iOS: {}",
                    ConfigReader.get("device.name"), ConfigReader.get("platform.version"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize IOSDriver: " + e.getMessage(), e);
        }
    }

    public static IOSDriver getDriver() {
        IOSDriver driver = driverThread.get();
        if (driver == null) {
            throw new IllegalStateException("Driver not initialized. Call initDriver() first.");
        }
        return driver;
    }

    public static void quitDriver() {
        IOSDriver driver = driverThread.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("IOSDriver session closed.");
            } catch (Exception e) {
                log.warn("Error while quitting driver: {}", e.getMessage());
            } finally {
                driverThread.remove();
            }
        }
    }

    private static XCUITestOptions buildOptions() {
        XCUITestOptions options = new XCUITestOptions();
        options.setPlatformName(ConfigReader.get("platform.name"));
        options.setPlatformVersion(ConfigReader.get("platform.version"));
        options.setDeviceName(ConfigReader.get("device.name"));
        options.setAutomationName(ConfigReader.get("automation.name"));

        // Pin to exact UDID — avoids ambiguity when multiple devices are connected
        String udid = ConfigReader.get("device.udid", "");
        if (!udid.isEmpty()) {
            options.setUdid(udid);
        }

        // Use bundleId (app pre-installed via TestFlight/MDM); fall back to .app path for simulators
        String bundleId = ConfigReader.get("app.bundle.id", "");
        String appPath  = ConfigReader.get("app.path", "");
        if (!bundleId.isEmpty()) {
            options.setBundleId(bundleId);
        } else if (!appPath.isEmpty()) {
            options.setApp(appPath);
        } else {
            throw new RuntimeException("Either app.bundle.id or app.path must be set in config.properties");
        }

        // WDA signing — required for real device automation
        String xcodeOrgId = ConfigReader.get("xcode.org.id", "");
        String xcodeSigningId = ConfigReader.get("xcode.signing.id", "Apple Development");
        if (!xcodeOrgId.isEmpty()) {
            options.setCapability("appium:xcodeOrgId", xcodeOrgId);
            options.setCapability("appium:xcodeSigningId", xcodeSigningId);
        }

        // App is pre-installed — do not reinstall or reset data
        options.setNoReset(true);
        options.setFullReset(false);
        options.setNewCommandTimeout(Duration.ofSeconds(120));

        // Reuse already-running WDA instead of killing/rebuilding it each session — major speed gain
        options.setCapability("appium:useNewWDA", false);
        // Skip WDA reinstall — WDA is already running, connect via tunnel
        options.setCapability("appium:skipServerInstallation", true);
        options.setCapability("appium:usePrebuiltWDA", true);
        // Give WDA extra time to launch on iOS 26.x before timing out
        options.setCapability("appium:wdaLaunchTimeout", 120000);
        options.setCapability("appium:wdaConnectionTimeout", 120000);
        return options;
    }
}
