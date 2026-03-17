package com.toi.utils;

import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * WaitUtils — fluent wrapper around WebDriverWait for common wait conditions.
 */
public class WaitUtils {

    private final IOSDriver driver;
    private final int defaultTimeoutSeconds;

    public WaitUtils(IOSDriver driver, int defaultTimeoutSeconds) {
        this.driver = driver;
        this.defaultTimeoutSeconds = defaultTimeoutSeconds;
    }

    // ── Visibility ────────────────────────────────────────────────────────────

    public WebElement forElementVisible(By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeoutSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement forElementVisible(By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement forElementVisible(WebElement element) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeoutSeconds))
                .until(ExpectedConditions.visibilityOf(element));
    }

    // ── Clickability ──────────────────────────────────────────────────────────

    public WebElement forElementClickable(By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeoutSeconds))
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement forElementClickable(WebElement element) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeoutSeconds))
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    // ── Presence ──────────────────────────────────────────────────────────────

    public WebElement forElementPresent(By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeoutSeconds))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // ── Invisibility ──────────────────────────────────────────────────────────

    public boolean forElementInvisible(By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeoutSeconds))
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // ── Text ──────────────────────────────────────────────────────────────────

    public boolean forTextInElement(By locator, String text) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeoutSeconds))
                .until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    // ── Custom timeout convenience ────────────────────────────────────────────

    public WebElement forElementVisible(By locator, int seconds) {
        return forElementVisible(locator, Duration.ofSeconds(seconds));
    }
}
