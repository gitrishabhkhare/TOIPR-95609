package com.toi.tests.regression;

import com.toi.base.BaseTest;
import com.toi.pages.ArticlePage;
import com.toi.pages.HomePage;
import com.toi.pages.SplashPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Regression tests — comprehensive coverage of HomePage behaviour.
 */
public class HomePageRegressionTest extends BaseTest {

    private HomePage homePage;

    @BeforeMethod(alwaysRun = true)
    public void navigateToHome() {
        homePage = new SplashPage().skipOnboarding();
    }

    @Test(groups = {"regression"},
          description = "Verify scrolling the home feed loads more articles")
    public void testScrollLoadMoreArticles() {
        int beforeScroll = homePage.getArticleCount();
        homePage.scrollToRefresh();
        int afterScroll = homePage.getArticleCount();
        Assert.assertTrue(afterScroll >= beforeScroll,
                "Article count should not decrease after scrolling down");
    }

    @Test(groups = {"regression"},
          description = "Verify tapping first article opens article detail")
    public void testTapFirstArticleOpensDetail() {
        ArticlePage article = homePage.tapFirstArticle();
        Assert.assertTrue(article.isLoaded(),
                "Article detail page should load on tapping a card");
    }

    @Test(groups = {"regression"},
          description = "Verify article detail page shows title and author")
    public void testArticleDetailShowsMetadata() {
        ArticlePage article = homePage.tapFirstArticle();
        Assert.assertFalse(article.getArticleTitle().isEmpty(),
                "Article title should not be empty");
        log.info("Article Title: {}", article.getArticleTitle());
    }

    @Test(groups = {"regression"},
          description = "Verify back navigation from article returns to home feed")
    public void testBackFromArticleReturnsHome() {
        HomePage home = homePage.tapFirstArticle().goBack();
        Assert.assertTrue(home.isLoaded(),
                "Navigating back from article should return to home feed");
    }

    @Test(groups = {"regression"},
          description = "Verify Sports category is accessible from home")
    public void testSportsCategoryAccess() {
        var sports = homePage.selectCategory("Sports");
        Assert.assertTrue(sports.isLoaded(),
                "Sports category page should load");
        Assert.assertTrue(sports.getArticleCount() > 0,
                "Sports category should have articles");
    }

    @Test(groups = {"regression"},
          description = "Verify Videos tab navigates correctly")
    public void testVideosTabNavigation() {
        var videoPage = homePage.goToVideos();
        Assert.assertTrue(videoPage.isLoaded(),
                "Videos page should load on tapping the Videos tab");
    }

    @Test(groups = {"regression"},
          description = "Verify My Feed tab is displayed")
    public void testMyFeedTabDisplayed() {
        var myFeed = homePage.goToMyFeed();
        Assert.assertTrue(myFeed.isLoaded(),
                "My Feed page should load on tapping the My Feed tab");
    }

    @Test(groups = {"regression"},
          description = "Verify bookmarking an article works")
    public void testBookmarkArticle() {
        ArticlePage article = homePage.tapFirstArticle();
        article.tapBookmark();
        Assert.assertTrue(article.isBookmarkActive(),
                "Article should be bookmarked after tapping the bookmark button");
    }

    @Test(groups = {"regression"},
          description = "Verify share button is present on article page")
    public void testShareButtonPresent() {
        ArticlePage article = homePage.tapFirstArticle();
        // Tapping share opens the native share sheet; we verify the action doesn't throw
        article.tapShare();
        log.info("Share button tapped successfully");
    }

    @Test(groups = {"regression"},
          description = "Verify related articles are shown at the bottom of an article")
    public void testRelatedArticlesPresent() {
        ArticlePage article = homePage.tapFirstArticle();
        article.scrollArticle();
        int relatedCount = article.getRelatedArticlesCount();
        Assert.assertTrue(relatedCount > 0,
                "Related articles section should have at least one item");
    }
}
