package com.toi.tests.sanity;

import com.toi.base.BaseTest;
import com.toi.pages.HomePage;
import com.toi.pages.SplashPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Sanity tests — verify the app launches and the home feed loads.
 */
public class AppLaunchSanityTest extends BaseTest {

    @Test(groups = {"sanity", "smoke"},
          description = "Verify the TOI app launches and splash screen is shown")
    public void testAppLaunchShowsSplash() {
        SplashPage splash = new SplashPage();
        Assert.assertTrue(splash.isLoaded(),
                "Splash/onboarding screen should be visible on first launch");
    }

    @Test(groups = {"sanity", "smoke"},
          description = "Verify skipping onboarding lands on the Home page",
          dependsOnMethods = "testAppLaunchShowsSplash")
    public void testSkipOnboardingLoadsHome() {
        SplashPage splash = new SplashPage();
        HomePage home = splash.skipOnboarding();
        Assert.assertTrue(home.isLoaded(),
                "Home page should load after skipping onboarding");
    }

    @Test(groups = {"sanity"},
          description = "Verify home feed has at least one article card")
    public void testHomeFeedHasArticles() {
        SplashPage splash = new SplashPage();
        HomePage home = splash.skipOnboarding();
        int count = home.getArticleCount();
        Assert.assertTrue(count > 0,
                "Home feed should display at least one article but found: " + count);
        log.info("Home feed loaded with {} article(s)", count);
    }
}
