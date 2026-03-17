package com.toi.tests.regression;

import com.toi.base.BaseTest;
import com.toi.pages.HomePage;
import com.toi.pages.SearchPage;
import com.toi.pages.SplashPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Regression tests — comprehensive coverage of Search functionality.
 */
public class SearchRegressionTest extends BaseTest {

    private SearchPage searchPage;

    @BeforeMethod(alwaysRun = true)
    public void openSearch() {
        searchPage = new SplashPage().skipOnboarding().tapSearch();
        Assert.assertTrue(searchPage.isLoaded(), "Search page should load before test");
    }

    @DataProvider(name = "searchKeywords")
    public Object[][] searchKeywords() {
        return new Object[][]{
                {"India"},
                {"Cricket"},
                {"Bollywood"},
                {"Stock Market"},
                {"Weather"},
        };
    }

    @Test(groups = {"regression"},
          dataProvider = "searchKeywords",
          description = "Verify search returns results for various news categories")
    public void testSearchForMultipleKeywords(String keyword) {
        searchPage.searchFor(keyword);
        int count = searchPage.getResultCount();
        Assert.assertTrue(count > 0,
                "Search for '" + keyword + "' should return results, but got " + count);
        log.info("'{}' returned {} result(s)", keyword, count);
    }

    @Test(groups = {"regression"},
          description = "Verify result titles are non-empty after search")
    public void testSearchResultTitlesNonEmpty() {
        searchPage.searchFor("Technology");
        List<String> titles = searchPage.getResultTitles();
        Assert.assertFalse(titles.isEmpty(), "Result titles list should not be empty");
        titles.forEach(title ->
                Assert.assertFalse(title.isBlank(), "Each result title should be non-blank"));
    }

    @Test(groups = {"regression"},
          description = "Verify trending searches are shown on empty search bar")
    public void testTrendingSearchesVisible() {
        Assert.assertTrue(searchPage.isTrendingSearchesDisplayed(),
                "Trending searches should be visible when search bar is empty");
    }

    @Test(groups = {"regression"},
          description = "Verify cancel button returns to home feed")
    public void testCancelSearchReturnsHome() {
        HomePage home = searchPage.cancelSearch();
        Assert.assertTrue(home.isLoaded(),
                "Home feed should be visible after cancelling search");
    }

    @Test(groups = {"regression"},
          description = "Verify search result navigates to article detail")
    public void testSearchResultNavigation() {
        var article = searchPage.searchFor("Politics").tapFirstResult();
        Assert.assertTrue(article.isLoaded(),
                "Article detail page should load when a search result is tapped");
        Assert.assertFalse(article.getArticleTitle().isEmpty(),
                "Article should have a title");
    }

    @Test(groups = {"regression"},
          description = "Verify clearing search input restores the search bar")
    public void testClearSearch() {
        searchPage.searchFor("Business").clearSearch();
        Assert.assertTrue(searchPage.isLoaded(),
                "Search page should remain active after clearing input");
    }
}
