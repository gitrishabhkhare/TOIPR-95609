package com.toi.tests.sanity;

import com.toi.base.BaseTest;
import com.toi.pages.EpaperPage;
import com.toi.pages.HomePage;
import com.toi.pages.SplashPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Sanity tests — ePaper tab and paywall/blocker.
 *
 * TC23 — ePaper tab is loading fine
 * TC24 — ePaper blocker is shown when non-logged-in user taps Subscribe to ePaper
 */
public class EpaperSanityTest extends BaseTest {

    // ── TC23 ─────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC23 — Verify ePaper tab is loading fine")
    public void testEpaperTabLoading() {
        HomePage home = new SplashPage().skipOnboarding();
        home.selectFilterTab("ePaper");
        EpaperPage epaper = new EpaperPage();
        Assert.assertTrue(epaper.isLoaded(),
                "TC23: ePaper tab should load and show Subscribe to ePaper button");
        log.info("TC23: ePaper tab loaded successfully");
    }

    // ── ePaper Blocker ────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC24 — Verify ePaper blocker is shown for non-logged-in user")
    public void testEpaperBlockerForNonLoggedInUser() {
        HomePage home = new SplashPage().skipOnboarding();
        home.selectFilterTab("ePaper");
        EpaperPage epaper = new EpaperPage();
        Assert.assertTrue(epaper.isLoaded(), "ePaper tab should load before tapping subscribe");
        epaper.tapSubscribeToEpaper();
        Assert.assertTrue(epaper.isBlockerDisplayed(),
                "TC24: ePaper paywall/blocker should be shown for non-logged-in user");
        log.info("TC24: ePaper blocker confirmed for non-logged-in user");
    }
}
