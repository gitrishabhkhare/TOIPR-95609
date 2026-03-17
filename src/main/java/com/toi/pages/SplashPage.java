package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;

/**
 * SplashPage — first screen shown on app launch.
 */
public class SplashPage extends BasePage {

    // Verified: TOI logo accessibility id from inspector
    private final By splashLogo       = byAccessibilityId("TOIlogoDark");
    private final By skipButton       = byAccessibilityId("Skip");
    private final By getStartedButton = byAccessibilityId("Get Started");

    // ── Actions ───────────────────────────────────────────────────────────────

    public HomePage skipOnboarding() {
        log.info("Skipping onboarding/splash screen");
        if (isDisplayed(skipButton)) {
            tap(skipButton);
        } else if (isDisplayed(getStartedButton)) {
            tap(getStartedButton);
        }
        return new HomePage();
    }

    // ── Verification ──────────────────────────────────────────────────────────

    @Override
    public boolean isLoaded() {
        return isDisplayed(splashLogo)
                || isDisplayed(skipButton)
                || isDisplayed(getStartedButton);
    }
}
