package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;

/**
 * SignUpPage — new user registration screen.
 */
public class SignUpPage extends BasePage {

    private final By nameField       = byAccessibilityId("Full Name");
    private final By emailField      = byAccessibilityId("Email");
    private final By passwordField   = byAccessibilityId("Password");
    private final By signUpButton    = byAccessibilityId("Create Account");
    private final By loginLink       = byAccessibilityId("Already have an account?");
    private final By signUpTitle     = byAccessibilityId("Create Account");
    private final By errorMessage    = byPredicateString("type == 'XCUIElementTypeStaticText' AND label CONTAINS 'error'");

    public HomePage register(String name, String email, String password) {
        log.info("Registering new user: {}", email);
        typeText(nameField, name);
        typeText(emailField, email);
        typeText(passwordField, password);
        tap(signUpButton);
        return new HomePage();
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessage);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public LoginPage goToLogin() {
        tap(loginLink);
        return new LoginPage();
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(signUpTitle);
    }
}
