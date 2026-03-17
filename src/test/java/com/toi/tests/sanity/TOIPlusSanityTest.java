package com.toi.tests.sanity;

import com.toi.base.BaseTest;
import com.toi.pages.ArticlePage;
import com.toi.pages.CategoryPage;
import com.toi.pages.HomePage;
import com.toi.pages.LoginPage;
import com.toi.pages.SplashPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Sanity tests — TOI+ subscription gating and user experience.
 *
 * TC9  — TOI+ blocker appears on accessing TOI+ article without login
 * TC10 — User is able to login with TOI+ account
 * TC11 — Blocked content is accessible using TOI+ user
 * TC12 — Ads are not displayed for TOI+ user
 */
public class TOIPlusSanityTest extends BaseTest {

    private static final String TOI_PLUS_EMAIL    = "toitestforapple2026@gmail.com";
    private static final String TOI_PLUS_PASSWORD = "Ti@Tes233112#";

    // ── TC9 ──────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC9 — Verify TOI+ blocker appears when accessing TOI+ article without login")
    public void testTOIPlusBlockerWithoutLogin() {
        // TOI+ articles appear on the home For You / Top feed (not Exclusive tab).
        // Verified on live device 2026-03-11: paywall shows "Want to read the full story?"
        HomePage home = new SplashPage().skipOnboarding();
        ArticlePage article = home.tapFirstTOIPlusArticle();
        Assert.assertTrue(article.isLoaded(), "TOI+ article page should load");
        Assert.assertTrue(article.isSubscribeBannerDisplayed(),
                "TOI+ paywall ('Want to read the full story?') should appear for non-logged-in user");
    }

    // ── TC10 ─────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC10 — Verify user is able to login with TOI+ account")
    public void testTOIPlusLogin() {
        HomePage home = new SplashPage()
                .skipOnboarding()
                .tapProfile()
                .loginWithEmail(TOI_PLUS_EMAIL, TOI_PLUS_PASSWORD);
        Assert.assertTrue(home.isLoaded(),
                "Home page should load after TOI+ account login");
    }

    // ── TC11 ─────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC11 — Verify blocked content is accessible using TOI+ user",
          dependsOnMethods = "testTOIPlusLogin")
    public void testBlockedContentAccessibleForTOIPlusUser() {
        // Login with TOI+ account
        HomePage home = new SplashPage()
                .skipOnboarding()
                .tapProfile()
                .loginWithEmail(TOI_PLUS_EMAIL, TOI_PLUS_PASSWORD);

        // Navigate to Exclusive/TOI+ content
        CategoryPage exclusivePage = home.goToExclusive();
        Assert.assertTrue(exclusivePage.getArticleCount() > 0,
                "Exclusive tab should show articles for TOI+ user");

        ArticlePage article = exclusivePage.tapFirstArticle();
        Assert.assertTrue(article.isLoaded(), "Article should load for TOI+ user");
        Assert.assertFalse(article.isSubscribeBannerDisplayed(),
                "Subscribe banner should NOT appear for logged-in TOI+ user");
    }

    // ── TC12 ─────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC12 — Verify ads are not displayed for TOI+ user")
    public void testNoAdsForTOIPlusUser() {
        // Login with TOI+ account
        HomePage home = new SplashPage()
                .skipOnboarding()
                .tapProfile()
                .loginWithEmail(TOI_PLUS_EMAIL, TOI_PLUS_PASSWORD);

        Assert.assertTrue(home.isLoaded(), "Home should load after TOI+ login");
        // Scroll a couple of times to give ads a chance to appear if present
        home.scrollToRefresh().scrollToRefresh();
        Assert.assertFalse(home.hasAdElements(),
                "Ad banners (300x250 / 'Ad' label) should NOT be displayed for TOI+ user");
    }
}
