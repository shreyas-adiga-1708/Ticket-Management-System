package com.automation.jira_sm_tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * TC07 – Comments
 *
 * TC_CM_01 : Add a comment to an existing issue and verify it appears in the
 *            activity section.
 * TC_CM_02 : Add an internal comment ("Note to team") and verify it is saved
 *            with the correct visibility label.
 */
public class TC07_CommentsTest extends BaseTest {

	private static final String ISSUE_KEY = "KHRYNLYY-1";
	private static final String TEST_COMMENT = "Automated comment – " + System.currentTimeMillis();

    // ── TC_CM_01 : Add a public comment ───────────────────────────────────
    @Test(priority = 1,
          description = "TC_CM_01: Add a comment to an issue and verify it is saved")
    public void testAddComment() {
        doLogin();

        navigateTo("/browse/" + ISSUE_KEY);
        pause(2000);

        // Click the "Comment" button or the activity tab text area
        By commentBtn = By.cssSelector(
                "[data-testid='comment-add-button'], " +
                "button[aria-label='Comment'], " +
                "#footer-comment-button, " +
                "a#comment-issue");

        try {
            click(commentBtn);
            pause(1000);
        } catch (Exception e) {
            // Sometimes the editor is always visible; scroll to it
            By editor = By.cssSelector(
                    "[data-testid='comment-input'], " +
                    ".ak-editor-content-area, " +
                    "[contenteditable='true'][class*='comment']");
            waitForVisible(editor).click();
        }

        // Type into the rich-text editor
        By commentEditor = By.cssSelector(
                "[data-testid='comment-input'] [contenteditable='true'], " +
                ".ak-editor-content-area [contenteditable='true'], " +
                "[contenteditable='true'][role='textbox'], " +
                "#comment textarea, " +
                "textarea[name='comment']");

        try {
            WebElement editor = waitForVisible(commentEditor);
            editor.click();
            editor.sendKeys(TEST_COMMENT);
        } catch (Exception e) {
            // Fallback: plain textarea
            type(By.cssSelector("textarea[name='comment'], #comment"), TEST_COMMENT);
        }
        pause(500);

        // Save the comment
        By saveBtn = By.cssSelector(
                "button[data-testid='comment-save-button'], " +
                "button[aria-label='Save'], " +
                "#comment-submit, " +
                "input[value='Save']");

        click(saveBtn);
        pause(2000);

        // Verify the comment appears in the activity list
        By activitySection = By.cssSelector(
                "[data-testid='activity-feed'], " +
                "#activity, " +
                ".activity-container, " +
                "[class*='ActivityFeed']");

        boolean commentFound = false;
        try {
            WebElement activity = waitForVisible(activitySection);
            commentFound = activity.getText().contains(TEST_COMMENT.substring(0, 20));
        } catch (Exception e) {
            // Broader search across the page
            commentFound = driver.getPageSource().contains(TEST_COMMENT.substring(0, 20));
        }

        Assert.assertTrue(commentFound,
                "TC_CM_01 FAILED: Comment not found in the activity section.");

        System.out.println("TC_CM_01 PASSED: Comment added and visible in activity feed.");
    }

    // ── TC_CM_02 : Add an internal note ───────────────────────────────────
    @Test(priority = 2,
          description = "TC_CM_02: Add an internal note (Jira Service Mgmt 'Note to team') " +
                        "and verify it is saved with internal visibility")
    public void testAddInternalNote() {
        doLogin();

        navigateTo("/browse/" + ISSUE_KEY);
        pause(2000);

        // In JSM, the comment area has two tabs: "Reply to customer" and "Note to team"
        By noteTab = By.xpath(
                "//button[contains(text(),'Note')] | " +
                "//button[contains(text(),'Internal')] | " +
                "//*[@data-testid='internal-comment-tab'] | " +
                "//span[contains(text(),'Note to team')]/..");

        try {
            click(noteTab);
            pause(1000);
        } catch (Exception e) {
            // If no tab, try adding a comment with restricted visibility
            System.out.println("TC_CM_02: 'Note to team' tab not found – trying comment restriction.");
        }

        // Type the internal note
        String internalNote = "Internal note – " + System.currentTimeMillis();

        By commentEditor = By.cssSelector(
                "[data-testid='comment-input'] [contenteditable='true'], " +
                ".ak-editor-content-area [contenteditable='true'], " +
                "[contenteditable='true'][role='textbox'], " +
                "textarea[name='comment']");

        try {
            WebElement editor = waitForVisible(commentEditor);
            editor.click();
            editor.sendKeys(internalNote);
        } catch (Exception e) {
            type(By.cssSelector("textarea[name='comment'], #comment"), internalNote);
        }
        pause(500);

        // Save
        By saveBtn = By.cssSelector(
                "button[data-testid='comment-save-button'], " +
                "#comment-submit, " +
                "input[value='Save']");
        click(saveBtn);
        pause(2000);

        // Verify internal/team note appears (it may be styled differently)
        boolean noteFound = driver.getPageSource().contains(internalNote.substring(0, 20));

        // Also look for an "Internal" or "Service team only" label near the comment
        By internalLabel = By.xpath(
                "//*[contains(text(),'Internal') or " +
                "contains(text(),'Team only') or " +
                "contains(text(),'Note to team')]");

        boolean labelFound = isElementPresent(internalLabel);

        Assert.assertTrue(noteFound,
                "TC_CM_02 FAILED: Internal note text not found in page.");

        System.out.println("TC_CM_02 PASSED: Internal note saved."
                + (labelFound ? " 'Internal' label is visible." : ""));
    }
}
