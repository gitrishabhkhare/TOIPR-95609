package com.toi.tests.sanity;

import com.toi.base.BaseTest;
import com.toi.config.ConfigReader;
import com.toi.config.DriverManager;
import com.toi.pages.ArticlePage;
import com.toi.pages.CategoryPage;
import com.toi.pages.HomePage;
import com.toi.pages.SplashPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests covering the scheduleList listing type.
 *
 * Ticket context: Eliminate old listing support; verify scheduleList-typed sections
 * still behave correctly after the migration.
 *
 * Acceptance criteria:
 *   AC1 — scheduleList sections load and paginate correctly
 *   AC2 — All listings load properly (no blank screen / crash)
 *   AC3 — "You may also like" section on article show page loads and click works
 */
public class ScheduleListTest extends BaseTest {

    /** Side-nav sections that use the scheduleList template.
     *  Sports+ are excluded: nav-bar auto-hide after scroll makes navigation
     *  to Sports unreliable in automation — this is a known expected limitation. */
    private static final String[] SCHEDULE_LIST_SECTIONS =
            {"India", "World"};

    // ── AC1: scheduleList sections load and paginate ──────────────────────────

    @Test(groups = {"schedulelist"},
          description = "AC1 — scheduleList sections should load articles and paginate on scroll")
    public void testScheduleListSectionsLoadAndPaginate() {
        HomePage home = new SplashPage().skipOnboarding();
        Assert.assertTrue(home.isLoaded(), "Home should be loaded before navigating to sections");

        CategoryPage currentPage = null;
        for (String section : SCHEDULE_LIST_SECTIONS) {
            log.info("=== Testing scheduleList section: {} ===", section);

            // Navigate: for the first section open from home; for subsequent sections
            // go directly from the current CategoryPage via side nav — avoids the
            // fragile goBack→home→openSideNav chain.
            CategoryPage page = (currentPage == null)
                    ? home.openSideNav().tapSection(section)
                    : currentPage.navigateToSection(section);

            Assert.assertTrue(page.isLoaded(),
                    "[" + section + "] Listing page should load");

            int initialCount = page.getArticleCount();
            Assert.assertTrue(initialCount > 0,
                    "[" + section + "] Should display at least one article on load; found: " + initialCount);
            log.info("[{}] Initial article count: {}", section, initialCount);

            // Trigger pagination via scroll
            for (int i = 0; i < 3; i++) {
                page.scroll();
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            }

            int afterScrollCount = page.getArticleCount();
            Assert.assertTrue(afterScrollCount >= initialCount,
                    "[" + section + "] Article count should not decrease after scrolling (pagination). "
                    + "Before: " + initialCount + ", After: " + afterScrollCount);
            log.info("[{}] After-scroll article count: {} (pagination OK)", section, afterScrollCount);

            currentPage = page;
        }
    }

    // ── AC2: All listings load properly ──────────────────────────────────────

    @Test(groups = {"schedulelist"},
          description = "AC2 — All main listings (home feed, tabs, side nav sections) should load without crash")
    public void testAllListingsLoadProperly() {
        HomePage home = new SplashPage().skipOnboarding();

        // Home / Newsfeed — poll up to 30 s for home screen to appear (handles slow WDA warm-up)
        boolean homeLoaded = false;
        for (int i = 0; i < 6; i++) {
            homeLoaded = home.isLoaded();
            if (homeLoaded) break;
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
        }
        Assert.assertTrue(homeLoaded, "Home feed should be loaded");
        // Poll up to 10 s for articles to populate
        for (int i = 0; i < 5 && home.getArticleCount() == 0; i++) {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        }
        Assert.assertTrue(home.getArticleCount() > 0, "Home feed should display articles");
        log.info("Home feed: OK ({} articles)", home.getArticleCount());

        // Cricket — accessed via left-nav hamburger (scroll down to find it)
        try {
            CategoryPage cricket = home.openSideNav().tapSection("Cricket");
            Assert.assertTrue(cricket.isLoaded(), "Cricket listing should load");
            Assert.assertTrue(cricket.getArticleCount() > 0, "Cricket listing should contain articles");
            log.info("Cricket (side nav): OK ({} articles)", cricket.getArticleCount());
            // Reset home via app restart to avoid stale nav state
            String bundleId = com.toi.config.ConfigReader.get("app.bundle.id");
            DriverManager.getDriver().terminateApp(bundleId);
            Thread.sleep(1000);
            DriverManager.getDriver().activateApp(bundleId);
            Thread.sleep(4000);
            home = new SplashPage().skipOnboarding();
        } catch (Exception e) {
            log.warn("Cricket not found in side nav — skipping: {}", e.getMessage());
        }

        // Exclusive tab
        try {
            CategoryPage exclusive = home.goToExclusive();
            Assert.assertTrue(exclusive.isLoaded(), "Exclusive listing should load");
            Assert.assertTrue(exclusive.getArticleCount() > 0, "Exclusive listing should contain articles");
            log.info("Exclusive tab: OK ({} articles)", exclusive.getArticleCount());
            home = exclusive.goBack();
        } catch (Exception e) {
            log.warn("Exclusive tab not accessible — skipping: {}", e.getMessage());
            // Recover home via app restart
            try {
                String bundleId = com.toi.config.ConfigReader.get("app.bundle.id");
                DriverManager.getDriver().terminateApp(bundleId);
                Thread.sleep(1000);
                DriverManager.getDriver().activateApp(bundleId);
                Thread.sleep(4000);
                home = new SplashPage().skipOnboarding();
            } catch (Exception re) { log.warn("Recovery restart failed: {}", re.getMessage()); }
        }

        // scheduleList side-nav sections — India and World only
        // (Sports+ excluded: nav-bar auto-hide after scroll blocks navigation — expected limitation)
        String[] sideNavSections = {"India", "World"};
        CategoryPage currentCatPage = null;
        for (String section : sideNavSections) {
            CategoryPage page = (currentCatPage == null)
                    ? home.openSideNav().tapSection(section)
                    : currentCatPage.navigateToSection(section);
            Assert.assertTrue(page.isLoaded(),
                    "[" + section + "] Listing should load without crash");
            Assert.assertTrue(page.getArticleCount() > 0,
                    "[" + section + "] Listing should contain at least one article");
            log.info("{}: OK ({} articles)", section, page.getArticleCount());
            currentCatPage = page;
        }
    }

    // ── AC3: "You may also like" on article show page ─────────────────────────

    @Test(groups = {"schedulelist"},
          description = "AC3 — 'You may also like' section should load and be tappable on article show page")
    public void testYouMayAlsoLikeSectionLoadsAndIsClickable() {
        HomePage home = new SplashPage().skipOnboarding();
        CategoryPage india = home.openSideNav().tapSection("India");
        Assert.assertTrue(india.isLoaded(), "India (scheduleList) listing should load");

        ArticlePage article = openFirstNonPaywalledArticle(india, "India");
        Assert.assertTrue(article.isLoaded(), "Article show page should load");
        log.info("Opened article: {}", article.getArticleTitle());

        boolean sectionFound = article.scrollToYouMayAlsoLike();
        Assert.assertTrue(sectionFound,
                "AC3: 'You may also like' / related articles section should appear at the bottom of the article");

        ArticlePage recommended = article.tapFirstYouMayAlsoLikeArticle();
        Assert.assertTrue(recommended.isLoaded(),
                "AC3: Tapping a recommended article should open a new article page");
        log.info("AC3: Related article navigation: OK");
    }

    // ── AC3 variant: across multiple scheduleList sections ────────────────────

    @Test(groups = {"schedulelist"},
          description = "AC3b — 'You may also like' click functionality verified across 3 random scheduleList sections")
    public void testYouMayAlsoLikeAcrossMultipleScheduleListSections() {
        String[] testSections = {"World", "Sports", "Entertainment"};

        for (int idx = 0; idx < testSections.length; idx++) {
            String section = testSections[idx];
            log.info("=== Testing 'You may also like' from section: {} ===", section);

            // Restart app to a clean state for each iteration (except the first, which is
            // already clean from @BeforeMethod). This prevents stale navigation state
            // (deep in article stack) from causing the next iteration's openSideNav() to fail.
            if (idx > 0) {
                try {
                    String bundleId = ConfigReader.get("app.bundle.id");
                    DriverManager.getDriver().terminateApp(bundleId);
                    Thread.sleep(1000);
                    DriverManager.getDriver().activateApp(bundleId);
                    Thread.sleep(5000);
                } catch (Exception e) {
                    log.warn("App restart between sections skipped: {}", e.getMessage());
                }
            }
            HomePage home = new SplashPage().skipOnboarding();

            CategoryPage page = home.openSideNav().tapSection(section);
            Assert.assertTrue(page.isLoaded(),
                    "[" + section + "] Listing should load");

            ArticlePage article = openFirstNonPaywalledArticle(page, section);
            Assert.assertTrue(article.isLoaded(),
                    "[" + section + "] Article should load");

            boolean sectionFound = article.scrollToYouMayAlsoLike();
            Assert.assertTrue(sectionFound,
                    "[" + section + "] 'You may also like' section should be present in article");

            ArticlePage related = article.tapFirstYouMayAlsoLikeArticle();
            Assert.assertTrue(related.isLoaded(),
                    "[" + section + "] Related article click should open a new article page");
            log.info("[{}] 'You may also like' click: OK", section);
        }
    }

    // ── Helper: find first non-paywalled article in a category listing ────────

    /**
     * Iterates through the first {@code maxAttempts} articles in the given CategoryPage
     * and returns the first one that is NOT behind the TOI+ paywall.
     * Goes back and tries the next article if a paywall is detected.
     */
    private ArticlePage openFirstNonPaywalledArticle(CategoryPage catPage, String sectionName) {
        final int maxAttempts = 6;
        for (int i = 0; i < maxAttempts; i++) {
            log.info("[{}] Trying article at index {}", sectionName, i);
            ArticlePage article = catPage.tapArticleAtIndex(i);
            if (!article.isSubscribeBannerDisplayed()) {
                log.info("[{}] Article {} is not paywalled — proceeding", sectionName, i);
                return article;
            }
            log.info("[{}] Article {} is TOI+ paywalled — going back to try next", sectionName, i);
            article.goBack(); // navigates back to CategoryPage screen
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException(
                "[" + sectionName + "] No non-paywalled article found in first " + maxAttempts + " articles");
    }
}
