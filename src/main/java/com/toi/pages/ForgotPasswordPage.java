package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;

/**
 * ForgotPasswordPage — password reset screen.
 */
public class ForgotPasswordPage extends BasePage {

    private final By emailField       = byAccessibilityId("Email");
    private final By sendResetButton  = byAccessibilityId("Send Reset Link");
    private final By successMessage   = byAccessibilityId("Reset link sent");
    private final By backButton       = byAccessibilityId("Back");
    private final By pageTitle        = byAccessibilityId("Forgot Password");

    public ForgotPasswordPage enterEmail(String email) {
        typeText(emailField, email);
        return this;
    }

    public ForgotPasswordPage submitReset() {
        tap(sendResetButton);
        return this;
    }

    public boolean isSuccessMessageDisplayed() {
        return isDisplayed(successMessage);
    }

    public LoginPage goBack() {
        tap(backButton);
        return new LoginPage();
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(pageTitle);
    }
}
