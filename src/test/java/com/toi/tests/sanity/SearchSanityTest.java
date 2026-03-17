package com.toi.tests.sanity;

import com.toi.base.BaseTest;
import com.toi.pages.SearchPage;
import com.toi.pages.SplashPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Sanity tests — verify core search functionality.
 */
public class SearchSanityTest extends BaseTest {

    @Test(groups = {"sanity", "smoke"},
          description = "Verify search returns results for a known keyword")
    public void testSearchReturnsResults() {
        SearchPage searchPage = new SplashPage()
                .skipOnboarding()
                .tapSearch()
                .searchFor("India");

        int resultCount = searchPage.getResultCount();
        Assert.assertTrue(resultCount > 0,
                "Search for 'India' should return at least one result");
        log.info("Search returned {} results", resultCount);
    }

    @Test(groups = {"sanity"},
          description = "Verify search with unknown keyword shows no-results state")
    public void testSearchNoResultsForGibberish() {
        SearchPage searchPage = new SplashPage()
                .skipOnboarding()
                .tapSearch()
                .searchFor("xyzxyzxyz999randomstring");

        Assert.assertTrue(searchPage.isNoResultsDisplayed(),
                "No-results message should be shown for unrecognised keyword");
    }

    @Test(groups = {"sanity"},
          description = "Verify tapping a search result opens the article")
    public void testSearchResultOpensArticle() {
        var articlePage = new SplashPage()
                .skipOnboarding()
                .tapSearch()
                .searchFor("Cricket")
                .tapFirstResult();

        Assert.assertTrue(articlePage.isLoaded(),
                "Tapping a search result should open the article detail page");
    }
}
