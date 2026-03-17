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

    /** Taps the back arrow to return to the previous screen (Home).
     *  Verified on live device 2026-03-11: name="new backIcon dark" on category/listing pages. */
    public HomePage goBack() {
        log.info("Navigating back from CategoryPage: {}", categoryName);
        By backBtn = byAccessibilityId("new backIcon dark");
        tap(backBtn);
        return new HomePage();
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(categoryTitle) || getArticleCount() > 0;
    }
}
