package ch.so.agi.datenportal.explore;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@Tag("playwright")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExploreIslandPlaywrightTest {

    @LocalServerPort
    private int port;

    private Playwright playwright;
    private Browser browser;

    @BeforeAll
    void setUpBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    void tearDownBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    void explorePageBootstrapsReactIsland() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/ch.so.bauinventar/explore"));

            page.waitForSelector(".dp-explore-workbench");

            assertThat(page.locator(".dp-explore-workbench[aria-label='Erkunden SQL-Labor']").count()).isEqualTo(1);
            assertThat(page.locator(".dp-explore-workbench__topbar").count()).isZero();
            assertThat(page.locator(".dp-explore-data-panel[aria-label='Daten und Schema']").count()).isEqualTo(1);
            assertThat(page.locator(".dp-schema-explorer").count()).isEqualTo(1);
            assertThat(page.locator(".dp-schema-explorer h2:has-text('SCHEMA EXPLORER')").count()).isEqualTo(1);
            assertThat(page.locator(".dp-explore-query-pane").count()).isEqualTo(1);
            assertThat(page.locator("[aria-label='Schema und SQL-Labor Grösse anpassen']").count()).isEqualTo(1);
            assertThat(page.getByText("Bereit", new Page.GetByTextOptions().setExact(true)).count()).isZero();
            assertThat(page.locator("text=Abfrage 1").count()).isZero();
            assertThat(page.locator("text=Zur Datensatzseite").count()).isZero();
            assertThat(page.locator("button[role='tab']:has-text('Vorschau')").count()).isZero();
            assertThat(page.locator("button[role='tab']:has-text('SQL-Labor')").count()).isZero();
            assertThat(page.locator("button[role='tab']:has-text('Diagramm')").count()).isZero();
            assertThat(page.locator("button[role='tab']:has-text('Code')").count()).isZero();
            assertThat(page.locator("text=Die interaktive Erkunden-Oberfläche wird in der nächsten Phase eingebunden").count()).isZero();
        }
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
