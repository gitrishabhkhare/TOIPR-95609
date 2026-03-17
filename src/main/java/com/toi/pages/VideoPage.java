package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;

/**
 * VideoPage — video news section.
 */
public class VideoPage extends BasePage {

    // Videos quick-access reached via byAccessibilityId("Videos") from home quick bar.
    private final By videoPageTitle = byAccessibilityId("Videos");
    private final By videoItems     = byPredicateString("type == 'XCUIElementTypeCell' AND visible == true");
    private final By backButton     = byAccessibilityId("new backIcon light");

    public int getVideoCount() {
        return findElements(videoItems).size();
    }

    public VideoPage playFirstVideo() {
        log.info("Playing first video");
        findElements(videoItems).get(0).click();
        return this;
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(videoPageTitle);
    }
}
