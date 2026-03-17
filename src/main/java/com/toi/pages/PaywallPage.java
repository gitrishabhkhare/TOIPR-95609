package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;

/**
 * PaywallPage — TOI+ subscription blocker screen.
 * Locators verified on live device: Rishabh Khare's iPhone, iOS 26.3, 2026-03-10.
 *
 * How to reach: Home → tap any TOI+ article → scroll down past free preview text.
 *
 * Screen has two layers:
 *
 * 1. INLINE BANNER (in article body, visible when scrolled to paywall):
 *    "Want to read the full story?"
 *    "Unlock this story and enjoy all TOI+ member benefits"
 *    [Subscribe Now] button
 *
 * 2. BOTTOM SHEET (pops up over the article):
 *    [X] dismiss button  ← accessibilityId: "dismiss cross dark"
 *    "Want to read the full story?"
 *    "Subscribe and unlock your access to:"
 *    • Ads-Free Experience
 *    • TOI ePaper & Digital Edition
 *    • TOI Exclusives
 *    • Times Special Podcasts
 *    "Already a Member Sign In now"  (TextView with inline link)
 *    [Sign In now] link
 *    [Anniversary Offer banner] → [Avail Offer] button  ← accessibilityId: "Button"
 */
public class PaywallPage extends BasePage {

    // ── Bottom Sheet locators (visible=true) ──────────────────────────────────
    private final By dismissBtn             = byAccessibilityId("dismiss cross dark");
    private final By bottomSheetHeading     = byAccessibilityId("Want to read the full story?");
    private final By subscribeSubheading    = byAccessibilityId("Subscribe and unlock your access to:");
    private final By adsFreeLabel           = byAccessibilityId("Ads-Free Experience");
    private final By ePaperLabel            = byAccessibilityId("TOI ePaper & Digital Edition");
    private final By exclusivesLabel        = byAccessibilityId("TOI Exclusives");
    private final By podcastsLabel          = byAccessibilityId("Times Special Podcasts");
    private final By alreadyMemberText      = byPredicateString(
            "type == 'XCUIElementTypeTextView' AND label CONTAINS[cd] 'already a member'");
    private final By signInNowLink          = byAccessibilityId("Sign In now");
    // Anniversary offer banner — name/label both = "Button" (no specific accessibility id)
    private final By offerBannerBtn         = byPredicateString(
            "type == 'XCUIElementTypeButton' AND label == 'Button'");

    // ── Inline article banner locators (visible when scrolled, before sheet pops) ──
    private final By inlineBannerHeading    = byPredicateString(
            "type == 'XCUIElementTypeStaticText' AND label == 'Want to read the full story?'");
    private final By inlineBannerSubtext    = byAccessibilityId("Unlock this story and enjoy all TOI+ member benefits");
    private final By subscribeNowBtn        = byAccessibilityId("Subscribe Now");

    // ── TOI+ article card locator (on Home/Feed screen) ───────────────────────
    // TOI+ badge on article listing is a StaticText with name='TOI' (the '+' is rendered as image)
    // Combined predicate: find a cell that contains both TOI badge and article title
    public final By toiPlusBadgeOnListing  = byPredicateString(
            "type == 'XCUIElementTypeStaticText' AND name == 'TOI'");

    // ── Actions ───────────────────────────────────────────────────────────────

    public PaywallPage dismiss() {
        log.info("Dismissing TOI+ paywall bottom sheet");
        tap(dismissBtn);
        return this;
    }

    public LoginPage tapSignInNow() {
        log.info("Paywall: tapping Sign In now");
        tap(signInNowLink);
        return new LoginPage();
    }

    public PaywallPage tapSubscribeNow() {
        log.info("Paywall: tapping Subscribe Now");
        tap(subscribeNowBtn);
        return this;
    }

    public PaywallPage tapAvailOffer() {
        log.info("Paywall: tapping Avail Offer banner");
        tap(offerBannerBtn);
        return this;
    }

    // ── Verification ──────────────────────────────────────────────────────────

    @Override
    public boolean isLoaded() {
        return isDisplayed(bottomSheetHeading) || isDisplayed(inlineBannerSubtext) || isDisplayed(subscribeNowBtn);
    }

    public boolean isBottomSheetVisible() {
        return isDisplayed(dismissBtn) && isDisplayed(bottomSheetHeading);
    }

    public boolean isSignInLinkVisible() {
        return isDisplayed(signInNowLink);
    }
}
