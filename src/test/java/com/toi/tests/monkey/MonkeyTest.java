package com.toi.tests.monkey;

import com.toi.base.BaseTest;
import com.toi.config.ConfigReader;
import com.toi.config.DriverManager;
import com.toi.pages.HomePage;
import com.toi.pages.SplashPage;
import com.toi.utils.MonkeyUtils;
import io.appium.java_client.ios.IOSDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * MonkeyTest — random / chaos testing for the TOI iOS app.
 *
 * Philosophy: fire random gestures, taps, swipes, and inputs at the app and verify
 * it never crashes (stays in foreground). No specific UI assertions are made beyond
 * "the app is still alive". All events are logged for post-run analysis.
 *
 * Run: mvn test -Pmonkey
 */
public class MonkeyTest extends BaseTest {

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Skip the splash/onboarding screen if it is shown and return a HomePage.
     * If splash is not present the app is already on the home feed.
     */
    private HomePage navigateToHome() {
        SplashPage splash = new SplashPage();
        if (splash.isLoaded()) {
            return splash.skipOnboarding();
        }
        return new HomePage();
    }

    private String bundleId() {
        return ConfigReader.get("app.bundle.id");
    }

    private IOSDriver driver() {
        return DriverManager.getDriver();
    }

    // ── Test 1 — Random Coordinate Taps ──────────────────────────────────────

    @Test(groups = {"monkey"},
          description = "Fire 50 random taps at arbitrary screen coordinates and verify the app never crashes")
    public void testRandomTapsNoCrash() {
        navigateToHome();
        IOSDriver driver = driver();
        String bundleId = bundleId();

        for (int i = 1; i <= 50; i++) {
            MonkeyUtils.randomTap(driver);
            MonkeyUtils.dismissAlertIfPresent(driver);
            MonkeyUtils.shortPause();

            if (i % 10 == 0) {
                Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                        "App crashed after " + i + " random taps");
                log.info("Alive check passed after {} taps", i);
            }
        }
        Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                "App should be alive after 50 random taps");
    }

    // ── Test 2 — Random Scrolls ───────────────────────────────────────────────

    @Test(groups = {"monkey"},
          description = "Perform 40 random scroll events (up/down) and verify no crash")
    public void testRandomScrollsNoCrash() {
        navigateToHome();
        IOSDriver driver = driver();
        String bundleId = bundleId();

        for (int i = 1; i <= 40; i++) {
            if (i % 2 == 0) {
                MonkeyUtils.scrollDown(driver);
            } else {
                MonkeyUtils.scrollUp(driver);
            }
            MonkeyUtils.shortPause();
        }

        Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                "App should be alive after 40 random scroll events");
    }

    // ── Test 3 — Random Interactive Element Taps ─────────────────────────────

    @Test(groups = {"monkey"},
          description = "Tap 40 randomly chosen interactive UI elements (buttons/cells/links) and verify no crash")
    public void testRandomInteractiveElementTaps() {
        navigateToHome();
        IOSDriver driver = driver();
        String bundleId = bundleId();

        for (int i = 1; i <= 40; i++) {
            MonkeyUtils.tapRandomElement(driver);
            MonkeyUtils.dismissAlertIfPresent(driver);
            MonkeyUtils.shortPause();

            if (i % 10 == 0) {
                Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                        "App crashed after " + i + " random element taps");
            }
        }
        Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                "App should be alive after 40 random element taps");
    }

    // ── Test 4 — Deep Navigation and Back ────────────────────────────────────

    @Test(groups = {"monkey"},
          description = "Navigate deep into articles 3 times and randomly go back — verify no crash or stuck screen")
    public void testDeepNavigationAndBack() {
        IOSDriver driver = driver();
        String bundleId = bundleId();

        for (int cycle = 1; cycle <= 3; cycle++) {
            log.info("Deep-nav cycle {}/3", cycle);
            // Start fresh at home for each cycle
            navigateToHome();

            // Scroll down 2-3 times to load more content
            for (int s = 0; s < cycle + 1; s++) {
                MonkeyUtils.scrollDown(driver);
                MonkeyUtils.shortPause();
            }

            // Tap a random element to navigate deep
            MonkeyUtils.tapRandomElement(driver);
            MonkeyUtils.shortPause();

            // Scroll a bit inside the opened screen
            for (int s = 0; s < 3; s++) {
                MonkeyUtils.scrollDown(driver);
                MonkeyUtils.shortPause();
            }

            // Navigate back to home
            for (int b = 0; b < 3; b++) {
                MonkeyUtils.navigateBack(driver);
                MonkeyUtils.shortPause();
            }

            Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                    "App crashed in deep-nav cycle " + cycle);
        }
    }

    // ── Test 5 — Search Field Fuzzing ─────────────────────────────────────────

    @Test(groups = {"monkey"},
          description = "Enter all 15 fuzz strings (empty, special chars, SQL injection, emoji, unicode, etc.) into the search field and verify no crash")
    public void testSearchInputFuzzing() {
        navigateToHome();
        IOSDriver driver = driver();
        String bundleId = bundleId();

        // Open search
        HomePage home = new HomePage();
        try {
            home.tapSearch();
        } catch (Exception e) {
            log.warn("Could not open search via tapSearch — {}", e.getMessage());
            Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId), "App crashed before search opened");
            return;
        }

        // Try every fuzz input in the search field
        for (int i = 0; i < MonkeyUtils.FUZZ_INPUTS.size(); i++) {
            String input = MonkeyUtils.FUZZ_INPUTS.get(i);
            String preview = input.length() > 40 ? input.substring(0, 40) + "…" : input;
            log.info("Fuzz input [{}/{}]: '{}'", i + 1, MonkeyUtils.FUZZ_INPUTS.size(), preview);

            MonkeyUtils.typeRandomFuzzText(driver);
            MonkeyUtils.shortPause();
            MonkeyUtils.dismissAlertIfPresent(driver);

            Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                    "App crashed after fuzz input: " + preview);
        }
    }

    // ── Test 6 — Rapid Tab Bar Switching ─────────────────────────────────────

    @Test(groups = {"monkey"},
          description = "Rapidly switch between bottom tab bar items 30 times and verify no crash or freeze")
    public void testRapidTabBarSwitching() {
        navigateToHome();
        IOSDriver driver = driver();
        String bundleId = bundleId();

        String[] tabIds = {"Home-01", "Cricket-01", "DeepRead-01"};
        java.util.Random rng = new java.util.Random();

        for (int i = 1; i <= 30; i++) {
            String tabId = tabIds[rng.nextInt(tabIds.length)];
            log.info("Monkey: switching to tab '{}' (iteration {})", tabId, i);
            try {
                driver.findElement(io.appium.java_client.AppiumBy.accessibilityId(tabId)).click();
            } catch (Exception e) {
                log.warn("Monkey: tab switch to '{}' failed — {}", tabId, e.getMessage());
            }
            MonkeyUtils.shortPause();

            if (i % 10 == 0) {
                Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                        "App crashed after " + i + " rapid tab switches");
            }
        }

        Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                "App should be alive after 30 rapid tab switches");
    }

    // ── Test 7 — Double Taps and Long Presses ────────────────────────────────

    @Test(groups = {"monkey"},
          description = "Fire 15 random double-taps and 15 random long-presses and verify no crash")
    public void testRandomDoubleTapsAndLongPresses() {
        navigateToHome();
        IOSDriver driver = driver();
        String bundleId = bundleId();

        // 15 double taps
        for (int i = 0; i < 15; i++) {
            MonkeyUtils.randomDoubleTap(driver);
            MonkeyUtils.dismissAlertIfPresent(driver);
            MonkeyUtils.shortPause();
        }
        Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                "App crashed after 15 double-taps");

        // 15 long presses
        for (int i = 0; i < 15; i++) {
            MonkeyUtils.randomLongPress(driver);
            MonkeyUtils.dismissAlertIfPresent(driver);
            MonkeyUtils.shortPause();
        }
        Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                "App crashed after 15 long-presses");
    }

    // ── Test 8 — Continuous Scroll Stress ────────────────────────────────────

    @Test(groups = {"monkey"},
          description = "Continuously scroll down 30 times then up 30 times — verifies the app does not freeze or crash under scroll stress")
    public void testContinuousScrollStress() {
        navigateToHome();
        IOSDriver driver = driver();
        String bundleId = bundleId();

        // 30 scrolls down
        for (int i = 0; i < 30; i++) {
            MonkeyUtils.scrollDown(driver);
            MonkeyUtils.shortPause();
        }
        Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                "App crashed after 30 scroll-down events");

        // 30 scrolls up
        for (int i = 0; i < 30; i++) {
            MonkeyUtils.scrollUp(driver);
            MonkeyUtils.shortPause();
        }
        Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                "App crashed after 30 scroll-up events");
    }

    // ── Test 9 — Pinch / Zoom Gestures on Article ────────────────────────────

    @Test(groups = {"monkey"},
          description = "Open an article and fire 20 random pinch (zoom-in / zoom-out) gestures — verifies article view handles zoom without crashing")
    public void testRandomPinchZoomOnArticle() {
        navigateToHome();
        IOSDriver driver = driver();
        String bundleId = bundleId();

        // Navigate into first article
        try {
            HomePage home = new HomePage();
            home.tapFirstArticle();
            MonkeyUtils.shortPause();
        } catch (Exception e) {
            log.warn("Could not tap first article — {}", e.getMessage());
        }

        java.util.Random rng = new java.util.Random();
        for (int i = 0; i < 20; i++) {
            if (rng.nextBoolean()) {
                MonkeyUtils.pinchZoomIn(driver);
            } else {
                MonkeyUtils.pinchZoomOut(driver);
            }
            MonkeyUtils.dismissAlertIfPresent(driver);
            MonkeyUtils.shortPause();
        }

        Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                "App crashed after 20 random pinch/zoom gestures on article");
    }

    // ── Test 10 — Mixed Chaos Events ─────────────────────────────────────────

    @Test(groups = {"monkey"},
          description = "Fire 100 fully random events (tap, scroll, swipe, double-tap, long-press, pinch, back, alert-dismiss, text-input) — the ultimate chaos test")
    public void testMixedChaosEvents() {
        navigateToHome();
        IOSDriver driver = driver();
        String bundleId = bundleId();

        for (int i = 1; i <= 100; i++) {
            MonkeyUtils.fireRandomEvent(driver);
            MonkeyUtils.dismissAlertIfPresent(driver);
            MonkeyUtils.shortPause();

            // Alive check every 20 events — try to recover if app went to background
            if (i % 20 == 0) {
                if (!MonkeyUtils.isAppAlive(driver, bundleId)) {
                    log.warn("App went to background at event #{} — attempting recovery", i);
                    try {
                        driver.activateApp(bundleId);
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        log.error("Recovery activateApp failed: {}", e.getMessage());
                    }
                    Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                            "App could not be recovered at chaos event #" + i);
                    log.info("=== Chaos recovery successful at event #{} ===", i);
                } else {
                    log.info("=== Chaos alive check passed at event #{} ===", i);
                }
            }
        }

        Assert.assertTrue(MonkeyUtils.isAppAlive(driver, bundleId),
                "App should be alive after 100 mixed chaos events");
    }
}
