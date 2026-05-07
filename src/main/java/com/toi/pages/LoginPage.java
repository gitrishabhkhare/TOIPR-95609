package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;

/**
 * LoginPage — sign-in screen for TOI app (Times Login).
 * Locators verified on live device: Rishabh Khare's iPhone, iOS 26.3, 2026-03-10.
 * Opened via: SideNav → "Login unlocks the good stuff" banner.
 *
 * Screen layout (top to bottom):
 *   Illustration (key image)
 *   "One Account, across Times"  ← heading (StaticText, off-screen at top)
 *   "TIMES LOGIN" logo
 *   "Sign In/Sign Up with" label  ← accessibility id: "Sign In/Sign Up with"
 *   [XCUIElementTypeTextField]    ← email/phone input (no name/label; placeholder "Email or Mobile number")
 *   Arrow/Continue button (→)
 *   "Or" divider
 *   "Continue with Google" button ← accessibility id: "Continue with Google"
 *   "Continue with Apple" button  ← accessibility id: "Continue with Apple"
 *   Terms of use / Privacy Policy text + links
 */
public class LoginPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────
    // Verified on live device: Rishabh Khare's iPhone, iOS 26.3, 2026-03-10
    // Keyboard-active state confirmed via page source dump.

    // "Sign In/Sign Up with" label (visible=true, x=42 y=321)
    private final By signInHeading      = byAccessibilityId("Sign In/Sign Up with");

    // Email/mobile text field — no name/label on device; matched by type only
    // Placeholder StaticText: accessibilityId("Email or Mobile number") — visible=false when focused
    private final By emailField         = byPredicateString(
            "type == 'XCUIElementTypeTextField'");

    // Arrow (→) button next to text field — label may be '' or the '→' unicode char
    private final By continueArrowBtn   = byPredicateString(
            "type == 'XCUIElementTypeButton' AND (label == '' OR label == '→')");
    private final By continueButton     = byPredicateString(
            "type == 'XCUIElementTypeButton' AND (label == 'Continue' OR label == 'Next')");

    // Social login buttons (verified accessibility IDs)
    private final By googleSignInButton = byAccessibilityId("Continue with Google");
    private final By appleSignInButton  = byAccessibilityId("Continue with Apple");

    // "Or" divider (visible=true, x=193 y=419)
    private final By orDivider          = byAccessibilityId("Or");

    // Terms / Privacy links (visible when keyboard is hidden)
    private final By termsText          = byPredicateString(
            "type == 'XCUIElementTypeStaticText' AND label CONTAINS 'Terms Of Use'");
    private final By termsOfUseLink     = byPredicateString(
            "type == 'XCUIElementTypeLink' AND label == 'Terms Of Use'");
    private final By privacyPolicyLink  = byPredicateString(
            "type == 'XCUIElementTypeLink' AND label == 'Privacy Policy'");

    private final By errorMessage       = byPredicateString(
            "type == 'XCUIElementTypeStaticText' AND (label CONTAINS[cd] 'error' OR label CONTAINS[cd] 'invalid' OR label CONTAINS[cd] 'incorrect')");

    // ── OTP Screen locators (after email is submitted) ─────────────────────────
    // Screen: "An OTP is shared on" + email + 6 OTP boxes + Resend + Continue with Password
    // OTP boxes are custom UI overlay over a single hidden TextField (no accessibility id)
    // Input is collected by the hidden TextField; tap box area and use number keyboard
    private final By otpScreenHeading       = byAccessibilityId("An OTP is shared on");
    private final By otpEmailLabel          = byPredicateString(
            "type == 'XCUIElementTypeStaticText' AND label CONTAINS[cd] '@'");
    private final By otpChangeEmailBtn      = byAccessibilityId("Change");
    private final By otpHiddenInputField    = byPredicateString(
            "type == 'XCUIElementTypeTextField'");           // hidden; value = entered email
    private final By resendOtpTimer         = byAccessibilityId("Resend OTP in");
    private final By resendOtpBtn           = byAccessibilityId("Resend OTP");
    private final By continueWithPasswordBtn = byAccessibilityId("or Continue with Password");

    // ── Password Screen locators (after "or Continue with Password" is tapped) ─
    // Screen: "Hi" + email + "Enter Your Password" field + eye icon + Continue + Forgot Password
    private final By passwordScreenGreeting  = byAccessibilityId("Hi");
    private final By passwordEmailLabel      = byPredicateString(
            "type == 'XCUIElementTypeStaticText' AND label CONTAINS[cd] '@'");
    private final By passwordChangeEmailBtn  = byAccessibilityId("Change");
    private final By passwordPlaceholder     = byAccessibilityId("Enter Your Password");
    private final By passwordField           = byPredicateString(
            "type == 'XCUIElementTypeSecureTextField'");     // no name/label on device
    private final By passwordShowToggleBtn   = byAccessibilityId("Show");  // eye icon; label='Show'
    private final By passwordContinueBtn     = byAccessibilityId("Continue");
    private final By forgotPasswordBtn       = byAccessibilityId("Forgot Password?");

    // Legacy alias kept for test compatibility
    private final By otpOrPasswordField      = passwordField;

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Enter email/phone and tap Continue to proceed to OTP/password step. */
    public LoginPage enterEmail(String email) {
        log.info("Entering email: {}", email);
        typeText(emailField, email);  // uses forElementVisible (not clickable) to avoid XCUITest clickability timeout
        return this;
    }

    public LoginPage tapContinue() {
        // Quick check with implicit wait (faster than isDisplayed which uses explicit 10s wait)
        List<WebElement> arrowBtns = driver.findElements(continueArrowBtn);
        if (!arrowBtns.isEmpty()) {
            arrowBtns.get(0).click();
            return this;
        }
        // Coordinate fallback: tap to the right of the email field where the → arrow sits
        try {
            WebElement field = driver.findElement(emailField);
            int x = field.getLocation().getX() + field.getSize().getWidth() + 20;
            int y = field.getLocation().getY() + field.getSize().getHeight() / 2;
            log.info("LoginPage: arrow btn not found by label — coord tap at ({}, {})", x, y);
            driver.executeScript("mobile: tap", Map.of("x", x, "y", y));
        } catch (Exception e) {
            log.warn("LoginPage: coord fallback failed — trying Continue/Next: {}", e.getMessage());
            tap(continueButton);
        }
        return this;
    }

    public LoginPage tapContinueWithPassword() {
        log.info("Tapping 'or Continue with Password'");
        tap(continueWithPasswordBtn);
        return this;
    }

    public LoginPage tapResendOtp() {
        log.info("Tapping Resend OTP");
        tap(resendOtpBtn);
        return this;
    }

    public LoginPage enterPassword(String password) {
        log.info("Entering password");
        typeText(passwordField, password);
        return this;
    }

    public LoginPage togglePasswordVisibility() {
        tap(passwordShowToggleBtn);
        return this;
    }

    public HomePage tapPasswordContinue() {
        log.info("Tapping Continue on password screen");
        tap(passwordContinueBtn);
        return new HomePage();
    }

    public LoginPage enterOtpOrPassword(String value) {
        log.info("Entering OTP/password");
        typeText(otpOrPasswordField, value);
        return this;
    }

    /**
     * Full email + password login flow.
     * Flow: enter email → tap arrow → OTP screen → tap "or Continue with Password"
     *       → enter password → tap Continue.
     * Verified on live device 2026-03-11.
     */
    public HomePage loginWithEmail(String email, String credential) {
        enterEmail(email);
        tapContinue();                   // taps arrow (→) button — no label
        tap(continueWithPasswordBtn);    // "or Continue with Password" on OTP screen
        enterPassword(credential);
        tap(passwordContinueBtn);        // "Continue" on password screen
        return new HomePage();
    }

    public HomePage signInWithGoogle() {
        log.info("Tapping Continue with Google");
        tap(googleSignInButton);
        return new HomePage();
    }

    public HomePage signInWithApple() {
        log.info("Tapping Continue with Apple");
        tap(appleSignInButton);
        return new HomePage();
    }

    /** Alias for loginWithEmail — kept for test compatibility. */
    public HomePage loginWithCredentials(String email, String password) {
        return loginWithEmail(email, password);
    }


    public LoginPage clickLoginExpectingError() {
        tapContinue();
        return this;
    }

    public SignUpPage goToSignUp() {
        log.info("Navigating to Sign Up");
        By signUpBtn = byPredicateString("type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'sign up'");
        tap(signUpBtn);
        return new SignUpPage();
    }

    public ForgotPasswordPage goToForgotPassword() {
        log.info("Navigating to Forgot Password");
        By forgotBtn = byPredicateString("type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'forgot'");
        tap(forgotBtn);
        return new ForgotPasswordPage();
    }

    public HomePage goBack() {
        By backBtn = byPredicateString("type == 'XCUIElementTypeButton' AND (label == 'Cancel' OR label CONTAINS[cd] 'back')");
        tap(backBtn);
        return new HomePage();
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessage);
    }

    public boolean isGoogleSignInDisplayed() {
        // Try exact predicate, then any element type containing "google"
        if (isDisplayed(googleSignInButton)) return true;
        return isDisplayed(byPredicateString("label CONTAINS[cd] 'google'"));
    }

    public boolean isAppleSignInDisplayed() {
        // Try exact predicate, then any element type containing "apple" or "sign in with apple"
        if (isDisplayed(appleSignInButton)) return true;
        return isDisplayed(byPredicateString("label CONTAINS[cd] 'apple'"));
    }

    // ── Verification ──────────────────────────────────────────────────────────

    @Override
    public boolean isLoaded() {
        // One combined predicate avoids multiple 5-sec timeouts
        By anyLoginElement = byPredicateString(
                "type == 'XCUIElementTypeTextField' OR " +
                "type == 'XCUIElementTypeTextView' OR " +
                "(type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'google') OR " +
                "(type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'apple') OR " +
                "(type == 'XCUIElementTypeStaticText' AND label CONTAINS[cd] 'sign in')");
        return isDisplayed(signInHeading) || isDisplayed(anyLoginElement);
    }
}
