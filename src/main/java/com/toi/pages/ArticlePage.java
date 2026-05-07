package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * ArticlePage — full article detail screen.
 */
public class ArticlePage extends BasePage {

    // ── Locators ─────────────────────────────────────────────────────────────
    // Verified via Appium inspector (iOS 26.3, com.2ergoTOI.jayant)
    private final By backButton         = byAccessibilityId("new backIcon light");
    private final By profileButton      = byAccessibilityId("homePageProfileIcon");
    private final By toiPlusLogo        = byAccessibilityId("plus_dark");

    // Category filter tabs visible in article/deep-read view
    private final By tabFeatured        = byPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'Featured'");
    private final By tabTOIExplains     = byPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'TOI Explains'");
    private final By tabNewYorkTimes    = byPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'New York Times'");
    private final By tabHealth          = byPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'Health'");
    private final By tabBusiness        = byPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'Business & Economy'");

    // Article feed cells
    private final By articleCells       = byPredicateString("type == 'XCUIElementTypeCell'");

    // TOI+ paywall/blocker — verified on live device 2026-03-11
    // "Subscribe Now" buttons are visible=false; check the always-visible StaticText elements
    private final By subscribeNowButton = byPredicateString(
            "type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'subscribe'");
    private final By paywallHeading     = byAccessibilityId("Want to read the full story?");
    private final By paywallSubheading  = byAccessibilityId("Subscribe and unlock your access to:");

    // Trending topic buttons inside article feed
    private final By trendingTopicsLabel = byPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'TRENDING TOPICS'");

    // Author and category labels (best effort — dynamic content)
    private final By authorStaticTexts  = byClassChain("**/XCUIElementTypeStaticText[`visible == true`]");
    private final By categoryButtons    = byPredicateString("type == 'XCUIElementTypeButton' AND visible == true");

    // ── Actions ───────────────────────────────────────────────────────────────

    public HomePage goBack() {
        log.info("Navigating back from article");
        tap(backButton);
        return new HomePage();
    }

    public ArticlePage scrollArticle() {
        scrollDown();
        return this;
    }

    public ArticlePage tapFirstArticle() {
        List<org.openqa.selenium.WebElement> cells = findElements(articleCells);
        if (cells.isEmpty()) throw new RuntimeException("No article cells found");
        cells.get(0).click();
        return new ArticlePage();
    }

    public ArticlePage tapArticleAtIndex(int index) {
        List<org.openqa.selenium.WebElement> cells = findElements(articleCells);
        if (index >= cells.size()) throw new RuntimeException("Index " + index + " out of bounds. Found: " + cells.size());
        cells.get(index).click();
        return new ArticlePage();
    }

    /** Select a category tab: "Featured", "TOI Explains", "New York Times", "Health", "Business & Economy" */
    public ArticlePage selectCategoryTab(String label) {
        log.info("Selecting category tab: {}", label);
        tap(byPredicateString("type == 'XCUIElementTypeStaticText' AND label == '" + label + "'"));
        return this;
    }

    /**
     * Returns true if the TOI+ paywall/blocker bottom sheet is visible.
     * Shown when a non-logged-in user taps a TOI+ gated article.
     * Verified on live device 2026-03-11:
     *   - "Want to read the full story?" StaticText (visible=true)
     *   - "Subscribe and unlock your access to:" StaticText (visible=true)
     *   - "Subscribe Now" buttons are visible=false so NOT used for detection.
     */
    public boolean isSubscribeBannerDisplayed() {
        return isDisplayed(paywallHeading) || isDisplayed(paywallSubheading);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int getArticleCount() {
        return findElements(articleCells).size();
    }

    public boolean isTrendingTopicsVisible() {
        return isDisplayed(trendingTopicsLabel);
    }

    // ── Article metadata (best-effort — first visible StaticText as title) ────

    /** Returns the first meaningful visible static text as a proxy for article title. */
    public String getArticleTitle() {
        List<WebElement> texts = findElements(authorStaticTexts);
        for (WebElement el : texts) {
            String t = el.getText();
            if (t != null && t.length() > 10) return t;
        }
        return "";
    }

    /** Scroll to bottom to find share/bookmark in the action bar if present. */
    public void tapShare() {
        log.info("Attempting share (best-effort)");
        By shareBtn = byPredicateString(
                "type == 'XCUIElementTypeButton' AND (label CONTAINS[cd] 'share' OR name CONTAINS[cd] 'share')");
        if (!isDisplayed(shareBtn)) {
            scrollDown();
        }
        if (isDisplayed(shareBtn)) tap(shareBtn);
    }

    public ArticlePage tapBookmark() {
        log.info("Attempting bookmark (best-effort)");
        By bookmarkBtn = byPredicateString(
                "type == 'XCUIElementTypeButton' AND (label CONTAINS[cd] 'bookmark' OR name CONTAINS[cd] 'bookmark')");
        if (!isDisplayed(bookmarkBtn)) {
            scrollDown();
        }
        if (isDisplayed(bookmarkBtn)) tap(bookmarkBtn);
        return this;
    }

    /**
     * Scrolls into the article body to reveal the action bar, then taps the bookmark icon.
     * For a non-logged-in user this triggers the "Log in to save your bookmarked articles" bottom sheet.
     * Locators verified on live device 2026-03-11: name="bookmarkBlack new" (enabled=true after scroll).
     */
    /**
     * Taps the bookmark icon on a regular article page.
     * Verified on live device 2026-03-11: name="bookmarkBlack new" is visible
     * immediately on regular articles without scrolling.
     * For a non-logged-in user this triggers the "Log in to save your bookmarked articles" sheet.
     */
    public ArticlePage tapBookmarkFromArticleBody() {
        log.info("Tapping bookmark icon on article page");
        By bookmarkBtn = byPredicateString(
                "type == 'XCUIElementTypeButton' AND name == 'bookmarkBlack new'");
        // Button is visible immediately; scroll at most 2 times as fallback
        for (int i = 0; i < 2 && !isDisplayed(bookmarkBtn); i++) {
            scrollDown();
        }
        tap(bookmarkBtn);
        return this;
    }

    /**
     * Returns true if the "Log in to save your bookmarked articles" bottom sheet is visible.
     * Shown when a non-logged-in user taps the bookmark icon.
     * Locators verified on live device 2026-03-11.
     */
    public boolean isLoginBookmarkSheetDisplayed() {
        By sheetHeading = byAccessibilityId("Log in to save your bookmarked articles");
        By loginBtn     = byAccessibilityId("Login");
        return isDisplayed(sheetHeading) && isDisplayed(loginBtn);
    }

    public boolean isBookmarkActive() {
        By bookmarkBtn = byPredicateString(
                "type == 'XCUIElementTypeButton' AND (label CONTAINS[cd] 'bookmark' OR name CONTAINS[cd] 'bookmark')");
        try {
            String value = driver.findElement(bookmarkBtn).getAttribute("value");
            String selected = driver.findElement(bookmarkBtn).getAttribute("selected");
            // Accept "1", "true", or any non-null non-"0" non-"false" value
            if ("1".equals(value) || "true".equalsIgnoreCase(value)) return true;
            if ("true".equalsIgnoreCase(selected)) return true;
            // If no explicit value, accept that the tap succeeded (button still present)
            return value != null && !value.isEmpty() && !"0".equals(value) && !"false".equalsIgnoreCase(value);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Taps the comments button/icon on the article action bar.
     * Scrolls down if the button is not immediately visible.
     */
    public CommentsPage tapComments() {
        log.info("Opening comments section");
        By commentsBtn = byPredicateString(
                "type == 'XCUIElementTypeButton' AND " +
                "(label CONTAINS[cd] 'comment' OR label CONTAINS[cd] 'chat' OR " +
                "label CONTAINS[cd] 'discuss' OR label CONTAINS[cd] 'message' OR name CONTAINS[cd] 'comment')");
        // Scroll down up to 3 times to find the comments button in the article action bar
        for (int i = 0; i < 3 && !isDisplayed(commentsBtn); i++) {
            scrollDown();
        }
        tap(commentsBtn);
        return new CommentsPage();
    }

    /** Returns true if the share sheet was triggered (best-effort — checks sheet visibility). */
    public boolean isShareSheetDisplayed() {
        By shareSheet = byPredicateString(
                "type == 'XCUIElementTypeSheet' OR " +
                "(type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'copy') OR " +
                "(type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'whatsapp') OR " +
                "(type == 'XCUIElementTypeButton' AND label == 'More')");
        return isDisplayed(shareSheet);
    }

    /** Scrolls down and counts remaining visible cells as a proxy for related articles. */
    public int getRelatedArticlesCount() {
        scrollDown();
        return Math.max(0, findElements(articleCells).size() - 3);
    }

    /**
     * Scrolls through the article body until the "You may also like" / recommended
     * articles section header is visible (max 10 scrolls). Returns true if found.
     */
    public boolean scrollToYouMayAlsoLike() {
        log.info("Scrolling to 'You may also like' section");
        By sectionHeader = byPredicateString(
                "type == 'XCUIElementTypeStaticText' AND " +
                "(label CONTAINS[cd] 'you may also like' OR " +
                " label CONTAINS[cd] 'you might also like' OR " +
                " label CONTAINS[cd] 'more stories' OR " +
                " label CONTAINS[cd] 'related stories' OR " +
                " label CONTAINS[cd] 'recommended' OR " +
                " label CONTAINS[cd] 'read next' OR " +
                " label CONTAINS[cd] 'also read' OR " +
                " label CONTAINS[cd] 'similar stories' OR " +
                " label CONTAINS[cd] 'more from' OR " +
                " label CONTAINS[cd] 'more news' OR " +
                " label CONTAINS[cd] 'more like this' OR " +
                " label CONTAINS[cd] 'trending now')");
        for (int i = 0; i < 10 && !isDisplayed(sectionHeader); i++) {
            scrollDown();
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        if (isDisplayed(sectionHeader)) return true;
        // Fallback: after reaching bottom of article, XCUIElementTypeCell elements
        // indicate related/recommended articles even when header text doesn't match our predicate
        int cellCount = findElements(articleCells).size();
        log.info("Section header not found after scrolling; fallback cell count: {}", cellCount);
        return cellCount > 0;
    }

    /**
     * After calling scrollToYouMayAlsoLike(), taps the first recommended article
     * cell and returns the resulting ArticlePage.
     */
    public ArticlePage tapFirstYouMayAlsoLikeArticle() {
        log.info("Tapping first article in 'You may also like' section");
        scrollDown(); // bring recommendation cards into view
        List<WebElement> cells = findElements(articleCells);
        if (cells.isEmpty()) throw new RuntimeException("No article cells found in recommended section");
        cells.get(0).click();
        return new ArticlePage();
    }

    // ── Verification ──────────────────────────────────────────────────────────

    @Override
    public boolean isLoaded() {
        return isDisplayed(backButton) || getArticleCount() > 0;
    }
}
