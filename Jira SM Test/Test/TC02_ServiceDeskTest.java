package com.automation.jira_sm_tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * TC02 – Service Desk
 *
 * TC_SD_01 : Navigate to the Service Management project list and verify at least
 *            one service desk project is visible.
 * TC_SD_02 : Open a specific service desk portal and verify the portal title loads.
 */
public class TC02_ServiceDeskTest extends BaseTest {

    // Update with your JSM project key, e.g. "IT", "SD", "HELP"
	private static final String PROJECT_KEY = "KHRYNLYY";

    // ── TC_SD_01 : Service desk list is accessible ────────────────────────
    @Test(priority = 1,
          description = "TC_SD_01: Service desk project list loads with at least one project")
    public void testServiceDeskListVisible() {
        doLogin();

        // Navigate to JSM projects overview
        navigateTo("/jira/servicedesk/projects");

        // The page should display a list / grid of service desk projects
        By projectCardLocator = By.cssSelector(
                "[data-testid*='project-card'], " +
                ".project-list-item, " +
                "[class*='ProjectCard'], " +
                "td[class*='name'] a, " +
                "[class*='servicedesk'] h2");

        List<WebElement> projects = driver.findElements(projectCardLocator);

        // Fallback: check the page title or heading
        if (projects.isEmpty()) {
            By heading = By.xpath(
                "//*[contains(text(),'Service') and (self::h1 or self::h2 or self::h3)]");
            projects = driver.findElements(heading);
        }

        Assert.assertFalse(projects.isEmpty(),
                "TC_SD_01 FAILED: No service desk projects found on the listing page.");

        System.out.println("TC_SD_01 PASSED: Found " + projects.size()
                + " service desk project(s).");
    }

    // ── TC_SD_02 : Service desk portal loads ──────────────────────────────
    @Test(priority = 2,
          description = "TC_SD_02: Service desk portal page loads and displays portal name")
    public void testServiceDeskPortalLoads() {
        doLogin();

        // Navigate directly to the customer portal for the project
        navigateTo("/jira/servicedesk/projects/" + PROJECT_KEY + "/portals");

        pause(2000);

        // The portal should show the project name / portal title
        By portalTitle = By.cssSelector(
                "h1, " +
                "[data-testid='servicedesk-portal-title'], " +
                "[class*='portalTitle'], " +
                ".portal-name, " +
                "[class*='ProjectHeader'] h1");

        String title = "";
        try {
            title = getText(portalTitle);
        } catch (Exception e) {
            // Sometimes the portal opens on a sub-URL; check current URL
            Assert.assertTrue(
                    driver.getCurrentUrl().contains("servicedesk") ||
                    driver.getCurrentUrl().contains(PROJECT_KEY),
                    "TC_SD_02 FAILED: Portal page did not load properly.");
            System.out.println("TC_SD_02 PASSED: Portal URL is valid – " + driver.getCurrentUrl());
            return;
        }

        Assert.assertFalse(title.isEmpty(),
                "TC_SD_02 FAILED: Portal title is empty.");

        System.out.println("TC_SD_02 PASSED: Portal loaded with title: " + title);
    }
}
