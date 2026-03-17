package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;

/**
 * SideNavPage — left-hand navigation drawer opened via the hamburger icon.
 * Locators verified on live device: Rishabh Khare's iPhone, iOS 26.3, 2026-03-10.
 *
 * Screen elements (top to bottom):
 *   Profile button
 *   "Login unlocks the good stuff" banner (taps → LoginPage)
 *   Notifications button (badge count varies)
 *   Bookmarks button
 *   "Local to Global" section header
 *   City / <city> button  |  India button  |  World button
 *   "Featured Five" section header
 *   Featured Five items (scrollable)
 *   Bottom bar: back arrow | Talk to us | Search | Settings
 */
public class SideNavPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────
    // Verified accessibility IDs from live page source
    private final By profileBtn          = byAccessibilityId("Profile");
    private final By loginUnlocksBanner  = byPredicateString(
            "type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'login unlocks the good stuff'");
    private final By notificationsItem   = byPredicateString(
            "type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'notifications'");
    private final By bookmarkItem        = byAccessibilityId("Bookmarks");
    private final By localToGlobalHeader = byAccessibilityId("Local to Global");
    private final By cityBtn             = byPredicateString(
            "type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'city'");
    private final By indiaBtn            = byAccessibilityId("India");
    private final By worldBtn            = byAccessibilityId("World");
    private final By featuredFiveHeader  = byAccessibilityId("Featured Five");
    private final By talkToUsBtn         = byAccessibilityId("Talk to us");
    private final By searchItem          = byAccessibilityId("magnifyingglass");
    private final By homeItem            = byAccessibilityId("Home");
    private final By liveNewsItem        = byAccessibilityId("Live News");
    private final By gamingItem          = byPredicateString(
            "(type == 'XCUIElementTypeStaticText' OR type == 'XCUIElementTypeButton' OR type == 'XCUIElementTypeCell') AND " +
            "(label CONTAINS[cd] 'gaming' OR label CONTAINS[cd] 'games' OR label CONTAINS[cd] 'game')");
    private final By closeBtn            = byAccessibilityId("sideNavIconDark");

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Tap a section by its exact display label (e.g. "India", "Entertainment", "Sports").
     * Scrolls the drawer if the item is not immediately visible.
     */
    public CategoryPage tapSection(String label) {
        log.info("Side nav: tapping section '{}'", label);
        By loc = byAccessibilityId(label);
        if (!isDisplayed(loc)) {
            // Fallback: match by StaticText/Button label (handles accessibility ID mismatches)
            loc = byPredicateString(
                    "(type == 'XCUIElementTypeStaticText' OR type == 'XCUIElementTypeButton') AND " +
                    "label CONTAINS[cd] '" + label + "'");
            if (!isDisplayed(loc)) {
                // Scroll down a few times to find the section
                for (int i = 0; i < 4 && !isDisplayed(loc); i++) {
                    scrollDown();
                }
            }
        }
        tap(loc);
        return new CategoryPage(label);
    }

    public LoginPage tapLoginUnlocks() {
        log.info("Side nav: tapping 'Login unlocks the good stuff'");
        tap(loginUnlocksBanner);
        return new LoginPage();
    }

    public SearchPage tapSearch() {
        log.info("Side nav: tapping Search");
        tap(searchItem);
        return new SearchPage();
    }

    public CategoryPage tapLiveNews() {
        return tapSection("Live News");
    }

    public CategoryPage tapGaming() {
        log.info("Side nav: tapping GAMING/Games");
        if (!isDisplayed(gamingItem)) {
            // scroll down a few times to find the gaming section
            for (int i = 0; i < 4 && !isDisplayed(gamingItem); i++) {
                scrollDown();
            }
        }
        tap(gamingItem);
        return new CategoryPage("GAMING");
    }

    public HomePage close() {
        log.info("Closing side nav");
        tap(closeBtn);
        return new HomePage();
    }

    // ── Verification ──────────────────────────────────────────────────────────

    @Override
    public boolean isLoaded() {
        return isDisplayed(profileBtn) || isDisplayed(loginUnlocksBanner) || isDisplayed(bookmarkItem);
    }
}
