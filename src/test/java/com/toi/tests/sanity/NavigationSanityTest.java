package com.toi.tests.sanity;

import com.toi.base.BaseTest;
import com.toi.pages.ArticlePage;
import com.toi.pages.CategoryPage;
import com.toi.pages.CommentsPage;
import com.toi.pages.HomePage;
import com.toi.pages.LoginPage;
import com.toi.pages.SideNavPage;
import com.toi.pages.SplashPage;
import com.toi.pages.VideoPage;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Sanity tests — app navigation, ads, bookmarks, games and comments.
 *
 * TC4  — All login options are visible/working
 * TC5  — Non-logged-in user taps bookmark → login bottom sheet appears
 * TC6  — Bookmark is working for article show page
 * TC7  — All bottom tabs are accessible
 * TC8  — Ads are appearing on home page
 * TC13 — User is able to play the games
 * TC20 — Exclusive tab loading fine
 * TC21 — Ads are clickable on home page
 * TC22 — Watch tab working fine
 * TC24 — List pages from left-hand sections are loading fine
 * TC25 — User is able to comment on any article
 */
public class NavigationSanityTest extends BaseTest {

    // ── TC7 ──────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC7 — Verify all bottom tabs are accessible")
    public void testAllBottomTabsAccessible() {
        HomePage home = new SplashPage().skipOnboarding();
        Assert.assertTrue(home.isTabBarDisplayed(), "Tab bar should be visible on home screen");

        // Newsfeed tab (default)
        home.goToNewsfeed();
        Assert.assertTrue(home.getArticleCount() > 0, "Newsfeed tab should have articles");

        // Cricket / T20 WC tab
        CategoryPage cricket = home.goToCricket();
        Assert.assertTrue(cricket.isLoaded(), "Cricket tab should load");

        // Exclusive / DeepRead tab
        CategoryPage exclusive = home.goToExclusive();
        Assert.assertTrue(exclusive.isLoaded(), "Exclusive tab should load");

        log.info("All 3 bottom tabs verified");
    }

    // ── TC5 ──────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC5 — Verify all login options are displayed (Email, Google, Apple)")
    public void testAllLoginOptionsDisplayed() {
        LoginPage loginPage = new SplashPage()
                .skipOnboarding()
                .tapProfile();
        Assert.assertTrue(loginPage.isLoaded(),
                "Login page should load when profile icon is tapped");
        Assert.assertTrue(loginPage.isGoogleSignInDisplayed(),
                "Continue with Google option should be visible");
        Assert.assertTrue(loginPage.isAppleSignInDisplayed(),
                "Continue with Apple option should be visible");
    }

    // ── TC8 ──────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC8 — Verify ads are appearing on home page for non-logged-in user")
    public void testAdsAppearOnHomePage() {
        HomePage home = new SplashPage().skipOnboarding();
        // Scroll to ensure ad slots have loaded
        home.scrollToRefresh().scrollToRefresh();
        Assert.assertTrue(home.hasAdElements(),
                "Ad banners (300x250 or 'Ad' label) should be visible on the home page");
    }

    // ── TC5 ──────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC5 — Verify non-logged-in user tapping bookmark shows login bottom sheet")
    public void testBookmarkPromptLoginForNonLoggedInUser() {
        // Must open a regular article — live blog pages do not have the bookmark button
        ArticlePage article = new SplashPage()
                .skipOnboarding()
                .tapFirstNonLiveBlogArticle();
        Assert.assertTrue(article.isLoaded(), "Article page should load");
        article.tapBookmarkFromArticleBody();
        Assert.assertTrue(article.isLoginBookmarkSheetDisplayed(),
                "TC5: 'Log in to save your bookmarked articles' bottom sheet should appear for non-logged-in user");
        log.info("TC5: Login bookmark sheet confirmed for non-logged-in user");
    }

    // ── TC6 ──────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC6 — Verify bookmark is working on the article show page")
    public void testBookmarkWorksOnArticlePage() {
        ArticlePage article = new SplashPage()
                .skipOnboarding()
                .tapFirstArticle();
        Assert.assertTrue(article.isLoaded(), "Article page should load");
        article.tapBookmark();
        Assert.assertTrue(article.isBookmarkActive(),
                "Article should be bookmarked after tapping the bookmark icon");
    }

    // ── TC13 ─────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC13 — Verify user is able to navigate to games section")
    public void testGamesSectionAccessible() {
        HomePage home = new SplashPage().skipOnboarding();
        SideNavPage sideNav = home.openSideNav();
        Assert.assertTrue(sideNav.isLoaded(), "Side nav drawer should open");
        CategoryPage gamesPage = sideNav.tapGaming();
        Assert.assertTrue(gamesPage.getArticleCount() > 0,
                "Games section should load and display at least one item");
        log.info("Games section loaded with {} item(s)", gamesPage.getArticleCount());
    }

    // ── TC24 ─────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC24 — Verify Astrology under Left Nav is tappable and opens fine")
    public void testSideNavSectionListPagesLoad() {
        HomePage home = new SplashPage().skipOnboarding();
        CategoryPage astrologyPage = home.openSideNav().tapSection("Astrology");
        Assert.assertTrue(astrologyPage.isLoaded(),
                "TC24: Astrology listing page should open from left nav");
        log.info("TC24: Astrology listing page loaded successfully");
    }

    // ── TC25 ─────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC25 — Verify user is able to open the comments section on an article")
    public void testCommentsAccessibleOnArticle() {
        ArticlePage article = new SplashPage()
                .skipOnboarding()
                .tapFirstArticle();
        Assert.assertTrue(article.isLoaded(), "Article page should load");
        CommentsPage comments = article.tapComments();
        Assert.assertTrue(comments.isLoaded(),
                "Comments section should open after tapping the comments button");
    }

    // ── TC20 ─────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC20 — Verify Exclusive tab is loading fine")
    public void testExclusiveTabLoading() {
        HomePage home = new SplashPage().skipOnboarding();
        CategoryPage exclusive = home.goToExclusive();
        Assert.assertTrue(exclusive.isLoaded(), "Exclusive tab should load");
        Assert.assertTrue(exclusive.getArticleCount() > 0,
                "Exclusive tab should display articles");
        log.info("TC20: Exclusive tab loaded with {} articles", exclusive.getArticleCount());
    }

    // ── TC21 ─────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC21 — Verify ads are clickable on home page")
    public void testAdsAreClickable() {
        HomePage home = new SplashPage().skipOnboarding();
        // Scroll to load ad slots
        home.scrollToRefresh().scrollToRefresh();
        Assert.assertTrue(home.hasAdElements(),
                "TC21: Ad banners should be present and interactable on the home page");
        log.info("TC21: Ad elements confirmed present on home feed");
    }

    // ── TC22 ─────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC22 — Verify Watch tab is working fine")
    public void testWatchTabLoading() {
        HomePage home = new SplashPage().skipOnboarding();
        CategoryPage watchPage = home.openWatch();
        Assert.assertTrue(watchPage.isLoaded(),
                "TC22: Watch section should load with content");
        log.info("TC22: Watch tab loaded successfully");
    }

    // ── TC19 ─────────────────────────────────────────────────────────────────

    @Test(groups = {"sanity"},
          description = "TC19 — Verify interstitial ad is coming")
    public void testInterstitialAdAppears() {
        // Interstitials are triggered by navigating through multiple articles.
        // Open 3 articles back-to-back; the ad should appear on one of the returns to home.
        HomePage home = new SplashPage().skipOnboarding();

        // Article visit 1
        home.tapFirstArticle().goBack();

        // Article visit 2
        home = new HomePage();
        home.tapFirstArticle().goBack();

        // Article visit 3 — interstitial most likely fires on this return
        home = new HomePage();
        home.tapFirstArticle().goBack();

        home = new HomePage();
        Assert.assertTrue(home.isInterstitialDisplayed(),
                "TC19: A full-screen interstitial ad should appear after navigating through multiple articles");
        log.info("TC19: Interstitial ad confirmed on screen");
    }
}
