package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.BoundingBox;
import java.util.List;
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
class CatalogFiltersPlaywrightTest {

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
    void desktopPopoverAppliesFiltersWithoutShiftingResultsAndRestoresUrlStateAfterReload() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            Locator resultsShell = page.locator("#dataset-results-shell");
            double initialTop = requireBoundingBox(resultsShell).y;

            page.click("#filter-trigger-theme");
            page.waitForSelector("#filter-popover-host [data-filter-panel]");

            double topWithPopover = requireBoundingBox(resultsShell).y;
            assertThat(Math.abs(topWithPopover - initialTop)).isLessThan(1.0d);

            page.locator("#popover-theme-Bau_und_Wohnungswesen").check();
            page.locator("#popover-theme-Geografie").check();
            page.click("#filter-popover-host button[type='submit']");

            waitForLocationSearchContains(page, "theme=Bau_und_Wohnungswesen", "theme=Geografie");
            page.waitForFunction("() => document.querySelectorAll('#active-filter-chips .dp-filter-chip').length === 2");

            assertThat(page.locator("#active-filter-chips a:has-text('Bau und Wohnungswesen')").count()).isEqualTo(1);
            assertThat(page.locator("#active-filter-chips a:has-text('Geografie')").count()).isEqualTo(1);

            page.reload();
            page.waitForLoadState();
            assertThat(page.locator("#active-filter-chips a:has-text('Bau und Wohnungswesen')").count()).isEqualTo(1);
            assertThat(page.locator("#active-filter-chips a:has-text('Geografie')").count()).isEqualTo(1);

            page.click("#active-filter-chips a:has-text('Bau und Wohnungswesen')");
            waitForLocationSearchContains(page, "theme=Geografie");
            waitForLocationSearchExcludes(page, "theme=Bau_und_Wohnungswesen");
            page.waitForFunction("() => document.querySelectorAll('#active-filter-chips .dp-filter-chip').length === 1");
        }
    }

    @Test
    void desktopChromeUsesFullWidthWhileSearchAndFiltersShareControlBand() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            BoundingBox header = requireBoundingBox(page.locator("so-header"));
            BoundingBox breadcrumb = requireBoundingBox(page.locator("so-breadcrumb"));
            BoundingBox contentContainer = requireBoundingBox(page.locator("main .dp-container").first());
            BoundingBox search = requireBoundingBox(page.locator(".dp-search"));
            BoundingBox filterToolbar = requireBoundingBox(page.locator("#filter-toolbar"));
            BoundingBox filterTrigger = requireBoundingBox(page.locator("#filter-trigger-theme"));
            BoundingBox resetButton = requireBoundingBox(page.locator(".dp-filter-reset-button"));
            BoundingBox resultControls = requireBoundingBox(page.locator("#result-controls"));
            BoundingBox resultsShell = requireBoundingBox(page.locator("#dataset-results-shell"));
            double gapAboveResults = requireGap(filterToolbar, resultControls);
            double gapBeforeTable = requireGap(resultControls, resultsShell);

            assertThat(header.width).isGreaterThan(1430d);
            assertThat(breadcrumb.width).isGreaterThan(1430d);
            assertThat(contentContainer.width).isGreaterThan(1380d);
            assertThat(search.width).isLessThan(contentContainer.width - 250d);
            assertThat(search.width).isGreaterThan(980d);
            assertThat(Math.abs(search.width - filterToolbar.width)).isLessThan(1.5d);
            assertThat(Math.abs(search.x - filterToolbar.x)).isLessThan(1.5d);
            assertThat(resultsShell.width).isGreaterThan(1380d);
            assertThat(Math.abs(resultsShell.width - contentContainer.width)).isLessThan(1.5d);
            assertThat(Math.abs(filterTrigger.y - resetButton.y)).isLessThan(1.5d);
            assertThat(gapAboveResults).isGreaterThan(gapBeforeTable);
            assertThat(Math.abs(gapAboveResults - 32d)).isLessThan(1.5d);
            assertThat(Math.abs(gapBeforeTable - 16d)).isLessThan(1.5d);
        }
    }

    @Test
    void detailPageUsesWideLayoutWithReadableDescription() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/ch.so.bauinventar"));

            BoundingBox contentContainer = requireBoundingBox(page.locator("main .dp-container").first());
            BoundingBox hero = requireBoundingBox(page.locator(".dp-detail-hero"));
            BoundingBox description = requireBoundingBox(page.locator(".dp-detail-description"));

            assertThat(contentContainer.width).isGreaterThan(1380d);
            assertThat(hero.width).isGreaterThan(1380d);
            assertThat(Math.abs(hero.width - contentContainer.width)).isLessThan(1.5d);
            assertThat(description.width).isLessThan(900d);
            assertThat(description.width).isLessThan(hero.width - 300d);
        }
    }

    @Test
    void desktopPopoverEscapeAndOutsideClickDiscardDraftStateAndReturnFocusToTrigger() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            page.focus("#filter-trigger-theme");
            page.click("#filter-trigger-theme");
            page.waitForSelector("#filter-popover-host [data-filter-panel]");
            page.locator("#popover-theme-Geografie").check();
            page.keyboard().press("Escape");

            page.waitForFunction("() => document.getElementById('filter-popover-host').hidden");
            assertThat(page.locator("#filter-trigger-theme").getAttribute("aria-expanded")).isEqualTo("false");
            assertThat(isFocused(page, "#filter-trigger-theme")).isTrue();

            page.click("#filter-trigger-theme");
            page.waitForSelector("#filter-popover-host [data-filter-panel]");
            assertThat(page.locator("#popover-theme-Geografie").isChecked()).isFalse();

            page.click("#results-summary");
            page.waitForFunction("() => document.getElementById('filter-popover-host').hidden");
            assertThat(isFocused(page, "#filter-trigger-theme")).isTrue();
        }
    }

    @Test
    void mobileFilterSheetSupportsApplyAndResetFlows() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(390, 844))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            assertThat(page.locator("#mobile-filter-button").isVisible()).isTrue();
            assertThat(page.locator(".dp-filter-toolbar__desktop").isVisible()).isFalse();

            page.click("#mobile-filter-button");
            page.waitForSelector("#mobile-filter-panel-host [data-filter-panel]");
            page.locator("#mobile-theme-Geografie").check();
            page.click("#mobile-filter-panel-host summary:has-text('Ressourcentyp')");
            page.locator("#mobile-resourceType-series").check();
            page.click("#mobile-filter-panel-host button[type='submit']");

            waitForLocationSearchContains(page, "theme=Geografie", "resourceType=series");
            page.waitForFunction("() => document.getElementById('mobile-filter-panel-host').hidden");
            assertThat(page.locator("#mobile-filter-button").textContent()).contains("(2)");
            assertThat(page.locator("#active-filter-chips a:has-text('Thema: Geografie')").count()).isEqualTo(1);
            assertThat(page.locator("#active-filter-chips a:has-text('Ressourcentyp: Datenreihe')").count()).isEqualTo(1);

            page.click("#mobile-filter-button");
            page.waitForSelector("#mobile-filter-panel-host [data-filter-panel]");
            page.click("#mobile-filter-panel-host a:has-text('Alle zurücksetzen')");

            waitForLocationSearchExcludes(page, "theme=Geografie");
            waitForLocationSearchExcludes(page, "resourceType=series");
            page.waitForFunction("() => document.getElementById('mobile-filter-panel-host').hidden");
            page.waitForFunction("() => document.getElementById('active-filter-chips').classList.contains('is-empty')");
            assertThat(page.locator("#active-filter-chips").getAttribute("class")).contains("is-empty");
        }
    }

    @Test
    void autoSearchStartsAtThreeCharactersAndClearResetsCardView() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets?view=cards"));

            Locator searchInput = page.locator("#catalog-search");
            Locator clearButton = page.locator("#catalog-search-clear");
            String initialUrl = page.url();

            assertThat(page.locator(".dp-card-grid").isVisible()).isTrue();
            assertThat(page.locator(".dp-result-card:has-text('Abstimmungsresultate')").count()).isEqualTo(1);

            searchInput.fill("Ba");
            page.waitForTimeout(450);
            assertThat(page.url()).isEqualTo(initialUrl);

            searchInput.fill("Bauinventar");
            waitForLocationSearchContains(page, "q=Bauinventar", "view=cards");
            assertThat(page.locator(".dp-result-card:has-text('Bauinventar')").count()).isEqualTo(1);
            assertThat(page.locator(".dp-result-card:has-text('Abstimmungsresultate')").count()).isEqualTo(0);

            searchInput.fill("Ba");
            waitForLocationSearchExcludes(page, "q=Bauinventar");
            assertThat(page.locator(".dp-card-grid").isVisible()).isTrue();
            assertThat(page.locator(".dp-result-card:has-text('Abstimmungsresultate')").count()).isEqualTo(1);

            searchInput.fill("Bauinventar");
            waitForLocationSearchContains(page, "q=Bauinventar", "view=cards");
            clearButton.click();

            waitForLocationSearchExcludes(page, "q=Bauinventar");
            assertThat(page.locator(".dp-card-grid").isVisible()).isTrue();
            assertThat(page.url()).contains("view=cards");
            assertThat(searchInput.inputValue()).isEmpty();
            assertThat(page.locator(".dp-result-card:has-text('Abstimmungsresultate')").count()).isEqualTo(1);
        }
    }

    @Test
    void searchFieldUsesRedUnderlineFocusInsteadOfGlobalBlueOutline() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            page.focus("#catalog-search");

            assertThat((String) page.locator("#catalog-search")
                            .evaluate("element => getComputedStyle(element).outlineColor"))
                    .isNotEqualTo("rgb(11, 95, 255)");
            assertThat((String) page.locator("#catalog-search")
                            .evaluate("element => getComputedStyle(element).outlineStyle"))
                    .isEqualTo("none");
            assertThat((String) page.locator(".dp-search")
                            .evaluate("element => getComputedStyle(element).borderBottomColor"))
                    .isEqualTo("rgb(224, 31, 38)");
        }
    }

    @Test
    void catalogTypographyUsesGlobal18pxAndLocal16pxControlOverrides() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            assertThat(fontSize(page.locator("body"))).isEqualTo("18px");
            assertThat(fontSize(page.locator("#filter-trigger-theme .dp-filter-trigger__label"))).isEqualTo("16px");
            assertThat(fontSize(page.locator("#filter-trigger-theme .dp-filter-trigger__state"))).isEqualTo("16px");
            assertThat(fontSize(page.locator(".dp-filter-reset-button"))).isEqualTo("16px");
            assertThat(fontSize(page.locator("#mobile-filter-button"))).isEqualTo("16px");
            assertThat(fontSize(page.locator(".dp-view-toggle__link").first())).isEqualTo("16px");
            assertThat(fontSize(page.locator("#results-summary"))).isEqualTo("16px");
            assertThat(fontSize(page.locator("label[for='catalog-sort']"))).isEqualTo("16px");
            assertThat(fontSize(page.locator("#catalog-sort"))).isEqualTo("16px");
            assertThat(fontSize(page.locator(".dp-sort-form .dp-button"))).isEqualTo("16px");
            assertThat(fontSize(page.locator(".dp-entry-table thead th").nth(1))).isEqualTo("18px");
            assertThat(fontSize(page.locator(".dp-entry-title").first())).isEqualTo("18px");
            assertThat(fontSize(page.locator(".dp-entry-description").first())).isEqualTo("16px");
            assertThat(fontSize(page.locator(".dp-entry-publication-date").first())).isEqualTo("18px");
            assertThat(fontSize(page.locator(".dp-entry-downloads .dp-download-link").first())).isEqualTo("18px");
            assertThat(page.locator(".dp-entry-themes").count()).isEqualTo(0);
        }
    }

    private String baseUrl(String path) {
        return "http://127.0.0.1:" + port + path;
    }

    private static BoundingBox requireBoundingBox(Locator locator) {
        BoundingBox boundingBox = locator.boundingBox();
        assertThat(boundingBox).isNotNull();
        return boundingBox;
    }

    private static double requireGap(BoundingBox upper, BoundingBox lower) {
        return lower.y - (upper.y + upper.height);
    }

    private static String fontSize(Locator locator) {
        return (String) locator.evaluate("element => getComputedStyle(element).fontSize");
    }

    private static void waitForLocationSearchContains(Page page, String... fragments) {
        page.waitForFunction(
                "expected => expected.every(fragment => window.location.search.includes(fragment))",
                List.of(fragments));
    }

    private static void waitForLocationSearchExcludes(Page page, String fragment) {
        page.waitForFunction("value => !window.location.search.includes(value)", fragment);
    }

    private static boolean isFocused(Page page, String selector) {
        return Boolean.TRUE.equals(page.evaluate(
                "value => document.activeElement === document.querySelector(value)",
                selector));
    }
}
