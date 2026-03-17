package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;

/**
 * EpaperPage — ePaper tab on the TOI home screen.
 * Locators verified on live device: Rishabh Khare's iPhone, iOS 26.3, 2026-03-10.
 *
 * How to reach: Home screen → tap "ePaper" tab (top nav bar, 3rd tab after Top | For You)
 *
 * Screen layout:
 *   Top nav: Top | For You | ePaper (selected, underlined)
 *   Newspaper front page preview image (ScrollView, x=0 y=344 w=402 h=447)
 *   [Subscribe to ePaper] button — bottom-right of preview
 *   Bottom tab bar: Newsfeed | T20 WC | Exclusive
 */
public class EpaperPage extends BasePage {

    // ── Tab locators (top nav bar) ─────────────────────────────────────────────
    // All three tabs are StaticText elements at y=312
    private final By epaperTab    = byAccessibilityId("ePaper");   // x=308 y=312 w=51 h=22
    private final By topTab       = byAccessibilityId("Top");      // x=55  y=312 w=28 h=22
    private final By forYouTab    = byAccessibilityId("For You");  // x=174 y=312 w=54 h=22

    // ── ePaper screen elements ────────────────────────────────────────────────
    // Newspaper front page — rendered inside a ScrollView (no accessibility id)
    private final By newspaperPreview   = byPredicateString(
            "type == 'XCUIElementTypeScrollView'");

    // "Subscribe to ePaper" button (visible bottom-right of preview)
    // Has both Button and StaticText with same name — Button is the tappable element
    private final By subscribeToEpaperBtn = byAccessibilityId("Subscribe to ePaper");

    // ── Bottom tab bar ────────────────────────────────────────────────────────
    private final By newsfeedTab   = byAccessibilityId("Home-01");      // label: Newsfeed
    private final By t20WcTab      = byAccessibilityId("Cricket-01");   // label: T20 WC
    private final By exclusiveTab  = byAccessibilityId("DeepRead-01");  // label: Exclusive

    // ── ePaper Blocker/Paywall locators ──────────────────────────────────────
    // Shown when a non-prime / logged-out user taps "Subscribe to ePaper".
    // How to reach: ePaper tab → tap "Subscribe to ePaper" button
    //
    // Screen layout (top to bottom):
    //   [✕] close button          ← accessibilityId: "cross dark"
    //   "ePaper" title            ← accessibilityId: "ePaper"
    //   TOI ePaper logo           ← image: "ePaperLogoLight"
    //   [Print View] [Digital View] toggle buttons
    //   Newspaper preview image
    //   "This is a TOI+ story."
    //   "Subscribe to unlock and get all member benefits:"
    //   • Ad-Free Experience
    //   • Daily TOI ePaper
    //   • 300+ Exclusives every month
    //   • Unlimited access to TOI
    //   [Subscribe Now] button
    //   "Already a Member? Sign In now" (inline link)
    //   [Anniversary Offer banner → Avail Offer] button

    // Verified on live device 2026-03-11: close button on ePaper blocker is "cross light"
    private final By blockerCloseBtn          = byAccessibilityId("cross light");
    private final By blockerTitle             = byAccessibilityId("This is a TOI+ story.");
    private final By blockerSubheading        = byAccessibilityId("Subscribe to unlock and get all member benefits:");
    private final By blockerAdFreeLabel       = byAccessibilityId("Ad-Free Experience");
    private final By blockerDailyEpaperLabel  = byAccessibilityId("Daily TOI ePaper");
    private final By blocker300Exclusives     = byAccessibilityId("300+ Exclusives every month");
    private final By blockerUnlimitedAccess   = byAccessibilityId("Unlimited access to TOI");
    private final By blockerSubscribeNowBtn   = byAccessibilityId("Subscribe Now");
    private final By blockerAlreadyMemberText = byPredicateString(
            "type == 'XCUIElementTypeTextView' AND label CONTAINS[cd] 'already a member'");
    private final By blockerSignInNowLink     = byAccessibilityId("Sign In now");
    private final By blockerAvailOfferBtn     = byPredicateString(
            "type == 'XCUIElementTypeButton' AND label == 'Button'");  // Anniversary Offer banner
    private final By printViewBtn             = byAccessibilityId("Print View");
    private final By digitalViewBtn           = byAccessibilityId("Digital View");
    private final By epaperLogo               = byAccessibilityId("ePaperLogoLight");

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Taps the "Subscribe to ePaper" button.
     * Verified on live device 2026-03-11: button is enabled=false (renders as disabled
     * in accessibility tree) but is visually tappable — use coordinate tap to bypass.
     */
    public EpaperPage tapSubscribeToEpaper() {
        log.info("ePaper: tapping Subscribe to ePaper (coordinate tap — button is enabled=false)");
        org.openqa.selenium.WebElement el = driver.findElement(subscribeToEpaperBtn);
        int x = el.getLocation().getX() + el.getSize().getWidth() / 2;
        int y = el.getLocation().getY() + el.getSize().getHeight() / 2;
        driver.executeScript("mobile: tap", java.util.Map.of("x", x, "y", y));
        return this;
    }

    public EpaperPage closeBlocker() {
        log.info("ePaper blocker: closing");
        tap(blockerCloseBtn);
        return this;
    }

    public EpaperPage tapBlockerSubscribeNow() {
        log.info("ePaper blocker: tapping Subscribe Now");
        tap(blockerSubscribeNowBtn);
        return this;
    }

    public LoginPage tapBlockerSignInNow() {
        log.info("ePaper blocker: tapping Sign In now");
        tap(blockerSignInNowLink);
        return new LoginPage();
    }

    public EpaperPage tapPrintView() {
        tap(printViewBtn);
        return this;
    }

    public EpaperPage tapDigitalView() {
        tap(digitalViewBtn);
        return this;
    }

    public HomePage goToNewsfeed() {
        log.info("ePaper: tapping Newsfeed tab");
        tap(newsfeedTab);
        return new HomePage();
    }

    // ── Verification ──────────────────────────────────────────────────────────

    @Override
    public boolean isLoaded() {
        return isDisplayed(epaperTab) && isDisplayed(subscribeToEpaperBtn);
    }

    public boolean isBlockerDisplayed() {
        return isDisplayed(blockerTitle) && isDisplayed(blockerSubscribeNowBtn);
    }
}
