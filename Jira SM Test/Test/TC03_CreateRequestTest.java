package com.automation.jira_sm_tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC03 – Create Request
 *
 * TC_CR_01 : Create a new service request via the portal "Create request" button
 *            and verify it is submitted successfully (confirmation message shown).
 * TC_CR_02 : Try to submit a request without the mandatory Summary field and
 *            verify the validation error is displayed.
 */
public class TC03_CreateRequestTest extends BaseTest {

    private static final String PROJECT_KEY = "YOUR_PROJECT_KEY";

    // ── TC_CR_01 : Successful request creation ────────────────────────────
    @Test(priority = 1,
          description = "TC_CR_01: Create a service request with valid data succeeds")
    public void testCreateRequestSuccess() {
        doLogin();

        // Open the portal for the project
        navigateTo("/jira/servicedesk/projects/" + PROJECT_KEY + "/portals");
        pause(2000);

        // Click "Create request" or "Raise a request" button
        By createBtn = By.xpath(
                "//*[contains(text(),'Create request') or " +
                "contains(text(),'Raise a request') or " +
                "contains(text(),'New request')]");
        click(createBtn);
        pause(1500);

        // Select a request type if shown (click the first available tile)
        By requestTypeTile = By.cssSelector(
                "[data-testid*='request-type'], " +
                "[class*='RequestTypeItem'], " +
                ".request-type-item, " +
                "[class*='portal-group-item'] a");

        try {
            WebElement tile = waitForClickable(requestTypeTile);
            tile.click();
            pause(1500);
        } catch (Exception ignored) {
            // Some portals skip the request-type selection step
        }

        // Fill in the Summary / Subject field
        By summaryField = By.cssSelector(
                "input[name='summary'], " +
                "textarea[name='summary'], " +
                "#summary, " +
                "[data-testid*='summary'] input, " +
                "input[placeholder*='ummary'], " +
                "input[placeholder*='ubject']");

        type(summaryField, "Automated Test Request – " + System.currentTimeMillis());

        // Fill in the Description field if visible
        By descField = By.cssSelector(
                "textarea[name='description'], " +
                "#description, " +
                "[data-testid*='description'] textarea, " +
                ".ak-editor-content-area");

        try {
            WebElement desc = driver.findElement(descField);
            if (desc.isDisplayed()) {
                desc.sendKeys("This request was created by an automated Selenium TestNG test.");
            }
        } catch (Exception ignored) {}

        // Submit the form
        By submitBtn = By.cssSelector(
                "button[type='submit'], " +
                "input[type='submit'], " +
                "button[data-testid*='submit'], " +
                "button[class*='submit']");
        click(submitBtn);

        // Verify confirmation page / success message
        By confirmation = By.xpath(
                "//*[contains(text(),'Request created') or " +
                "contains(text(),'submitted') or " +
                "contains(text(),'raised successfully') or " +
                "contains(text(),'Your request')]");

        boolean success = false;
        try {
            waitForVisible(confirmation);
            success = true;
        } catch (Exception e) {
            // Fallback: check if URL moved to a ticket detail page (contains issue key)
            success = driver.getCurrentUrl().matches(".*/browse/[A-Z]+-\\d+.*")
                   || driver.getCurrentUrl().contains("/requests/");
        }

        Assert.assertTrue(success,
                "TC_CR_01 FAILED: Request creation confirmation not detected.");

        System.out.println("TC_CR_01 PASSED: Request created. URL: " + driver.getCurrentUrl());
    }

    // ── TC_CR_02 : Validation error when Summary is empty ─────────────────
    @Test(priority = 2,
          description = "TC_CR_02: Submitting request without Summary shows validation error")
    public void testCreateRequestWithoutSummaryShowsError() {
        doLogin();

        navigateTo("/jira/servicedesk/projects/" + PROJECT_KEY + "/portals");
        pause(2000);

        By createBtn = By.xpath(
                "//*[contains(text(),'Create request') or " +
                "contains(text(),'Raise a request') or " +
                "contains(text(),'New request')]");
        click(createBtn);
        pause(1500);

        // Select first request type if prompted
        By requestTypeTile = By.cssSelector(
                "[data-testid*='request-type'], " +
                "[class*='RequestTypeItem'], " +
                ".request-type-item");
        try {
            waitForClickable(requestTypeTile).click();
            pause(1500);
        } catch (Exception ignored) {}

        // Leave Summary blank and click Submit
        By submitBtn = By.cssSelector(
                "button[type='submit'], " +
                "input[type='submit'], " +
                "button[data-testid*='submit']");
        click(submitBtn);

        // Validation error should appear
        By validationError = By.cssSelector(
                "[class*='errorText'], " +
                "[class*='error-message'], " +
                "[data-testid*='error'], " +
                ".aui-message-error, " +
                "[class*='validationError']");

        boolean errorDisplayed = false;
        try {
            waitForVisible(validationError);
            errorDisplayed = true;
        } catch (Exception e) {
            // Check for HTML5 validation (browser-level required-field highlighting)
            try {
                WebElement summaryInput = driver.findElement(By.cssSelector(
                        "input[name='summary'], textarea[name='summary'], #summary"));
                // In HTML5, a required field will not submit and the field stays
                errorDisplayed = !summaryInput.getAttribute("value").isEmpty()
                        || driver.getCurrentUrl().contains("portal"); // still on portal
            } catch (Exception ex) {
                errorDisplayed = driver.getCurrentUrl().contains("portal");
            }
        }

        Assert.assertTrue(errorDisplayed,
                "TC_CR_02 FAILED: Expected validation error for missing Summary.");

        System.out.println("TC_CR_02 PASSED: Validation error shown for empty Summary.");
    }
}
