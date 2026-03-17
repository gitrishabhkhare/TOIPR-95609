package com.toi.tests.sanity;

import com.toi.base.BaseTest;
import com.toi.config.ConfigReader;
import com.toi.pages.HomePage;
import com.toi.pages.LoginPage;
import com.toi.pages.SplashPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Sanity tests — verify critical login paths work end-to-end.
 */
public class LoginSanityTest extends BaseTest {

    @Test(groups = {"sanity", "smoke"},
          description = "Verify successful login with valid credentials")
    public void testValidLogin() {
        String email    = ConfigReader.get("test.user.email");
        String password = ConfigReader.get("test.user.password");

        HomePage home = new SplashPage()
                .skipOnboarding()
                .tapProfile()
                .loginWithCredentials(email, password);

        Assert.assertTrue(home.isLoaded(),
                "Home page should load after successful login");
    }

    @Test(groups = {"sanity"},
          description = "Verify login page loads when profile icon is tapped")
    public void testLoginPageLoads() {
        LoginPage loginPage = new SplashPage()
                .skipOnboarding()
                .tapProfile();

        Assert.assertTrue(loginPage.isLoaded(),
                "Login page should be displayed");
    }

    @Test(groups = {"sanity"},
          description = "Verify error shown for invalid credentials")
    public void testInvalidLoginShowsError() {
        LoginPage loginPage = new SplashPage()
                .skipOnboarding()
                .tapProfile()
                .enterEmail("invalid@test.com")
                .enterPassword("wrongpassword")
                .clickLoginExpectingError();

        Assert.assertTrue(loginPage.isErrorDisplayed(),
                "An error message should be displayed for invalid credentials");
    }

    @Test(groups = {"sanity"},
          description = "Verify search icon is accessible from home without login")
    public void testSearchAccessibleWithoutLogin() {
        var searchPage = new SplashPage()
                .skipOnboarding()
                .tapSearch();

        Assert.assertTrue(searchPage.isLoaded(),
                "Search page should open without requiring login");
    }
}
