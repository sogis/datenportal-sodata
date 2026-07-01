package ch.so.agi.datenportal.explore;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.DatenportalApplication;
import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetAttribute;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@Tag("playwright")
@SpringBootTest(
        classes = {DatenportalApplication.class, ExploreIslandParquetPlaywrightTest.ExploreFixtureCatalogConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExploreIslandParquetPlaywrightTest {

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
    void explorePageRegistersSameOriginParquetAndShowsPreviewRows() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            List<String> browserErrors = collectBrowserErrors(page);
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            page.waitForSelector(".dp-explore-status--ready");
            page.waitForSelector("[aria-label='Tabellenvorschau']");

            assertThat(page.locator("text=Registriert").count()).isEqualTo(1);
            assertThat(page.locator("[aria-label='Tabellenvorschau']").count()).isEqualTo(1);
            assertThat(page.locator("text=Solothurn").count()).isGreaterThanOrEqualTo(1);
            assertThat(page.locator("text=Olten").count()).isGreaterThanOrEqualTo(1);
            assertThat(browserErrors).isEmpty();
        }
    }

    @Test
    void exploreTabsSupportKeyboardNavigation() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            page.waitForSelector(".dp-explore-status--ready");
            page.getByRole(com.microsoft.playwright.options.AriaRole.TAB, new Page.GetByRoleOptions().setName("Vorschau")).focus();
            page.keyboard().press("ArrowRight");

            assertThat(page.locator("button[role='tab'][aria-selected='true']:has-text('SQL-Labor')").count()).isEqualTo(1);
            assertThat(page.locator("[role='tabpanel'] [aria-label='Beispielabfragen']").count()).isEqualTo(1);

            page.keyboard().press("End");
            assertThat(page.locator("button[role='tab'][aria-selected='true']:has-text('Code')").count()).isEqualTo(1);

            page.keyboard().press("Home");
            assertThat(page.locator("button[role='tab'][aria-selected='true']:has-text('Vorschau')").count()).isEqualTo(1);
        }
    }

    @Test
    void parquetLoadingFailureShowsReadableErrorAndKeepsDatasetLink() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-broken-parquet/explore"));

            page.waitForSelector(".dp-explore-status--error");

            assertThat(page.locator(".dp-explore-runtime-error").count()).isGreaterThanOrEqualTo(1);
            assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
                    new Page.GetByRoleOptions().setName("Zur Datensatzseite")).getAttribute("href"))
                    .isEqualTo("/datasets/explore-broken-parquet");
        }
    }

    @Test
    void sqlLaboratoryRunsGeneratedRecipeAndExportsCsv() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 900)
                .setAcceptDownloads(true))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            page.waitForSelector(".dp-explore-status--ready");
            page.getByRole(com.microsoft.playwright.options.AriaRole.TAB, new Page.GetByRoleOptions().setName("SQL-Labor")).click();
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Anzahl Datensätze")).click();
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();

            page.waitForSelector("[aria-label='SQL Ergebnis']");
            var result = page.locator("[aria-label='SQL Ergebnis']");
            assertThat(result.locator("text=anzahl").count()).isGreaterThanOrEqualTo(1);
            assertThat(result.locator("text=2").count()).isGreaterThanOrEqualTo(1);
            assertThat(page.locator("text=Lokale Historie").count()).isEqualTo(1);
            assertThat(page.locator("[aria-label='Lokale Abfragen'] >> text=select count(*) as anzahl").count()).isGreaterThanOrEqualTo(1);

            Download download = page.waitForDownload(() ->
                    page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Resultat als CSV")).click());
            assertThat(download.suggestedFilename()).isEqualTo("datenportal-explore-fixture-result.csv");

            page.getByRole(com.microsoft.playwright.options.AriaRole.TAB, new Page.GetByRoleOptions().setName("Code")).click();
            assertThat(page.locator("text=Weiterverwenden").count()).isEqualTo(1);
            assertThat(page.locator("button[role='tab']:has-text('DuckDB CLI')").count()).isEqualTo(1);
            assertThat(page.locator("button[role='tab']:has-text('Python mit DuckDB')").count()).isEqualTo(1);
            assertThat(page.locator("button[role='tab']:has-text('R mit duckdb')").count()).isEqualTo(1);
            assertThat(page.locator("text=read_parquet").count()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void sqlLaboratoryRendersChartForGroupedRecipe() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            page.waitForSelector(".dp-explore-status--ready");
            page.getByRole(com.microsoft.playwright.options.AriaRole.TAB, new Page.GetByRoleOptions().setName("SQL-Labor")).click();
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Nach gemeinde gruppieren")).click();
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();

            page.waitForSelector("[aria-label='SQL Ergebnis']");
            page.waitForSelector(".dp-explore-chart [data-chart-type='bar']");
            assertThat(page.locator(".dp-explore-chart").count()).isEqualTo(1);
            assertThat(page.locator(".dp-explore-chart svg").count()).isGreaterThanOrEqualTo(1);
            assertThat(page.locator("[aria-label='Diagrammsteuerung'] select").count()).isGreaterThanOrEqualTo(4);
        }
    }

    @Test
    void chartControlsRemainInsideMobileViewport() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(390, 844))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            page.waitForSelector(".dp-explore-status--ready");
            page.getByRole(com.microsoft.playwright.options.AriaRole.TAB, new Page.GetByRoleOptions().setName("SQL-Labor")).click();
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Nach gemeinde gruppieren")).click();
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();

            page.waitForSelector("[aria-label='SQL Ergebnis']");
            page.waitForSelector(".dp-explore-chart [data-chart-type='bar']");
            var chart = page.locator(".dp-explore-chart");
            chart.scrollIntoViewIfNeeded();

            var chartBox = chart.boundingBox();
            var controlsBox = page.locator("[aria-label='Diagrammsteuerung']").boundingBox();
            assertThat(chartBox).isNotNull();
            assertThat(controlsBox).isNotNull();
            assertThat(chartBox.x).isGreaterThanOrEqualTo(0);
            assertThat(chartBox.x + chartBox.width).isLessThanOrEqualTo(391);
            assertThat(controlsBox.x).isGreaterThanOrEqualTo(0);
            assertThat(controlsBox.x + controlsBox.width).isLessThanOrEqualTo(391);
        }
    }

    @Test
    void exploreLayoutDoesNotCreatePageLevelHorizontalOverflowAtCommonWidths() {
        for (int width : List.of(320, 390, 768)) {
            try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(width, 844))) {
                Page page = context.newPage();
                page.navigate(baseUrl("/datasets/explore-fixture/explore"));

                page.waitForSelector(".dp-explore-status--ready");
                page.getByRole(com.microsoft.playwright.options.AriaRole.TAB, new Page.GetByRoleOptions().setName("SQL-Labor")).click();
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Nach gemeinde gruppieren")).click();
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();
                page.waitForSelector("[aria-label='SQL Ergebnis']");
                page.waitForSelector(".dp-explore-chart [data-chart-type='bar']");

                assertThat(pageLevelHorizontalOverflow(page)).as("viewport width " + width).isLessThanOrEqualTo(1);
            }
        }
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private static List<String> collectBrowserErrors(Page page) {
        List<String> errors = new ArrayList<>();
        page.onConsoleMessage(message -> collectConsoleError(message, errors));
        page.onPageError(errors::add);
        return errors;
    }

    private static void collectConsoleError(ConsoleMessage message, List<String> errors) {
        if ("error".equals(message.type())) {
            errors.add(message.text());
        }
    }

    private static int pageLevelHorizontalOverflow(Page page) {
        Number overflow = (Number) page.evaluate("""
                () => Math.max(
                  document.documentElement.scrollWidth,
                  document.body.scrollWidth
                ) - window.innerWidth
                """);
        return overflow.intValue();
    }

    @TestConfiguration
    static class ExploreFixtureCatalogConfiguration {

        @Bean
        @Primary
        CatalogSnapshot exploreFixtureCatalogSnapshot() {
            return CatalogSnapshot.of(
                    new Catalog(List.of(fixtureDataset(), brokenParquetDataset()), List.of()),
                    Instant.parse("2026-07-01T08:00:00Z"),
                    "explore-parquet-fixture");
        }

        private static DatasetEntry fixtureDataset() {
            var office = new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
            var theme = new Theme("mobilitaet", "Mobilität");
            return new DatasetEntry(
                    "explore-fixture",
                    "ÖV-Haltestellen Fixture",
                    "Kleine Parquet-Fixture für DuckDB-Wasm.",
                    office,
                    office,
                    List.of(theme),
                    List.of("Parquet"),
                    LocalDate.parse("2026-06-30"),
                    AccessLevel.OPEN,
                    metadata(),
                    List.of(new DistributionLink(
                            URI.create("/datasets/explore-fixture"),
                            URI.create("/explore-fixtures/ch.so.oev_haltestellen.parquet"),
                            DistributionFormat.PARQUET)));
        }

        private static DatasetEntry brokenParquetDataset() {
            var office = new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
            var theme = new Theme("mobilitaet", "Mobilität");
            return new DatasetEntry(
                    "explore-broken-parquet",
                    "Defekte Parquet Fixture",
                    "Fixture mit fehlender Parquet-Datei für Fehlerzustände.",
                    office,
                    office,
                    List.of(theme),
                    List.of("Parquet"),
                    LocalDate.parse("2026-06-30"),
                    AccessLevel.OPEN,
                    metadata(),
                    List.of(new DistributionLink(
                            URI.create("/datasets/explore-broken-parquet"),
                            URI.create("/explore-fixtures/missing.parquet"),
                            DistributionFormat.PARQUET)));
        }

        private static CatalogEntryMetadata metadata() {
            return new CatalogEntryMetadata(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    List.of(
                            new DatasetAttribute("objekt_id", "VARCHAR", Optional.of("Objekt-ID"), Optional.empty(), true),
                            new DatasetAttribute("gemeinde", "VARCHAR", Optional.of("Gemeinde"), Optional.empty(), false),
                            new DatasetAttribute("wert", "DOUBLE", Optional.of("Messwert"), Optional.empty(), false)),
                    Optional.empty());
        }
    }
}
