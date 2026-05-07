package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;

/**
 * CategoryPage — news listing for a specific category (e.g. Sports, Tech, Business).
 */
public class CategoryPage extends BasePage {

    private final String categoryName;
    private final By categoryTitle;
    private final By articleCards = byPredicateString("type == 'XCUIElementTypeCell'");

    public CategoryPage(String categoryName) {
        super();
        this.categoryName  = categoryName;
        this.categoryTitle = byAccessibilityId(categoryName);
    }

    public int getArticleCount() {
        return findElements(articleCards).size();
    }

    public ArticlePage tapArticleAtIndex(int index) {
        findElements(articleCards).get(index).click();
        return new ArticlePage();
    }

    public ArticlePage tapFirstArticle() {
        return tapArticleAtIndex(0);
    }

    public CategoryPage scroll() {
        scrollDown();
        return this;
    }

    /** Navigates directly to another side-nav section without returning to home.
     *  Scrolls up to reveal the nav bar; if still hidden uses a status-bar tap
     *  (standard iOS gesture to scroll-to-top and reveal the hidden nav bar) followed
     *  by a coordinate tap at the hamburger icon's known position. */
    public CategoryPage navigateToSection(String sectionLabel) {
        log.info("Navigating from '{}' to '{}' via side nav", categoryName, sectionLabel);
        By sideNavBtn = byPredicateString(
                "type == 'XCUIElementTypeButton' AND (name == 'sideNavIconDark' OR name == 'sideNavIconLight')");

        // Use findElements (no wait) for fast per-attempt check
        for (int i = 0; i < 4 && findElements(sideNavBtn).isEmpty(); i++) {
            scrollUp();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }

        if (!findElements(sideNavBtn).isEmpty()) {
            findElements(sideNavBtn).get(0).click();
        } else {
            // Nav bar still hidden after scroll-ups.
            // Tap the status bar (y≈25) — standard iOS gesture to scroll-to-top and
            // force the navigation bar back into view, then tap the hamburger by coordinate.
            log.info("Side nav button not found after scroll-ups — tapping status bar + hamburger coordinate");
            org.openqa.selenium.Dimension sz = driver.manage().window().getSize();
            driver.executeScript("mobile: tap", java.util.Map.of("x", sz.width / 2, "y", 25));
            try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
            driver.executeScript("mobile: tap", java.util.Map.of(
                    "x", (int)(sz.width  * 0.07),   // hamburger: ~7 % from left
                    "y", (int)(sz.height * 0.08)));  // hamburger: ~8 % from top (nav bar row)
        }

        return new SideNavPage().tapSection(sectionLabel);
    }

    /** Taps the back arrow to return to the previous screen (Home).
     *  Verified on live device 2026-03-11: name="new backIcon dark" on category/listing pages.
     *  Scrolls up first to reveal the nav bar if it was hidden by downward scrolling. */
    public HomePage goBack() {
        log.info("Navigating back from CategoryPage: {}", categoryName);
        By backBtn = byAccessibilityId("new backIcon dark");
        for (int i = 0; i < 3 && !isDisplayed(backBtn); i++) {
            // Nav bar may be hidden after scrolling down — scroll up to restore it
            scrollUp();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }
        tap(backBtn);
        // Allow transition animation to complete before caller interacts with home screen
        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
        return new HomePage();
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(categoryTitle) || getArticleCount() > 0;
    }
}
