package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SearchPage — search bar and search results screen.
 */
public class SearchPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────
    // Search is opened via sideNavIconDark (hamburger) on the home screen.
    // Verified via Appium inspector (iOS 26.3, com.2ergoTOI.jayant).
    //
    // Screen layout:
    //   [newBackBtnIcon]  [new-searchIcon-dark]  [TextField]
    //   "RECENT SEARCHES" / "Your recent searches will show up here"
    //   [CLEAR ALL] button
    //   "TRENDING STORIES" section with article cells

    private final By backButton         = byAccessibilityId("newBackBtnIcon");
    private final By searchIcon         = byAccessibilityId("new-searchIcon-dark");
    private final By searchBar          = byClassChain("**/XCUIElementTypeTextField");
    private final By searchHintText     = byAccessibilityId("Search news and photos...");
    private final By recentSearchesLabel = byAccessibilityId("RECENT SEARCHES");
    private final By clearAllButton     = byAccessibilityId("CLEAR ALL");
    private final By trendingStoriesLabel = byAccessibilityId("TRENDING STORIES");
    private final By searchResults      = byPredicateString("type == 'XCUIElementTypeCell' AND visible == true");
    private final By noResultsLabel     = byPredicateString("type == 'XCUIElementTypeStaticText' AND label CONTAINS 'No results'");

    // ── Actions ───────────────────────────────────────────────────────────────

    public SearchPage searchFor(String keyword) {
        log.info("Searching for: {}", keyword);
        tap(searchBar);
        typeText(searchBar, keyword);
        driver.findElement(searchBar).sendKeys("\n");   // submit search
        return this;
    }

    public ArticlePage tapResultAtIndex(int index) {
        List<WebElement> results = findElements(searchResults);
        if (results.isEmpty()) {
            throw new RuntimeException("No search results displayed.");
        }
        results.get(index).click();
        return new ArticlePage();
    }

    public ArticlePage tapFirstResult() {
        return tapResultAtIndex(0);
    }

    public SearchPage clearSearch() {
        tap(clearAllButton);
        return this;
    }

    public HomePage goBack() {
        tap(backButton);
        return new HomePage();
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int getResultCount() {
        return findElements(searchResults).size();
    }

    public List<String> getResultTitles() {
        return findElements(searchResults)
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public boolean isNoResultsDisplayed() {
        return isDisplayed(noResultsLabel);
    }

    public boolean isTrendingStoriesDisplayed() {
        return isDisplayed(trendingStoriesLabel);
    }

    /** Alias kept for test compatibility. */
    public boolean isTrendingSearchesDisplayed() {
        return isTrendingStoriesDisplayed();
    }

    public boolean isRecentSearchesDisplayed() {
        return isDisplayed(recentSearchesLabel);
    }

    /** Alias for goBack — kept for test compatibility. */
    public HomePage cancelSearch() {
        return goBack();
    }

    // ── Verification ──────────────────────────────────────────────────────────

    @Override
    public boolean isLoaded() {
        return isDisplayed(searchBar);
    }
}
