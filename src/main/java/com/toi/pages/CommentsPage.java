package com.toi.pages;

import com.toi.base.BasePage;
import org.openqa.selenium.By;

/**
 * CommentsPage — article comments section.
 */
public class CommentsPage extends BasePage {

    private final By commentsTitle   = byAccessibilityId("Comments");
    private final By commentsList    = byPredicateString("type == 'XCUIElementTypeCell' AND name CONTAINS 'comment'");
    private final By addCommentField = byAccessibilityId("Add a comment...");
    private final By postButton      = byAccessibilityId("Post");
    private final By closeButton     = byAccessibilityId("Close");

    public int getCommentCount() {
        return findElements(commentsList).size();
    }

    public CommentsPage addComment(String text) {
        tap(addCommentField);
        typeText(addCommentField, text);
        tap(postButton);
        return this;
    }

    public ArticlePage closeComments() {
        tap(closeButton);
        return new ArticlePage();
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(commentsTitle);
    }
}
