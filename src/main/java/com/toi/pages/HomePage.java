package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;

/**
 * HomePage — main news feed screen of the TOI app.
 */
public class HomePage extends BasePage {

    // ── Navigation ────────────────────────────────────────────────────────────
    // Verified via Appium inspector (iOS 26.3, com.2ergoTOI.jayant)
    private final By sideNavButton       = byPredicateString(
            "type == 'XCUIElementTypeButton' AND (name == 'sideNavIconDark' OR name == 'sideNavIconLight')");
    private final By profileButton       = byAccessibilityId("homePageProfileIcon");
    private final By toiLogo             = byAccessibilityId("TOIlogoDark");

    // ── Tab Bar (3 tabs) ──────────────────────────────────────────────────────
    private final By tabBar              = byAccessibilityId("Tab Bar");
    private final By newsfeedTab         = byAccessibilityId("Home-01");      // label: Newsfeed
    private final By cricketTab          = byAccessibilityId("Cricket-01");   // label: T20 WC
    private final By exclusiveTab        = byAccessibilityId("DeepRead-01");  // label: Exclusive

    // ── Content Filter Strip (Top / For You / ePaper) ─────────────────────────
    private final By tabForYou           = byPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'For You'");
    private final By tabTop              = byPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'Top'");
    private final By tabEpaper           = byPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'ePaper'");

    // ── Quick Access Bar (TOI+ / Games / Videos / Watch) ──────────────────────
    private final By quickAccessTOIPlus  = byAccessibilityId("TOI+");
    private final By quickAccessGames    = byAccessibilityId("Games");
    private final By quickAccessVideos   = byAccessibilityId("Videos");
    private final By quickAccessWatch    = byAccessibilityId("Watch");

    // ── Trending Topics ───────────────────────────────────────────────────────
    private final By trendingTopicsLabel = byPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'TRENDING TOPICS'");

    // ── Article Feed ──────────────────────────────────────────────────────────
    private final By articleCells        = byPredicateString("type == 'XCUIElementTypeCell'");

    // ── Actions ───────────────────────────────────────────────────────────────

    public void tapSideNav() {
        log.info("Tapping side navigation");
        tap(sideNavButton);
    }

    /** Opens the side nav drawer and returns its page object. */
    public SideNavPage openSideNav() {
        log.info("Opening side navigation drawer");
        tap(sideNavButton);
        return new SideNavPage();
    }

    /** Returns true if ad elements (300x250 banner or 'Ad' label) are visible on the page. */
    public boolean hasAdElements() {
        By adBanner = byPredicateString("type == 'XCUIElementTypeOther' AND name == '300x250'");
        By adLabel  = byPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'Ad'");
        return isDisplayed(adBanner) || isDisplayed(adLabel);
    }

    /** Opens the side nav drawer then taps the Search item inside it. */
    public SearchPage tapSearch() {
        log.info("Opening search via side nav");
        tap(sideNavButton);
        return new SideNavPage().tapSearch();
    }

    /** Alias for tapCategoryButton — kept for test compatibility. */
    public CategoryPage selectCategory(String categoryName) {
        return tapCategoryButton(categoryName);
    }

    /** Alias for openVideos — kept for test compatibility. */
    public VideoPage goToVideos() {
        return openVideos();
    }

    /** Navigates to the personalised "For You" feed. */
    public MyFeedPage goToMyFeed() {
        log.info("Navigating to My Feed (For You)");
        tap(tabForYou);
        return new MyFeedPage();
    }

    public LoginPage tapProfile() {
        log.info("Opening login via side nav profile");
        // Try known accessibility IDs (Dark/Light variants); fallback to top-left coordinate tap
        List<WebElement> navBtns = driver.findElements(sideNavButton);
        if (!navBtns.isEmpty()) {
            navBtns.get(0).click();
        } else {
            // Hamburger "≡ TOI" sits in the top-left corner of the nav bar
            Dimension size = driver.manage().window().getSize();
            int x = (int) (size.width * 0.07);   // ~7% from left edge
            int y = (int) (size.height * 0.065); // ~6.5% from top (nav bar row)
            log.info("sideNavButton not found by ID — coord tap at ({}, {})", x, y);
            driver.executeScript("mobile: tap", Map.of("x", x, "y", y));
        }
        // For a non-logged-in user the side nav shows a "Login unlocks the good stuff" banner
        By loginBanner = byPredicateString(
                "type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'login unlocks the good stuff'");
        if (isDisplayed(loginBanner)) {
            tap(loginBanner);
        } else {
            tap(byAccessibilityId("Profile"));
        }
        return new LoginPage();
    }

    public ArticlePage tapFirstArticle() {
        log.info("Tapping first article card");
        List<WebElement> cells = findElements(articleCells);
        if (cells.isEmpty()) {
            throw new RuntimeException("No article cells found on HomePage");
        }
        cells.get(0).click();
        return new ArticlePage();
    }

    /**
     * Taps the first regular (non-live-blog) article on the home feed.
     * Live blog cells contain a child StaticText with name="Live" (red badge).
     * Verified on live device 2026-03-11: bookmark button (bookmarkBlack new) is
     * visible on regular articles but absent on live blog pages.
     * Strategy: scroll until no "Live" badge is visible, then tap first cell.
     */
    public ArticlePage tapFirstNonLiveBlogArticle() {
        log.info("Tapping first non-live-blog article");
        By liveBadge = byPredicateString(
                "type == 'XCUIElementTypeStaticText' AND name == 'Live' AND visible == true");
        // Scroll down until Live badge is off screen (max 3 scrolls)
        for (int i = 0; i < 3 && isDisplayed(liveBadge); i++) {
            scrollDown();
        }
        List<WebElement> cells = findElements(articleCells);
        if (cells.isEmpty()) throw new RuntimeException("No article cells found after scrolling past live blog");
        cells.get(0).click();
        return new ArticlePage();
    }

    public ArticlePage tapArticleAtIndex(int index) {
        log.info("Tapping article at index {}", index);
        List<WebElement> cells = findElements(articleCells);
        if (index >= cells.size()) {
            throw new RuntimeException("Index " + index + " out of bounds. Found: " + cells.size());
        }
        cells.get(index).click();
        return new ArticlePage();
    }

    public HomePage scrollToRefresh() {
        log.info("Pull-to-refresh on home feed");
        scrollDown();
        return this;
    }

    public int getArticleCount() {
        return findElements(articleCells).size();
    }

    /** Select a content filter tab by label: "Top", "For You", "ePaper" */
    public HomePage selectFilterTab(String label) {
        log.info("Selecting filter tab: {}", label);
        tap(byPredicateString("type == 'XCUIElementTypeStaticText' AND label == '" + label + "'"));
        return this;
    }

    /** Tap a trending topic button: Opinion, Explainer, Interview, Data Dive, Analysis, City Dialogues */
    public HomePage tapTopic(String topicName) {
        log.info("Tapping topic: {}", topicName);
        tap(byAccessibilityId(topicName));
        return this;
    }

    /** Tap a category pill button (e.g. "Middle East", "International") */
    public CategoryPage tapCategoryButton(String category) {
        log.info("Tapping category: {}", category);
        tap(byPredicateString("type == 'XCUIElementTypeButton' AND label == '" + category + "'"));
        return new CategoryPage(category);
    }

    // ── Tab Bar Navigation ────────────────────────────────────────────────────

    public HomePage goToNewsfeed() {
        tap(newsfeedTab);
        return this;
    }

    public CategoryPage goToCricket() {
        tap(cricketTab);
        return new CategoryPage("T20 WC");
    }

    /**
     * Scrolls the home feed to find the first TOI+ gated article (red TOI+ badge)
     * and taps it. TOI+ articles have a child StaticText with name="TOI+".
     * Verified on live device 2026-03-11: TOI+ articles appear on the For You / Top feed,
     * NOT on the Exclusive tab.
     */
    public ArticlePage tapFirstTOIPlusArticle() {
        log.info("Looking for first TOI+ article on home feed");
        By toiPlusBadge = byPredicateString(
                "type == 'XCUIElementTypeStaticText' AND name == 'TOI+' AND visible == true");
        for (int i = 0; i < 8 && !isDisplayed(toiPlusBadge); i++) {
            scrollDown();
        }
        try {
            WebElement toiEl = driver.findElement(toiPlusBadge);
            int cellY = toiEl.getLocation().getY();
            int cellX = driver.manage().window().getSize().getWidth() / 2;
            driver.executeScript("mobile: tap", java.util.Map.of("x", cellX, "y", cellY));
        } catch (Exception e) {
            throw new RuntimeException("No TOI+ article found on home feed after scrolling", e);
        }
        return new ArticlePage();
    }

    /**
     * Scrolls the home feed to find the first article with a "Live" badge (red blinker)
     * and taps it. Live blog articles have a child StaticText with name="Live".
     * Verified on live device 2026-03-11.
     */
    public ArticlePage tapFirstLiveBlogArticle() {
        log.info("Looking for first Live blog article on home feed");
        By liveBadge = byPredicateString(
                "type == 'XCUIElementTypeStaticText' AND name == 'Live' AND visible == true");
        for (int i = 0; i < 5 && !isDisplayed(liveBadge); i++) {
            scrollDown();
        }
        // Tap the parent cell by coordinate — get Y of the Live badge and tap its cell
        try {
            WebElement liveEl = driver.findElement(liveBadge);
            int cellY = liveEl.getLocation().getY();
            int cellX = driver.manage().window().getSize().getWidth() / 2;
            driver.executeScript("mobile: tap", java.util.Map.of("x", cellX, "y", cellY));
        } catch (Exception e) {
            throw new RuntimeException("No Live blog article found on home feed after scrolling", e);
        }
        return new ArticlePage();
    }

    public CategoryPage goToExclusive() {
        tap(exclusiveTab);
        return new CategoryPage("Exclusive");
    }

    // ── Quick Access ──────────────────────────────────────────────────────────

    public CategoryPage openTOIPlus() {
        tap(quickAccessTOIPlus);
        return new CategoryPage("TOI+");
    }

    public VideoPage openVideos() {
        tap(quickAccessVideos);
        return new VideoPage();
    }

    public CategoryPage openWatch() {
        tap(quickAccessWatch);
        return new CategoryPage("Watch");
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public boolean isTrendingTopicsVisible() {
        return isDisplayed(trendingTopicsLabel);
    }

    public boolean isTabBarDisplayed() {
        return isDisplayed(tabBar);
    }

    // ── Interstitial Ad ───────────────────────────────────────────────────────

    /**
     * Returns true if a full-screen interstitial ad overlay is currently visible.
     * Interstitials from Google Ad Manager surface as a full-screen modal with a
     * close/skip button — we check for the most common close button labels.
     */
    public boolean isInterstitialDisplayed() {
        By closeBtn = byPredicateString(
                "type == 'XCUIElementTypeButton' AND (label == 'Close' OR label == 'X' OR label == 'Skip Ad' OR label == 'Skip')");
        By adOverlay = byPredicateString(
                "type == 'XCUIElementTypeOther' AND name CONTAINS[cd] 'interstitial'");
        return isDisplayed(closeBtn) || isDisplayed(adOverlay);
    }

    /** Dismisses an interstitial overlay if one is present, then returns self. */
    public HomePage dismissInterstitialIfPresent() {
        By closeBtn = byPredicateString(
                "type == 'XCUIElementTypeButton' AND (label == 'Close' OR label == 'X' OR label == 'Skip Ad' OR label == 'Skip')");
        if (isDisplayed(closeBtn)) {
            log.info("Dismissing interstitial ad");
            tap(closeBtn);
        }
        return this;
    }

    // ── Verification ──────────────────────────────────────────────────────────

    @Override
    public boolean isLoaded() {
        return isDisplayed(toiLogo) || isDisplayed(tabBar) || getArticleCount() > 0;
    }
}
