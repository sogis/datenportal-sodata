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

            page.waitForSelector(".dp-explore-island");

            assertThat(page.locator(".dp-explore-island h2:has-text('Bauinventar')").count()).isEqualTo(1);
            assertThat(page.locator("text=DuckDB wird initialisiert").count()
                    + page.locator("text=Parquet-Dateien werden registriert").count()
                    + page.locator("text=Vorschau wird geladen").count()
                    + page.locator("text=Bereit").count()
                    + page.locator("text=DuckDB-Hinweis").count()).isGreaterThanOrEqualTo(1);
            assertThat(page.locator("button[role='tab']:has-text('Vorschau')").count()).isEqualTo(1);
            assertThat(page.locator("button[role='tab']:has-text('SQL-Labor')").count()).isEqualTo(1);
            assertThat(page.locator("button[role='tab']:has-text('Diagramm')").count()).isEqualTo(1);
            assertThat(page.locator("button[role='tab']:has-text('Code')").count()).isEqualTo(1);
            assertThat(page.locator("text=Die interaktive Erkunden-Oberfläche wird in der nächsten Phase eingebunden").count()).isZero();
        }
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
