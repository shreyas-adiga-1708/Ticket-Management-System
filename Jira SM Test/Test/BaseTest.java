package com.automation.jira_sm_tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import java.time.Duration;

/**
 * BaseTest — sets up ChromeDriver via WebDriverManager,
 * provides shared helpers (click, type, wait), and tears down after each test.
 */
public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    // ── Update these three constants for your Jira Cloud instance ──────────
    protected static final String BASE_URL     = "https://shreyasadiga178.atlassian.net";
    protected static final String USER_EMAIL   = "shreyasadiga178@gmail.com";
    protected static final String USER_PASSWORD = "Adiga@1782";
    // ────────────────────────────────────────────────────────────────────────

    protected static final Duration TIMEOUT = Duration.ofSeconds(20);

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");   // uncomment for CI
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait   = new WebDriverWait(driver, TIMEOUT);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ── Shared helpers ────────────────────────────────────────────────────

    protected void navigateTo(String path) {
        driver.get(BASE_URL + path);
    }

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        waitForClickable(locator).click();
    }

    protected void type(By locator, String text) {
        WebElement el = waitForVisible(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForVisible(locator).getText().trim();
    }

    protected boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected void pause(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }

    /**
     * Logs in to Jira using the Atlassian login page.
     * Reused by every test class.
     */
    protected void doLogin() {
        driver.get("https://id.atlassian.com/login");

        // Step 1 – email
        type(By.id("username"), USER_EMAIL);
        click(By.id("login-submit"));

        // Step 2 – password (Atlassian splits login into two screens)
        type(By.id("password"), USER_PASSWORD);
        click(By.id("login-submit"));

        // Wait until Jira's home page loads
        wait.until(ExpectedConditions.urlContains("atlassian.net"));
        pause(2000); // let React hydrate
    }
}
