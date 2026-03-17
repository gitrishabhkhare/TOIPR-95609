package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;

/**
 * MyFeedPage — personalised news feed for logged-in users.
 */
public class MyFeedPage extends BasePage {

    // "For You" tab maps to personalised feed; tab bar "Home-01" is the Newsfeed.
    private final By forYouTab          = byPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'For You'");
    private final By feedArticles       = byPredicateString("type == 'XCUIElementTypeCell' AND visible == true");
    private final By loginPrompt        = byPredicateString("type == 'XCUIElementTypeStaticText' AND label CONTAINS 'sign in'");
    private final By trendingTopics     = byPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'TRENDING TOPICS'");

    public int getFeedArticleCount() {
        return findElements(feedArticles).size();
    }

    public ArticlePage tapFirstFeedArticle() {
        findElements(feedArticles).get(0).click();
        return new ArticlePage();
    }

    public boolean isLoginPromptDisplayed() {
        return isDisplayed(loginPrompt);
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(forYouTab) || findElements(feedArticles).size() > 0;
    }
}
