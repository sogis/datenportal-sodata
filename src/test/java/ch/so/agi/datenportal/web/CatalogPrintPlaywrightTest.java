package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Media;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@Tag("playwright")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CatalogPrintPlaywrightTest {

    private static final Path PREVIEWS = Path.of("build", "print-previews");
    private static final String DATASET = "/datasets/ch.so.oev_haltestellen";
    private static final String SERIES = "/series/ch.so.gemeindegrenzen";
    private static final String CURRENT = SERIES + "/issues/current";
    private static final String HISTORICAL = SERIES + "/issues/ch.so.gemeindegrenzen_2025";

    @LocalServerPort
    private int port;

    private Playwright playwright;
    private Browser chromium;
    private Browser firefox;

    @BeforeAll
    void setUp() throws IOException {
        Files.createDirectories(PREVIEWS);
        playwright = Playwright.create();
        chromium = playwright.chromium().launch();
        firefox = playwright.firefox().launch();
    }

    @AfterAll
    void tearDown() {
        if (playwright != null) {
            playwright.close();
        }
    }

    static Stream<Arguments> pages() {
        var cases = List.of(
                new PrintCase("catalog-list", "/", 200),
                new PrintCase("catalog-cards", "/datasets?view=cards", 200),
                new PrintCase("catalog-filtered", "/datasets?q=Gemeinde&theme=Geografie", 200),
                new PrintCase("catalog-expanded", "/datasets?q=Gemeindegrenzen&expanded=ch.so.gemeindegrenzen", 200),
                new PrintCase("catalog-empty", "/datasets?q=absolutelymissingdataset", 200),
                new PrintCase("dataset", DATASET, 200),
                new PrintCase("dataset-structure", DATASET + "/structure-quality-origin", 200),
                new PrintCase("dataset-usage", DATASET + "/usage", 200),
                new PrintCase("series", SERIES, 200),
                new PrintCase("issue-current", CURRENT, 200),
                new PrintCase("issue-current-structure", CURRENT + "/structure-quality-origin", 200),
                new PrintCase("issue-current-usage", CURRENT + "/usage", 200),
                new PrintCase("issue-historical", HISTORICAL, 200),
                new PrintCase("issue-historical-structure", HISTORICAL + "/structure-quality-origin", 200),
                new PrintCase("issue-historical-usage", HISTORICAL + "/usage", 200),
                new PrintCase("restricted", "/datasets/ch.2581.baumkataster", 200),
                new PrintCase("not-found", "/datasets/does-not-exist", 404),
                new PrintCase("error", "/error", 500));
        return cases.stream().flatMap(testCase -> Stream.of("chromium", "firefox")
                .map(engine -> Arguments.of(engine, testCase)));
    }

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("pages")
    void publicPagesFitThePrintableWidthAndKeepTheirContext(String engine, PrintCase testCase) {
        try (BrowserContext context = context(engine)) {
            Page page = context.newPage();
            assertThat(page.navigate(url(testCase.path())).status()).isEqualTo(testCase.status());
            page.evaluate("() => document.fonts.ready");

            assertThat(page.locator(".dp-print-header").isVisible()).isFalse();
            page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));

            assertThat(page.locator(".dp-print-header").isVisible()).isTrue();
            assertThat(page.locator(".dp-print-header").innerText()).contains("Datenportal", "Daten und Statistiken");
            assertThat(page.locator("h1").isVisible()).isTrue();
            assertThat(page.locator("so-header").isVisible()).isFalse();
            assertThat(page.locator("so-breadcrumb").isVisible()).isFalse();
            assertContentFits(page);

            if (engine.equals("chromium")) {
                savePdf(page, testCase.name());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"chromium", "firefox"})
    void printQueryFollowsHtmxSearchFilterAndSortChanges(String engine) {
        try (BrowserContext context = context(engine)) {
            Page page = context.newPage();
            page.setViewportSize(1440, 1000);
            page.navigate(url("/datasets"));
            page.locator("#catalog-search").fill("Gemeinde");
            page.waitForFunction("() => document.querySelector('.dp-print-query').textContent.includes('Gemeinde')");

            page.click("#filter-trigger-theme");
            page.locator("#popover-theme-Geografie").check();
            page.click("#filter-panel-host-theme button[type='submit']");
            page.waitForFunction("""
                    () => document.querySelectorAll('#active-filter-chips .dp-filter-chip').length === 1
                      && document.querySelector('#result-controls input[name="theme"][value="Geografie"]')
                      && !document.querySelector('.htmx-request, .htmx-settling')
                    """);
            page.locator("#catalog-sort").selectOption("title-asc");
            page.waitForFunction("() => document.querySelector('.dp-print-query').textContent.includes('Titel')");

            assertThat(page.locator(".dp-print-query").isVisible()).isFalse();
            var titles = page.locator(".dp-entry-title").allTextContents();
            page.setViewportSize(680, 900);
            page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));
            assertThat(page.locator(".dp-print-query").innerText()).contains("Suchbegriff: Gemeinde", "Titel");
            assertThat(page.locator("#active-filter-chips").innerText()).contains("Geografie").doesNotContain("×", "Alle zurücksetzen");
            assertThat(page.locator("#results-summary").isVisible()).isTrue();
            assertThat(page.locator("#catalog-search-form").isVisible()).isFalse();
            assertThat(page.locator("#filter-toolbar").isVisible()).isFalse();
            assertThat(page.locator(".dp-entry-title").allTextContents()).isEqualTo(titles);
            assertContentFits(page);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"chromium", "firefox"})
    void onlyTheSelectedCodeExampleIsPrintedAndReturningToScreenRestoresControls(String engine) {
        try (BrowserContext context = context(engine)) {
            Page page = context.newPage();
            page.navigate(url(DATASET + "/usage"));
            for (String id : List.of("curl", "python", "duckdb")) {
                page.click("#usage-tab-" + id);
                assertThat(page.locator("#usage-panel-" + id + " h3").isVisible()).isFalse();
                page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));
                assertThat(page.locator(".dp-usage-code-panel:visible").count()).isEqualTo(1);
                assertThat(page.locator("#usage-panel-" + id + " h3").isVisible()).isTrue();
                assertThat(page.locator(".dp-usage-tabs").isVisible()).isFalse();
                assertThat(page.locator(".dp-usage-copy:visible, .dp-usage-copy-action:visible").count()).isZero();
                assertContentFits(page);
                if (engine.equals("chromium")) {
                    savePdf(page, "usage-" + id);
                }
                page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.SCREEN));
                assertThat(page.locator(".dp-usage-tabs").isVisible()).isTrue();
                assertThat(page.locator("#usage-panel-" + id + " .dp-usage-copy").isVisible()).isTrue();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"chromium", "firefox"})
    void printContextWorksWithoutJavascript(String engine) {
        try (BrowserContext context = (engine.equals("chromium") ? chromium : firefox).newContext(
                new Browser.NewContextOptions().setJavaScriptEnabled(false).setViewportSize(680, 900))) {
            Page page = context.newPage();
            page.navigate(url(DATASET));
            page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));
            assertThat(page.locator(".dp-print-header").innerText()).contains("Öffentlicher Verkehr", "Kanton Solothurn");
            assertThat(page.locator(".dp-site-header:visible, .dp-breadcrumb:visible").count()).isZero();
            assertContentFits(page);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"chromium", "firefox"})
    void longContentWrapsAndLaboratoryDoesNotLoadThePrintLayout(String engine) {
        try (BrowserContext context = context(engine)) {
            Page page = context.newPage();
            page.navigate(url(DATASET + "/structure-quality-origin"));
            page.locator(".dp-attribute-table tbody tr:first-child td:last-child").evaluate("""
                    element => {
                      element.textContent = 'Lange Attributbeschreibung '.repeat(100) + 'x'.repeat(180);
                      const row = element.closest('tr');
                      for (let i = 0; i < 3; i++) row.parentElement.append(row.cloneNode(true));
                    }
                    """);
            page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));
            assertContentFits(page);
            if (engine.equals("chromium")) {
                savePdf(page, "structure-long");
            }

            page.navigate(url("/datasets?q=Haltestellen"));
            page.locator(".dp-entry-description").evaluate("element => element.textContent = 'Lange Beschreibung '.repeat(150)");
            assertContentFits(page);
            assertThat(page.locator(".dp-entry-description").evaluate("element => getComputedStyle(element).overflow")).isEqualTo("visible");
            if (engine.equals("chromium")) {
                savePdf(page, "catalog-long");
            }

            page.navigate(url("/datasets?q=Haltestellen&view=cards"));
            // Cards retain the server's 50-word preview limit, including in print.
            page.locator(".dp-result-card__description").evaluate("element => element.textContent = 'Lange Beschreibung '.repeat(24) + 'Letzte Worte'");
            assertContentFits(page);
            if (engine.equals("chromium")) {
                savePdf(page, "card-long");
            }

            page.navigate(url(DATASET + "/usage"));
            page.locator(".dp-usage-code-panel:not([hidden]) code").evaluate("""
                    element => element.textContent += '\\nhttps://example.org/' + 'segment'.repeat(45)
                    """);
            assertContentFits(page);
            if (engine.equals("chromium")) {
                savePdf(page, "usage-long");
            }

            page.navigate(url(DATASET + "/explore"));
            assertThat(page.locator("link[href='/css/print.css']").count()).isZero();
            assertThat(page.locator(".dp-print-header").count()).isZero();
        }
    }

    private BrowserContext context(String engine) {
        // A4 minus two 15 mm margins: approximately 680 CSS pixels at 96 dpi.
        return (engine.equals("chromium") ? chromium : firefox).newContext(
                new Browser.NewContextOptions().setViewportSize(680, 900));
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private static void assertContentFits(Page page) {
        assertThat((Boolean) page.evaluate("() => document.documentElement.scrollWidth <= window.innerWidth + 1")).isTrue();
        assertThat((Boolean) page.locator("main table, main pre").evaluateAll("""
                elements => elements.every(element => element.scrollWidth <= element.clientWidth + 1
                  && parseFloat(getComputedStyle(element).fontSize) >= 12)
                """)).isTrue();
        assertThat(Double.parseDouble(page.locator("body").evaluate("element => getComputedStyle(element).fontSize").toString().replace("px", "")))
                .isGreaterThanOrEqualTo(14);
    }

    private static void savePdf(Page page, String name) {
        page.pdf(new Page.PdfOptions()
                .setPath(PREVIEWS.resolve(name + ".pdf"))
                .setPreferCSSPageSize(true)
                .setScale(1)
                .setPrintBackground(false)
                .setDisplayHeaderFooter(false));
    }

    record PrintCase(String name, String path, int status) {
        @Override
        public String toString() {
            return name;
        }
    }
}
