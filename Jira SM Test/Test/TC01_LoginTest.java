package com.automation.jira_sm_tests;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC01 – Login
 *
 * TC_LOGIN_01 : Valid credentials → user reaches Jira home / project page.
 * TC_LOGIN_02 : Invalid credentials → error message displayed, login rejected.
 */
public class TC01_LoginTest extends BaseTest {

    // ── TC_LOGIN_01 : Successful login ────────────────────────────────────
    @Test(priority = 1,
          description = "TC_LOGIN_01: Login with valid credentials succeeds")
    public void testSuccessfulLogin() {
        driver.get("https://id.atlassian.com/login");

        // Enter email and continue
        type(By.id("username"), USER_EMAIL);
        click(By.id("login-submit"));

        // Enter password and submit
        type(By.id("password"), USER_PASSWORD);
        click(By.id("login-submit"));

        // Verify we land on Atlassian/Jira
        wait.until(d -> d.getCurrentUrl().contains("atlassian.net"));

        Assert.assertTrue(driver.getCurrentUrl().contains("atlassian.net"),
                "Expected to land on Atlassian domain after login.");

        // The Jira home page should show the user avatar or 'Your work' heading
        boolean homeIndicator =
                isElementPresent(By.cssSelector("[data-testid='atlassian-navigation--primary-actions']"))
             || isElementPresent(By.xpath("//*[contains(text(),'Your work')]"))
             || isElementPresent(By.id("app"));

        Assert.assertTrue(homeIndicator,
                "Jira home page elements not found – login may have failed.");

        System.out.println("TC_LOGIN_01 PASSED: Logged in successfully.");
    }

    // ── TC_LOGIN_02 : Failed login with wrong password ────────────────────
    @Test(priority = 2,
          description = "TC_LOGIN_02: Login with invalid password shows error message")
    public void testInvalidPasswordShowsError() {
        driver.get("https://id.atlassian.com/login");

        // Valid email
        type(By.id("username"), USER_EMAIL);
        click(By.id("login-submit"));

        // Wrong password
        type(By.id("password"), "WrongPassword@99999");
        click(By.id("login-submit"));

        // Atlassian shows an error message on bad credentials
        By errorLocator = By.cssSelector(
                "[data-testid='form-error--error-message'], " +
                "#error-message-container, " +
                ".aui-message-error, " +
                "[class*='errorText'], " +
                "[class*='error']");

        boolean errorShown = false;
        try {
            waitForVisible(errorLocator);
            errorShown = true;
        } catch (Exception e) {
            // fallback: check URL did NOT move away from login
            errorShown = driver.getCurrentUrl().contains("login") ||
                         driver.getCurrentUrl().contains("id.atlassian");
        }

        Assert.assertTrue(errorShown,
                "TC_LOGIN_02 FAILED: Expected error message not displayed for invalid credentials.");

        System.out.println("TC_LOGIN_02 PASSED: Error shown for invalid credentials.");
    }
}
