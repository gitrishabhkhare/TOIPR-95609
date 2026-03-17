package com.toi.tests.regression;

import com.toi.base.BaseTest;
import com.toi.config.ConfigReader;
import com.toi.pages.ForgotPasswordPage;
import com.toi.pages.LoginPage;
import com.toi.pages.SignUpPage;
import com.toi.pages.SplashPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Regression tests — comprehensive coverage of Login, SignUp and ForgotPassword flows.
 */
public class LoginRegressionTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod(alwaysRun = true)
    public void openLoginPage() {
        loginPage = new SplashPage().skipOnboarding().tapProfile();
        Assert.assertTrue(loginPage.isLoaded(), "Login page should be loaded before test");
    }

    @Test(groups = {"regression"},
          description = "Verify empty email shows validation error")
    public void testEmptyEmailValidation() {
        loginPage.enterEmail("")
                 .enterPassword("anypassword")
                 .clickLoginExpectingError();
        Assert.assertTrue(loginPage.isErrorDisplayed(),
                "Validation error should appear for empty email");
    }

    @Test(groups = {"regression"},
          description = "Verify empty password shows validation error")
    public void testEmptyPasswordValidation() {
        loginPage.enterEmail("valid@test.com")
                 .enterPassword("")
                 .clickLoginExpectingError();
        Assert.assertTrue(loginPage.isErrorDisplayed(),
                "Validation error should appear for empty password");
    }

    @Test(groups = {"regression"},
          description = "Verify invalid email format shows validation error")
    public void testInvalidEmailFormat() {
        loginPage.enterEmail("notanemail")
                 .enterPassword("Password123")
                 .clickLoginExpectingError();
        Assert.assertTrue(loginPage.isErrorDisplayed(),
                "Validation error should appear for malformed email");
    }

    @Test(groups = {"regression"},
          description = "Verify Sign Up navigation from Login page")
    public void testNavigationToSignUp() {
        SignUpPage signUp = loginPage.goToSignUp();
        Assert.assertTrue(signUp.isLoaded(),
                "Sign Up page should load from Login page");
    }

    @Test(groups = {"regression"},
          description = "Verify Forgot Password navigation from Login page")
    public void testNavigationToForgotPassword() {
        ForgotPasswordPage forgotPwd = loginPage.goToForgotPassword();
        Assert.assertTrue(forgotPwd.isLoaded(),
                "Forgot Password page should load from Login page");
    }

    @Test(groups = {"regression"},
          description = "Verify forgot password — valid email sends reset link")
    public void testForgotPasswordValidEmail() {
        ForgotPasswordPage forgotPwd = loginPage.goToForgotPassword();
        forgotPwd.enterEmail(ConfigReader.get("test.user.email"))
                 .submitReset();
        Assert.assertTrue(forgotPwd.isSuccessMessageDisplayed(),
                "Success message should appear after submitting a valid email for reset");
    }

    @Test(groups = {"regression"},
          description = "Verify back button from Login returns to Home")
    public void testBackFromLoginReturnsHome() {
        var home = loginPage.goBack();
        Assert.assertTrue(home.isLoaded(),
                "Home page should load after going back from Login");
    }

    @Test(groups = {"regression"},
          description = "Verify successful login with valid credentials")
    public void testSuccessfulLogin() {
        var home = loginPage.loginWithCredentials(
                ConfigReader.get("test.user.email"),
                ConfigReader.get("test.user.password"));
        Assert.assertTrue(home.isLoaded(),
                "Home page should load after successful login");
    }
}
