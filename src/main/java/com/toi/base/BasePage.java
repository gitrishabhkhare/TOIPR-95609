package com.toi.base;

import com.toi.config.ConfigReader;
import com.toi.config.DriverManager;
import com.toi.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;
import java.util.List;

/**
 * BasePage — parent of all page objects.
 * Provides common interaction helpers (tap, type, scroll, swipe, etc.).
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());
    protected final IOSDriver driver;
    protected final WaitUtils wait;

    protected BasePage() {
        this.driver = DriverManager.getDriver();
        this.wait   = new WaitUtils(driver,
                ConfigReader.getInt("explicit.wait"));
        PageFactory.initElements(driver, this);
    }

    // ── Element interactions ──────────────────────────────────────────────────

    protected void tap(By locator) {
        log.debug("Tap: {}", locator);
        wait.forElementClickable(locator).click();
    }

    protected void tap(WebElement element) {
        log.debug("Tap element");
        wait.forElementClickable(element).click();
    }

    protected void typeText(By locator, String text) {
        log.debug("Type '{}' into {}", text, locator);
        WebElement el = wait.forElementVisible(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected void typeText(WebElement element, String text) {
        log.debug("Type '{}' into element", text);
        wait.forElementVisible(element).clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return wait.forElementVisible(locator).getText();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return wait.forElementVisible(locator, Duration.ofSeconds(5)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    // ── Swipe / Scroll ────────────────────────────────────────────────────────

    /**
     * Scroll down by swiping from 80% to 20% of the screen height.
     */
    protected void scrollDown() {
        int height = driver.manage().window().getSize().getHeight();
        int width  = driver.manage().window().getSize().getWidth();
        driver.executeScript("mobile: scroll", java.util.Map.of(
                "direction", "down"
        ));
        log.debug("Scrolled down");
    }

    protected void scrollUp() {
        driver.executeScript("mobile: scroll", java.util.Map.of(
                "direction", "up"
        ));
        log.debug("Scrolled up");
    }

    protected void scrollToElement(String elementText) {
        driver.executeScript("mobile: scroll", java.util.Map.of(
                "direction", "down",
                "predicateString", "label == '" + elementText + "'"
        ));
    }

    // ── Accessibility / iOS-specific locators ────────────────────────────────

    protected By byAccessibilityId(String id) {
        return AppiumBy.accessibilityId(id);
    }

    protected By byPredicateString(String predicate) {
        return AppiumBy.iOSNsPredicateString(predicate);
    }

    protected By byClassChain(String chain) {
        return AppiumBy.iOSClassChain(chain);
    }

    // ── Page verification ─────────────────────────────────────────────────────

    /**
     * Every page must implement this to confirm it has loaded correctly.
     */
    public abstract boolean isLoaded();
}
