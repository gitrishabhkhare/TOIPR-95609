package com.toi.utils;

import io.appium.java_client.appmanagement.ApplicationState;
import io.appium.java_client.ios.IOSDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * MonkeyUtils — helper methods for monkey (random) testing.
 *
 * All methods are stateless and take an IOSDriver as parameter
 * so they can be shared across multiple test classes.
 */
public class MonkeyUtils {

    private static final Logger log = LogManager.getLogger(MonkeyUtils.class);
    private static final Random random = new Random();

    // ── Fuzz inputs for text-field stress testing ─────────────────────────────

    public static final List<String> FUZZ_INPUTS = Arrays.asList(
            "",                                     // empty string
            " ",                                    // single whitespace
            "a",                                    // minimal single char
            "12345",                                // digits only
            "!@#$%^&*()",                           // special characters
            "' OR 1=1 --",                          // SQL injection probe
            "<script>alert(1)</script>",            // XSS probe
            "😀🔥💥🎉🚀",                           // emoji sequence
            "A".repeat(500),                        // very long string (500 chars)
            "\n\t\r",                               // control characters
            "null",                                 // "null" literal
            "undefined",                            // "undefined" literal
            "सुप्रभात",                              // Hindi unicode
            "中文测试",                               // Chinese unicode
            "0/0"                                   // arithmetic edge case
    );

    // ── Event types for the chaos mix ────────────────────────────────────────

    public enum EventType {
        RANDOM_TAP,
        SCROLL_DOWN,
        SCROLL_UP,
        SWIPE_LEFT,
        SWIPE_RIGHT,
        DOUBLE_TAP,
        LONG_PRESS,
        TAP_ELEMENT,
        NAVIGATE_BACK,
        DISMISS_ALERT,
        PINCH_IN,
        PINCH_OUT,
        TYPE_FUZZ_TEXT
    }

    // ── Coordinate helpers ────────────────────────────────────────────────────

    /** Returns a random X coordinate staying within 10–90 % of screen width. */
    private static int safeX(Dimension size) {
        return random.nextInt((int) (size.width * 0.8)) + (int) (size.width * 0.1);
    }

    /** Returns a random Y coordinate staying within 22–80 % of screen height (avoids status bar, notification centre, and home indicator). */
    private static int safeY(Dimension size) {
        return random.nextInt((int) (size.height * 0.58)) + (int) (size.height * 0.22);
    }

    // ── Core interactions ─────────────────────────────────────────────────────

    /**
     * Tap at a random safe coordinate on the screen.
     */
    public static void randomTap(IOSDriver driver) {
        Dimension size = driver.manage().window().getSize();
        int x = safeX(size);
        int y = safeY(size);
        log.info("Monkey: tap at ({}, {})", x, y);
        try {
            driver.executeScript("mobile: tap", Map.of("x", x, "y", y));
        } catch (Exception e) {
            log.warn("Monkey: tap failed — {}", e.getMessage());
        }
    }

    /**
     * Scroll down one page using W3C touch actions (bypasses app main-thread blocking).
     */
    public static void scrollDown(IOSDriver driver) {
        log.info("Monkey: scroll down");
        try {
            Dimension size = driver.manage().window().getSize();
            int x = size.width / 2;
            int startY = (int) (size.height * 0.75);
            int endY   = (int) (size.height * 0.25);
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1);
            swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, startY));
            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(400), PointerInput.Origin.viewport(), x, endY));
            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(Collections.singletonList(swipe));
        } catch (Exception e) {
            log.warn("Monkey: scroll down failed — {}", e.getMessage());
        }
    }

    /**
     * Scroll up one page using W3C touch actions (bypasses app main-thread blocking).
     */
    public static void scrollUp(IOSDriver driver) {
        log.info("Monkey: scroll up");
        try {
            Dimension size = driver.manage().window().getSize();
            int x = size.width / 2;
            int startY = (int) (size.height * 0.25);
            int endY   = (int) (size.height * 0.75);
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1);
            swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, startY));
            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(400), PointerInput.Origin.viewport(), x, endY));
            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(Collections.singletonList(swipe));
        } catch (Exception e) {
            log.warn("Monkey: scroll up failed — {}", e.getMessage());
        }
    }

    /**
     * Swipe left using W3C touch actions (in-app carousel/navigation swipe).
     * Starts from right-centre, ends at left-centre — never triggers the iOS
     * system back-to-previous-app edge gesture.
     */
    public static void swipeLeft(IOSDriver driver) {
        log.info("Monkey: swipe left");
        try {
            Dimension size = driver.manage().window().getSize();
            int startX = (int) (size.width * 0.80);
            int endX   = (int) (size.width * 0.20);
            int y      = size.height / 2;
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1);
            swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, y));
            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(300), PointerInput.Origin.viewport(), endX, y));
            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(Collections.singletonList(swipe));
        } catch (Exception e) {
            log.warn("Monkey: swipe left failed — {}", e.getMessage());
        }
    }

    /**
     * Swipe right (in-app back gesture) using W3C touch actions.
     * Starts at x=40pt (inside the app, not the very edge) to avoid triggering
     * the iOS system app-switcher or home-screen gesture.
     */
    public static void swipeRight(IOSDriver driver) {
        log.info("Monkey: swipe right (back gesture)");
        try {
            Dimension size = driver.manage().window().getSize();
            int startX = 40;
            int endX   = (int) (size.width * 0.70);
            int y      = size.height / 2;
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1);
            swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, y));
            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(300), PointerInput.Origin.viewport(), endX, y));
            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(Collections.singletonList(swipe));
        } catch (Exception e) {
            log.warn("Monkey: swipe right failed — {}", e.getMessage());
        }
    }

    /**
     * Double-tap at a random safe coordinate.
     */
    public static void randomDoubleTap(IOSDriver driver) {
        Dimension size = driver.manage().window().getSize();
        int x = safeX(size);
        int y = safeY(size);
        log.info("Monkey: double-tap at ({}, {})", x, y);
        try {
            driver.executeScript("mobile: doubleTap", Map.of("x", x, "y", y));
        } catch (Exception e) {
            log.warn("Monkey: double-tap failed — {}", e.getMessage());
        }
    }

    /**
     * Long-press at a random safe coordinate for 1–3 seconds.
     */
    public static void randomLongPress(IOSDriver driver) {
        Dimension size = driver.manage().window().getSize();
        int x = safeX(size);
        int y = safeY(size);
        double duration = 1.0 + random.nextDouble() * 2.0;
        log.info("Monkey: long-press at ({}, {}) for {:.1f}s", x, y, duration);
        try {
            driver.executeScript("mobile: touchAndHold",
                    Map.of("x", x, "y", y, "duration", duration));
        } catch (Exception e) {
            log.warn("Monkey: long-press failed — {}", e.getMessage());
        }
    }

    /**
     * Pinch gesture to zoom IN (spread — scale > 1).
     */
    public static void pinchZoomIn(IOSDriver driver) {
        double scale = 1.5 + random.nextDouble() * 0.5; // 1.5–2.0
        log.info("Monkey: pinch zoom-in (scale={})", String.format("%.2f", scale));
        try {
            driver.executeScript("mobile: pinch",
                    Map.of("scale", scale, "velocity", 1.0 + random.nextDouble()));
        } catch (Exception e) {
            log.warn("Monkey: pinch zoom-in failed — {}", e.getMessage());
        }
    }

    /**
     * Pinch gesture to zoom OUT (pinch — scale < 1).
     */
    public static void pinchZoomOut(IOSDriver driver) {
        double scale = 0.3 + random.nextDouble() * 0.4; // 0.3–0.7
        log.info("Monkey: pinch zoom-out (scale={})", String.format("%.2f", scale));
        try {
            driver.executeScript("mobile: pinch",
                    Map.of("scale", scale, "velocity", -(1.0 + random.nextDouble())));
        } catch (Exception e) {
            log.warn("Monkey: pinch zoom-out failed — {}", e.getMessage());
        }
    }

    /**
     * Tap a random interactive element (Button, Cell, or Link) visible on screen.
     * Falls back to a random coordinate tap if no interactive elements are found.
     */
    public static void tapRandomElement(IOSDriver driver) {
        try {
            List<WebElement> elements = driver.findElements(
                    By.xpath("//XCUIElementTypeButton | //XCUIElementTypeCell | //XCUIElementTypeLink"));
            if (!elements.isEmpty()) {
                WebElement el = elements.get(random.nextInt(elements.size()));
                String label = el.getAttribute("label");
                log.info("Monkey: tapping element '{}'", label != null ? label : "(no label)");
                el.click();
            } else {
                log.info("Monkey: no interactive elements found — falling back to random tap");
                randomTap(driver);
            }
        } catch (Exception e) {
            log.warn("Monkey: tapRandomElement failed — {}", e.getMessage());
            randomTap(driver);
        }
    }

    /**
     * Navigate back: tries the iOS Back button first, then falls back to swipe-right.
     */
    public static void navigateBack(IOSDriver driver) {
        log.info("Monkey: navigate back");
        try {
            List<WebElement> backButtons = driver.findElements(
                    By.xpath("//XCUIElementTypeButton[@name='Back' or @label='Back']"));
            if (!backButtons.isEmpty()) {
                backButtons.get(0).click();
            } else {
                swipeRight(driver);
            }
        } catch (Exception e) {
            log.warn("Monkey: navigateBack failed — {}", e.getMessage());
        }
    }

    /**
     * Dismiss any system alert/dialog that may have appeared by tapping the first button.
     */
    public static void dismissAlertIfPresent(IOSDriver driver) {
        try {
            List<WebElement> alerts = driver.findElements(By.xpath("//XCUIElementTypeAlert"));
            if (!alerts.isEmpty()) {
                log.info("Monkey: dismissing system alert");
                List<WebElement> btns = driver.findElements(
                        By.xpath("//XCUIElementTypeAlert//XCUIElementTypeButton"));
                if (!btns.isEmpty()) {
                    btns.get(0).click();
                }
            }
        } catch (Exception e) {
            log.warn("Monkey: dismissAlert failed — {}", e.getMessage());
        }
    }

    /**
     * Type a random fuzz string into any visible text field / search field.
     * Does nothing if no editable field is on screen.
     */
    public static void typeRandomFuzzText(IOSDriver driver) {
        try {
            List<WebElement> fields = driver.findElements(
                    By.xpath("//XCUIElementTypeTextField | //XCUIElementTypeTextView | //XCUIElementTypeSearchField"));
            if (!fields.isEmpty()) {
                String text = FUZZ_INPUTS.get(random.nextInt(FUZZ_INPUTS.size()));
                WebElement field = fields.get(random.nextInt(fields.size()));
                String preview = text.length() > 30 ? text.substring(0, 30) + "…" : text;
                log.info("Monkey: typing '{}' into text field", preview);
                field.sendKeys(text);
            }
        } catch (Exception e) {
            log.warn("Monkey: typeRandomFuzzText failed — {}", e.getMessage());
        }
    }

    /**
     * Fire one random event from any EventType.
     */
    public static void fireRandomEvent(IOSDriver driver) {
        EventType[] types = EventType.values();
        EventType type = types[random.nextInt(types.length)];
        log.info("Monkey: firing event type = {}", type);
        switch (type) {
            case RANDOM_TAP:
                randomTap(driver);
                break;
            case SCROLL_DOWN:
                scrollDown(driver);
                break;
            case SCROLL_UP:
                scrollUp(driver);
                break;
            case SWIPE_LEFT:
                swipeLeft(driver);
                break;
            case SWIPE_RIGHT:
                swipeRight(driver);
                break;
            case DOUBLE_TAP:
                randomDoubleTap(driver);
                break;
            case LONG_PRESS:
                randomLongPress(driver);
                break;
            case TAP_ELEMENT:
                tapRandomElement(driver);
                break;
            case NAVIGATE_BACK:
                navigateBack(driver);
                break;
            case DISMISS_ALERT:
                dismissAlertIfPresent(driver);
                break;
            case PINCH_IN:
                pinchZoomIn(driver);
                break;
            case PINCH_OUT:
                pinchZoomOut(driver);
                break;
            case TYPE_FUZZ_TEXT:
                typeRandomFuzzText(driver);
                break;
            default:
                log.warn("Monkey: unknown event type {}", type);
                break;
        }
    }

    /**
     * Check whether the app is still running in the foreground (not crashed/backgrounded).
     */
    public static boolean isAppAlive(IOSDriver driver, String bundleId) {
        try {
            ApplicationState state = driver.queryAppState(bundleId);
            boolean alive = state == ApplicationState.RUNNING_IN_FOREGROUND;
            if (!alive) {
                log.error("App is NOT in foreground! Current state: {}", state);
            }
            return alive;
        } catch (Exception e) {
            log.error("Could not query app state: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Short random pause (50–300 ms) to let the UI settle between events.
     */
    public static void shortPause() {
        try {
            Thread.sleep(50 + random.nextInt(250));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
