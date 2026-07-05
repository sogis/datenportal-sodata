package ch.so.agi.datenportal.explore;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.DatenportalApplication;
import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetAttribute;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.BoundingBox;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
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
        properties = "datenportal.catalog.duckdb.classpath-location=explore_fixture_catalog.duckdb",
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

            waitForExploreReady(page, browserErrors);
            assertThat(page.locator(".dp-schema-explorer__node.is-active:has-text('ch_so_oev_haltestellen')").count()).isZero();
            openCatalogSchema(page);
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();
            waitForSqlResult(page, browserErrors);

            assertThat(page.locator(".dp-schema-explorer__node.is-active:has-text('ch_so_oev_haltestellen')").count()).isEqualTo(1);
            assertThat(page.getByText("Bereit", new Page.GetByTextOptions().setExact(true)).count()).isZero();
            assertThat(page.locator("[aria-label='SQL Ergebnis']").count()).isEqualTo(1);
            assertThat(page.locator("text=Solothurn").count()).isGreaterThanOrEqualTo(1);
            assertThat(page.locator("text=Olten").count()).isGreaterThanOrEqualTo(1);
            assertThat(page.locator(".dp-explore-chart").count()).isZero();
            assertThat(page.locator("button[role='tab']:has-text('Diagramm')").count()).isZero();
            assertThat(browserErrors).isEmpty();
        }
    }

    @Test
    void issueExplorePageRegistersSameOriginParquetAndShowsPreviewRows() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            List<String> browserErrors = collectBrowserErrors(page);
            page.navigate(baseUrl("/series/explore-series/issues/current/explore"));

            waitForExploreReady(page);
            openCatalogSchema(page);
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();
            page.waitForSelector("[aria-label='SQL Ergebnis']");

            assertThat(page.locator(".dp-schema-explorer__node.is-active:has-text('ch_so_oev_haltestellen')").count()).isEqualTo(1);
            assertThat(page.locator("[aria-label='SQL Ergebnis']").count()).isEqualTo(1);
            assertThat(page.locator("text=Solothurn").count()).isGreaterThanOrEqualTo(1);
            assertThat(page.locator("text=Olten").count()).isGreaterThanOrEqualTo(1);
            assertThat(browserErrors).isEmpty();
        }
    }

    @Test
    void schemaExplorerStartsCollapsedUsesLightTypographyAndScrollsLocally() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);

            Locator title = page.locator(".dp-schema-explorer__header h2");
            Locator schemaNode = page.locator(".dp-schema-explorer__node--schema:has-text('opendata')");
            Locator schemaName = schemaNode.locator(".dp-schema-explorer__name").first();
            assertThat(computedStyle(title, "fontWeight")).isEqualTo("400");
            assertThat(computedStyle(schemaName, "fontWeight")).isEqualTo("400");
            assertThat(computedStyle(schemaName, "fontSize")).isEqualTo("14px");
            assertThat(schemaNode.getAttribute("aria-expanded")).isEqualTo("false");
            assertThat(page.locator(".dp-schema-explorer__node.is-active").count()).isZero();

            openCatalogSchema(page);

            Locator tableName = page.locator(".dp-schema-explorer__node--table .dp-schema-explorer__name").first();
            assertThat(computedStyle(tableName, "fontSize")).isEqualTo("14px");
            assertThat(page.locator(".dp-schema-explorer__node.is-active:has-text('ch_so_oev_haltestellen')").count()).isEqualTo(1);
            assertThat(page.locator(".dp-schema-explorer__column .dp-schema-explorer__name:has-text('wert')").count()).isEqualTo(1);

            Locator tree = page.locator(".dp-schema-explorer__tree");
            tree.evaluate("""
                    el => {
                      for (let index = 0; index < 80; index += 1) {
                        const row = document.createElement('div');
                        row.className = 'dp-schema-explorer__node dp-schema-explorer__node--table';
                        row.textContent = `synthetic_table_${index}`;
                        el.appendChild(row);
                      }
                    }
                    """);

            assertThat((Boolean) tree.evaluate("el => el.scrollHeight > el.clientHeight + 1")).isTrue();
            assertThat((Boolean) tree.evaluate("""
                    el => {
                      el.scrollTop = 120;
                      return el.scrollTop > 0;
                    }
                    """)).isTrue();
            assertThat(pageLevelHorizontalOverflow(page)).isLessThanOrEqualTo(1);
        }
    }

    @Test
    void schemaExplorerActionMenuClosesAfterCopyAndOutsideClick() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            context.grantPermissions(
                    List.of("clipboard-write"),
                    new BrowserContext.GrantPermissionsOptions().setOrigin(baseUrl("")));
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);
            openCatalogSchema(page);

            Locator actionsButton = page.getByRole(
                    com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Aktionen für ch_so_oev_haltestellen"));
            actionsButton.click();
            assertThat(page.locator(".dp-schema-explorer__menu").count()).isEqualTo(1);

            page.getByRole(
                    com.microsoft.playwright.options.AriaRole.MENUITEM,
                    new Page.GetByRoleOptions().setName("SELECT kopieren")).click();
            page.waitForFunction("() => !document.querySelector('.dp-schema-explorer__menu')");

            actionsButton.click();
            assertThat(page.locator(".dp-schema-explorer__menu").count()).isEqualTo(1);
            page.locator(".dp-explore-workbench__main").click();
            page.waitForFunction("() => !document.querySelector('.dp-schema-explorer__menu')");
        }
    }

    @Test
    void compactWorkbenchSupportsKeyboardRunWithoutPrimaryTabs() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);
            assertThat(page.locator("button[role='tab']").count()).isZero();

            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).focus();
            page.keyboard().press("Enter");
            page.waitForSelector("[aria-label='SQL Ergebnis']");

            var result = page.locator("[aria-label='SQL Ergebnis']");
            assertThat(result.locator("text=gemeinde").count()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void parquetLoadingFailureShowsReadableErrorWithoutDatasetLink() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-broken-parquet/explore"));

            page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT,
                    new Page.GetByRoleOptions().setName("Erkunden Status")).waitFor();

            assertThat(page.locator(".dp-explore-runtime-overlay__card.is-error").count()).isEqualTo(1);
            assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.PROGRESSBAR,
                    new Page.GetByRoleOptions().setName("Ladevorgang")).count()).isZero();
            assertThat(computedStyle(page.locator(".dp-explore-runtime-overlay"), "backgroundColor"))
                    .isEqualTo("rgba(0, 0, 0, 0.72)");
            assertThat(computedStyle(page.locator(".dp-explore-runtime-overlay__card"), "boxShadow"))
                    .isEqualTo("none");
            assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
                    new Page.GetByRoleOptions().setName("Zur Datensatzseite")).count()).isZero();
        }
    }

    @Test
    void sqlLaboratoryRunsGeneratedRecipeAndExportsCurrentResult() throws IOException {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 900)
                .setAcceptDownloads(true))) {
            Page page = context.newPage();
            List<String> externalMonacoRequests = collectExternalMonacoRequests(page);
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);
            page.waitForSelector("[data-testid='sql-monaco-editor'] .monaco-editor");
            page.waitForSelector("[data-testid='sql-monaco-editor'] .view-line:has-text('SELECT')");
            assertThat(page.locator("[data-testid='sql-monaco-editor'] .monaco-editor").count()).isEqualTo(1);
            assertThat(page.locator("[data-testid='sql-monaco-editor'] .view-line:has-text('limit 100')").count()).isZero();
            assertThat(externalMonacoRequests).isEmpty();

            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();

            page.waitForSelector("[aria-label='SQL Ergebnis']");
            page.waitForSelector("[aria-label='SQL Ergebnis'] >> text=Solothurn");
            var result = page.locator("[aria-label='SQL Ergebnis']");
            assertThat(result.locator("text=gemeinde").count()).isGreaterThanOrEqualTo(1);
            assertThat(result.locator(".dp-explore-result-table__type").count()).isGreaterThanOrEqualTo(1);

            Download download = downloadExport(page, "CSV");
            assertThat(download.suggestedFilename()).isEqualTo("datenportal-explore-fixture-result.csv");
            assertThat(Files.readString(download.path())).contains("gemeinde").contains("Solothurn");

            Download xlsxDownload = downloadExport(page, "XLSX");
            assertThat(xlsxDownload.suggestedFilename()).isEqualTo("datenportal-explore-fixture-result.xlsx");
            assertThat(Files.size(xlsxDownload.path())).isGreaterThan(0);

            Download parquetDownload = downloadExport(page, "Parquet");
            assertThat(parquetDownload.suggestedFilename()).isEqualTo("datenportal-explore-fixture-result.parquet");
            assertThat(Files.size(parquetDownload.path())).isGreaterThan(0);

            assertThat(page.locator("button[role='tab']:has-text('Code')").count()).isZero();
        }
    }

    @Test
    void sqlEditorIsVisibleEditableAndRunsChangedQuery() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);
            page.waitForSelector("[data-testid='sql-monaco-editor'] .view-line:has-text('SELECT')");
            var editor = page.locator("[data-testid='sql-monaco-editor'] .monaco-editor");
            editor.click();
            page.keyboard().press("ControlOrMeta+A");
            page.keyboard().type("select 42 as answer;");
            page.keyboard().press("Escape");

            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();

            page.waitForSelector("[aria-label='SQL Ergebnis'] >> text=answer");
            assertThat(page.locator("[aria-label='SQL Ergebnis'] >> text=42").count()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void sqlEditorShowsSqlRoomsAutocompleteForKeywordsTablesAndColumns() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            List<String> browserErrors = collectBrowserErrors(page);
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);
            openCatalogSchema(page);
            page.waitForSelector("[data-testid='sql-monaco-editor'] .view-line:has-text('SELECT')");
            var tableName = page.locator(".dp-schema-explorer__node.is-active .dp-schema-explorer__name").first().textContent().trim();
            var firstColumn = page.locator(".dp-schema-explorer__column .dp-schema-explorer__name").first().textContent().trim();
            var editor = page.locator("[data-testid='sql-monaco-editor'] .monaco-editor");
            assertThat(page.locator(".dp-schema-explorer__column .dp-schema-explorer__name:has-text('wert')").count()).isEqualTo(1);

            editor.click();
            page.keyboard().press("ControlOrMeta+A");
            page.keyboard().type("SEL");
            waitForSuggestion(page, "SELECT", browserErrors);

            page.keyboard().press("Escape");
            page.keyboard().press("ControlOrMeta+A");
            page.keyboard().type("select * from " + tableName.substring(0, Math.min(5, tableName.length())));
            waitForSuggestion(page, tableName, browserErrors);

            page.keyboard().press("Escape");
            page.keyboard().press("ControlOrMeta+A");
            page.keyboard().type("select " + tableName + ".");
            waitForSuggestion(page, firstColumn, browserErrors);

            page.keyboard().press("Escape");
            page.keyboard().press("ControlOrMeta+A");
            page.keyboard().type("select " + tableName + ".we");
            waitForSuggestion(page, "wert", browserErrors);
        }
    }

    @Test
    void oldSavedPanelSizesDoNotHideMonacoEditor() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            context.addInitScript("""
                    () => {
                      localStorage.setItem('react-resizable-panels:datenportal.explore.explore-fixture.sql', JSON.stringify({layout: [1, 99]}));
                      localStorage.setItem('react-resizable-panels:datenportal.explore.explore-fixture.workbench', JSON.stringify({layout: [1, 99]}));
                    }
                    """);
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);
            page.waitForSelector("[data-testid='sql-monaco-editor'] .view-line:has-text('SELECT')");

            BoundingBox editorBox = requireBoundingBox(page.locator("[data-testid='sql-monaco-editor'] .monaco-editor"));
            assertThat(editorBox.height).isGreaterThan(120);
        }
    }

    @Test
    void rowLimitComboboxChangesReturnedRowCount() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);
            page.waitForSelector("[data-testid='sql-monaco-editor'] .view-line:has-text('SELECT')");
            var editor = page.locator("[data-testid='sql-monaco-editor'] .monaco-editor");
            editor.click();
            page.keyboard().press("ControlOrMeta+A");
            page.keyboard().type("select range as n from range(1000);");
            page.keyboard().press("Escape");

            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();
            page.waitForSelector("[aria-label='SQL Ergebnis']");
            page.waitForSelector(".dp-explore-result__footer:has-text('rows')");

            page.getByLabel("Anzahl zurückgelieferter Resultatzeilen").selectOption("100");
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();
            page.waitForSelector(".dp-explore-result__footer:has-text('100 rows')");
        }
    }

    @Test
    void resizeHandlesChangeWorkbenchAndResultPanelSizes() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);

            Locator schemaPanel = page.locator(".dp-explore-data-panel");
            BoundingBox schemaHandle = requireBoundingBox(page.locator("[aria-label='Schema und SQL-Labor Grösse anpassen']"));
            assertThat(schemaHandle.width).isLessThanOrEqualTo(6);
            BoundingBox schemaBefore = requireBoundingBox(schemaPanel);
            dragHandle(page, "Schema und SQL-Labor Grösse anpassen", 90, 0);
            BoundingBox schemaAfter = requireBoundingBox(schemaPanel);
            assertThat(schemaAfter.width).isGreaterThan(schemaBefore.width + 40);

            Locator queryPanel = page.locator(".dp-explore-query-pane");
            BoundingBox resultHandle = requireBoundingBox(page.locator("[aria-label='SQL-Editor und Resultattabelle Grösse anpassen']"));
            assertThat(resultHandle.height).isLessThanOrEqualTo(6);
            BoundingBox queryBefore = requireBoundingBox(queryPanel);
            dragHandle(page, "SQL-Editor und Resultattabelle Grösse anpassen", 0, 90);
            BoundingBox queryAfter = requireBoundingBox(queryPanel);
            assertThat(queryAfter.height).isGreaterThan(queryBefore.height + 40);
        }
    }

    @Test
    void structuralBordersAndSplitButtonUseSingleBorderSource() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            context.grantPermissions(
                    List.of("clipboard-write"),
                    new BrowserContext.GrantPermissionsOptions().setOrigin(baseUrl("")));
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();
            page.waitForSelector("[aria-label='SQL Ergebnis']");

            BoundingBox toolbarBox = requireBoundingBox(page.locator(".dp-explore-query-pane__toolbar-row"));
            BoundingBox runButtonBox = requireBoundingBox(page.getByRole(
                    com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Ausführen")));
            assertThat(toolbarBox.height).isGreaterThan(runButtonBox.height + 10);

            Locator copyButton = page.getByRole(
                    com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("SQL kopieren"));
            BoundingBox copyBefore = requireBoundingBox(copyButton);
            assertThat(copyBefore.width).isLessThan(134.4);
            copyButton.click();
            Locator copiedButton = page.getByRole(
                    com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("✓ SQL kopiert"));
            copiedButton.waitFor();
            BoundingBox copyAfter = requireBoundingBox(copiedButton);
            assertThat(Math.abs(copyAfter.width - copyBefore.width)).isLessThan(0.5);

            assertThat(page.locator(".dp-explore-workbench__topbar").count()).isZero();
            BoundingBox workbenchBox = requireBoundingBox(page.locator(".dp-explore-workbench"));
            BoundingBox workbenchBodyBox = requireBoundingBox(page.locator(".dp-explore-workbench__body"));
            assertThat(computedStyle(page.locator(".dp-explore-workbench"), "borderTopWidth")).isEqualTo("1px");
            assertThat(computedStyle(page.locator(".dp-explore-workbench"), "borderTopColor"))
                    .isEqualTo("rgb(226, 232, 240)");
            double workbenchBodyTopOffset = workbenchBodyBox.y - workbenchBox.y;
            assertThat(workbenchBodyTopOffset).isBetween(0.5, 1.5);
            assertThat(Math.abs((workbenchBodyBox.height + workbenchBodyTopOffset) - workbenchBox.height)).isLessThan(1.0);
            assertThat(computedStyle(page.locator(".dp-explore-query-pane__toolbar-row"), "borderBottomColor"))
                    .isEqualTo("rgb(226, 232, 240)");
            assertThat(computedStyle(page.locator(".dp-explore-result__footer"), "borderTopColor"))
                    .isEqualTo("rgb(226, 232, 240)");
            assertThat(computedStyle(page.locator(".dp-explore-result-table tbody td").first(), "borderRightColor"))
                    .isEqualTo("rgb(226, 232, 240)");
            assertThat(pseudoComputedStyle(
                    page.locator("[aria-label='Schema und SQL-Labor Grösse anpassen']"),
                    "::before",
                    "backgroundColor"))
                    .isEqualTo("rgb(226, 232, 240)");

            var exportSplit = page.locator(".dp-explore-export-split");
            var exportPrimary = page.locator(".dp-explore-export-split__primary");
            var exportToggle = page.locator(".dp-explore-export-split__toggle");
            assertThat(computedStyle(exportSplit, "borderTopWidth")).isEqualTo("1px");
            assertThat(computedStyle(exportSplit, "borderTopColor")).isEqualTo("rgb(226, 232, 240)");
            assertThat(cssPixels(computedStyle(exportPrimary, "borderTopLeftRadius"))).isGreaterThan(0.0);
            assertThat(cssPixels(computedStyle(exportPrimary, "borderBottomLeftRadius"))).isGreaterThan(0.0);
            assertThat(cssPixels(computedStyle(exportToggle, "borderTopRightRadius"))).isGreaterThan(0.0);
            assertThat(cssPixels(computedStyle(exportToggle, "borderBottomRightRadius"))).isGreaterThan(0.0);
            assertThat(computedStyle(exportPrimary, "borderRightWidth")).isEqualTo("0px");
            assertThat(computedStyle(exportToggle, "borderLeftWidth")).isEqualTo("1px");
            assertThat(computedStyle(exportToggle, "borderLeftColor")).isEqualTo("rgb(226, 232, 240)");
        }
    }

    @Test
    void resultTableStickyIndexCellsStayAboveDataCells() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();
            page.waitForSelector("[aria-label='SQL Ergebnis']");

            Number headerIndexZ = (Number) page.locator("thead .dp-explore-result-table__index")
                    .evaluate("el => Number(getComputedStyle(el).zIndex)");
            Number bodyIndexZ = (Number) page.locator("tbody .dp-explore-result-table__index").first()
                    .evaluate("el => Number(getComputedStyle(el).zIndex)");
            String firstDataZ = (String) page.locator("tbody td").first()
                    .evaluate("el => getComputedStyle(el).zIndex");

            assertThat(headerIndexZ.intValue()).isGreaterThan(bodyIndexZ.intValue());
            assertThat(bodyIndexZ.intValue()).isGreaterThan(0);
            assertThat(firstDataZ).isEqualTo("auto");
        }
    }

    @Test
    void resultTableRevealsScrollbarsOnHoverAndKeyboardFocus() throws IOException {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);
            page.waitForSelector("[data-testid='sql-monaco-editor'] .view-line:has-text('SELECT')");
            String wideTallQuery = "select range as n, range + 1 as n_01, range + 2 as n_02, "
                    + "range + 3 as n_03, range + 4 as n_04, range + 5 as n_05, "
                    + "range + 6 as n_06, range + 7 as n_07, range + 8 as n_08, "
                    + "range + 9 as n_09, range + 10 as n_10, range + 11 as n_11, "
                    + "range + 12 as n_12, range + 13 as n_13, range + 14 as n_14, "
                    + "range + 15 as n_15, range + 16 as n_16, range + 17 as n_17, "
                    + "range + 18 as n_18, range + 19 as n_19 from range(200);";
            var editor = page.locator("[data-testid='sql-monaco-editor'] .monaco-editor");
            editor.click();
            page.keyboard().press("ControlOrMeta+A");
            page.keyboard().insertText(wideTallQuery);
            page.waitForSelector("[data-testid='sql-monaco-editor'] .view-line:has-text('n_19')");
            page.keyboard().press("Escape");

            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();

            page.waitForSelector("[aria-label='SQL Ergebnis']");
            page.waitForSelector(".dp-explore-result__footer:has-text('200 rows')");
            Locator scroll = page.locator(".dp-explore-result-table__scroll");
            Locator viewport = page.getByRole(com.microsoft.playwright.options.AriaRole.REGION,
                    new Page.GetByRoleOptions().setName("SQL Ergebnistabelle"));
            page.waitForSelector(".dp-explore-result-table__thumb");

            assertThat((Boolean) viewport.evaluate("el => el.scrollWidth > el.clientWidth + 1")).isTrue();
            assertThat((Boolean) viewport.evaluate("el => el.scrollHeight > el.clientHeight + 1")).isTrue();
            assertThat((Boolean) viewport.evaluate("""
                    el => {
                      el.scrollLeft = 120;
                      el.scrollTop = 120;
                      return el.scrollLeft > 0 && el.scrollTop > 0;
                    }
                    """)).isTrue();
            viewport.evaluate("el => { el.scrollLeft = 0; el.scrollTop = 0; }");

            int hiddenThumbPixels = countScrollbarThumbPixels(scroll);
            assertThat(hiddenThumbPixels).isLessThan(40);

            scroll.hover();
            page.waitForTimeout(50);
            assertThat(countScrollbarThumbPixels(scroll)).isGreaterThan(hiddenThumbPixels + 120);

            page.mouse().move(5, 5);
            viewport.click();
            assertThat((Boolean) viewport.evaluate("el => document.activeElement === el")).isTrue();
            page.waitForTimeout(50);
            assertThat(countScrollbarThumbPixels(scroll)).isGreaterThan(hiddenThumbPixels + 120);
        }
    }

    @Test
    void resultChartViewRendersRechartsAndPieColors() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            List<String> browserErrors = collectBrowserErrors(page);
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);
            page.waitForSelector("[data-testid='sql-monaco-editor'] .view-line:has-text('SELECT')");
            var editor = page.locator("[data-testid='sql-monaco-editor'] .monaco-editor");
            editor.click();
            page.keyboard().press("ControlOrMeta+A");
            page.keyboard().insertText("""
                    select 'Solothurn' as gemeinde, 3 as anzahl
                    union all select 'Olten' as gemeinde, 2 as anzahl
                    union all select 'Grenchen' as gemeinde, 1 as anzahl;
                    """);
            page.waitForSelector("[data-testid='sql-monaco-editor'] .view-line:has-text('Grenchen')");
            page.keyboard().press("Escape");

            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();

            waitForSqlResult(page, browserErrors);
            assertThat(page.locator(".dp-explore-chart").count()).isZero();
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Diagramm")).click();
            page.waitForSelector("[aria-label='Diagramm aus Resultat']");
            page.waitForSelector("[aria-label='Diagrammsteuerung']");
            page.waitForSelector("[data-chart-type='bar']");

            var defaultBarFills = normalizedFillAttributes(page.locator("[data-chart-type='bar']"));
            assertThat(defaultBarFills).contains("#104e8b");
            assertThat(defaultBarFills).doesNotContain("#000", "#000000", "black");

            page.getByLabel("Farbe").selectOption("multi");
            var multiBarFills = normalizedFillAttributes(page.locator("[data-chart-type='bar']"));
            assertThat(multiBarFills.stream().filter(fill -> fill.startsWith("#")).distinct().count())
                    .isGreaterThan(1L);
            assertThat(multiBarFills).doesNotContain("#000", "#000000", "black");

            page.getByLabel("Typ").selectOption("pie");
            page.waitForSelector("[data-chart-type='pie']");
            assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Farben neu")).count()).isEqualTo(1);
            var colorsBefore = legendSwatchColors(page);
            assertThat(colorsBefore.size()).isGreaterThan(1);
            assertThat(new java.util.HashSet<>(colorsBefore).size()).isGreaterThan(1);

            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Farben neu")).click();
            var colorsAfter = legendSwatchColors(page);
            assertThat(colorsAfter).isNotEqualTo(colorsBefore);

            page.getByLabel("Typ").selectOption("donut");
            page.waitForSelector("[data-chart-type='donut']");
            assertThat(page.locator("button[role='tab']:has-text('Diagramm')").count()).isZero();
        }
    }

    @Test
    void resultTableScrollsLocallyInsideMobileViewport() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(390, 844))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();

            page.waitForSelector("[aria-label='SQL Ergebnis']");
            var scroll = page.locator(".dp-explore-result-table__scroll");
            scroll.scrollIntoViewIfNeeded();

            var scrollBox = scroll.boundingBox();
            assertThat(scrollBox).isNotNull();
            assertThat(scrollBox.x).isGreaterThanOrEqualTo(0);
            assertThat(scrollBox.x + scrollBox.width).isLessThanOrEqualTo(391);
            assertThat(pageLevelHorizontalOverflow(page)).isLessThanOrEqualTo(1);
        }
    }

    @Test
    void exploreLayoutDoesNotCreatePageLevelHorizontalOverflowAtCommonWidths() {
        for (int width : List.of(320, 390, 768)) {
            try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(width, 844))) {
                Page page = context.newPage();
                page.navigate(baseUrl("/datasets/explore-fixture/explore"));

                waitForExploreReady(page);
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();
                page.waitForSelector("[aria-label='SQL Ergebnis']");

                assertThat(pageLevelHorizontalOverflow(page)).as("viewport width " + width).isLessThanOrEqualTo(1);
            }
        }
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private static void waitForExploreReady(Page page) {
        waitForExploreReady(page, List.of());
    }

    private static void waitForSqlResult(Page page, List<String> browserErrors) {
        try {
            page.waitForSelector("[aria-label='SQL Ergebnis']");
        } catch (TimeoutError error) {
            String bodyText = page.locator("body").innerText(new Locator.InnerTextOptions().setTimeout(1_000));
            throw new AssertionError("SQL result did not appear. Browser errors: " + browserErrors
                    + ". Body: " + bodyText, error);
        }
    }

    private static void waitForExploreReady(Page page, List<String> browserErrors) {
        try {
            page.waitForSelector(".dp-schema-explorer__node--schema:has-text('opendata')");
            page.waitForFunction("() => !document.querySelector('.dp-explore-runtime-overlay')");
        } catch (TimeoutError error) {
            String bodyText = page.locator("body").innerText(new Locator.InnerTextOptions().setTimeout(1_000));
            String overlayText = String.join(" | ", page.locator(".dp-explore-runtime-overlay").allTextContents());
            String contextText = page.locator("#datenportal-explore-context").textContent(new Locator.TextContentOptions().setTimeout(1_000));
            throw new AssertionError("Explore did not reach ready state. Overlay: " + overlayText
                    + ". Context: " + truncate(contextText)
                    + ". Browser errors: " + browserErrors
                    + ". Body: " + bodyText, error);
        }
    }

    private static void openCatalogSchema(Page page) {
        Locator openButton = page.getByRole(
                com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("opendata ausklappen"));
        if (openButton.count() > 0) {
            openButton.click();
        }
        page.waitForSelector(".dp-schema-explorer__node.is-active");
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 4_000 ? text : text.substring(0, 4_000) + "...";
    }

    private static List<String> collectBrowserErrors(Page page) {
        List<String> errors = new ArrayList<>();
        page.onConsoleMessage(message -> collectConsoleError(message, errors));
        page.onPageError(errors::add);
        return errors;
    }

    private static List<String> collectExternalMonacoRequests(Page page) {
        List<String> requests = new ArrayList<>();
        page.onRequest(request -> collectExternalMonacoRequest(request, requests));
        return requests;
    }

    private static void collectConsoleError(ConsoleMessage message, List<String> errors) {
        if ("error".equals(message.type())) {
            errors.add(message.text());
        }
    }

    private static void collectExternalMonacoRequest(Request request, List<String> requests) {
        String url = request.url();
        if ((url.contains("cdn.jsdelivr.net") || url.contains("unpkg.com")) && url.contains("monaco-editor")) {
            requests.add(url);
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

    private static BoundingBox requireBoundingBox(Locator locator) {
        BoundingBox boundingBox = locator.boundingBox();
        assertThat(boundingBox).isNotNull();
        return boundingBox;
    }

    private static Download downloadExport(Page page, String formatLabel) {
        return page.waitForDownload(() -> {
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Exportformat auswählen")).click();
            assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.MENUITEM,
                    new Page.GetByRoleOptions().setName("CSV")).count()).isEqualTo(1);
            assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.MENUITEM,
                    new Page.GetByRoleOptions().setName("XLSX")).count()).isEqualTo(1);
            assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.MENUITEM,
                    new Page.GetByRoleOptions().setName("Parquet")).count()).isEqualTo(1);
            page.getByRole(com.microsoft.playwright.options.AriaRole.MENUITEM,
                    new Page.GetByRoleOptions().setName(formatLabel)).click();
        });
    }

    private static String computedStyle(Locator locator, String property) {
        return (String) locator.evaluate("(el, property) => getComputedStyle(el)[property]", property);
    }

    @SuppressWarnings("unchecked")
    private static List<String> legendSwatchColors(Page page) {
        return (List<String>) page.locator(".dp-explore-chart__legend-swatch")
                .evaluateAll("els => els.map(el => getComputedStyle(el).backgroundColor)");
    }

    @SuppressWarnings("unchecked")
    private static List<String> normalizedFillAttributes(Locator root) {
        return ((List<String>) root.locator("[fill]")
                .evaluateAll("els => els.map(el => el.getAttribute('fill')).filter(Boolean)"))
                .stream()
                .map(value -> value.trim().toLowerCase())
                .toList();
    }

    private static double cssPixels(String value) {
        return Double.parseDouble(value.replace("px", "").trim());
    }

    private static String pseudoComputedStyle(Locator locator, String pseudoElement, String property) {
        return (String) locator.evaluate(
                "(el, args) => getComputedStyle(el, args.pseudoElement)[args.property]",
                java.util.Map.of("pseudoElement", pseudoElement, "property", property));
    }

    private static int countScrollbarThumbPixels(Locator locator) throws IOException {
        byte[] png = locator.screenshot();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        int matches = 0;
        int edgeWidth = 18;
        int verticalStart = Math.max(0, image.getWidth() - edgeWidth);
        int horizontalStart = Math.max(0, image.getHeight() - edgeWidth);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (x < verticalStart && y < horizontalStart) {
                    continue;
                }
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xff;
                int red = (argb >>> 16) & 0xff;
                int green = (argb >>> 8) & 0xff;
                int blue = argb & 0xff;
                if (alpha > 180 && nearColor(red, green, blue, 148, 163, 184, 10)) {
                    matches++;
                }
            }
        }
        return matches;
    }

    private static boolean nearColor(int red, int green, int blue, int expectedRed, int expectedGreen, int expectedBlue, int tolerance) {
        return Math.abs(red - expectedRed) <= tolerance
                && Math.abs(green - expectedGreen) <= tolerance
                && Math.abs(blue - expectedBlue) <= tolerance;
    }

    private static void dragHandle(Page page, String ariaLabel, double deltaX, double deltaY) {
        BoundingBox handle = requireBoundingBox(page.locator("[aria-label='" + ariaLabel + "']"));
        double centerX = handle.x + handle.width / 2;
        double centerY = handle.y + handle.height / 2;
        page.mouse().move(centerX, centerY);
        page.mouse().down();
        page.mouse().move(centerX + deltaX, centerY + deltaY);
        page.mouse().up();
    }

    private static void waitForSuggestion(Page page, String label, List<String> browserErrors) {
        TimeoutError lastError = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            page.keyboard().press("Control+Space");
            try {
                page.waitForSelector(
                        ".suggest-widget .monaco-list-row:has-text('" + label + "')",
                        new Page.WaitForSelectorOptions().setTimeout(5000));
                return;
            } catch (TimeoutError error) {
                lastError = error;
                page.waitForTimeout(250);
            }
        }

        try {
            page.waitForSelector(".suggest-widget .monaco-list-row:has-text('" + label + "')");
        } catch (TimeoutError error) {
            String editorText = page.locator("[data-testid='sql-monaco-editor'] .view-lines").textContent();
            String widgetText = String.join(" | ", page.locator(".suggest-widget").allTextContents());
            throw new AssertionError("Expected Monaco suggestion '" + label + "'. Editor text: " + editorText
                    + ". Suggest widget text: " + widgetText + ". Browser errors: " + browserErrors,
                    lastError == null ? error : lastError);
        }
    }

    @TestConfiguration
    static class ExploreFixtureCatalogConfiguration {

        @Bean
        @Primary
        CatalogSnapshot exploreFixtureCatalogSnapshot() {
            return CatalogSnapshot.of(
                    new Catalog(List.of(fixtureDataset(), brokenParquetDataset()), List.of(fixtureSeries())),
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

        private static DatasetSeriesEntry fixtureSeries() {
            var office = new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
            var theme = new Theme("mobilitaet", "Mobilität");
            var currentIssue = new DatasetIssueEntry(
                    "explore-series-2026",
                    "ÖV-Haltestellen Fixture 2026",
                    "Kleine Parquet-Fixture als Serienausgabe.",
                    office,
                    office,
                    List.of(theme),
                    List.of("Parquet"),
                    LocalDate.parse("2026-06-30"),
                    AccessLevel.OPEN,
                    metadata(),
                    List.of(new DistributionLink(
                            URI.create("/series/explore-series/issues/current"),
                            URI.create("/explore-fixtures/ch.so.oev_haltestellen.parquet"),
                            DistributionFormat.PARQUET)),
                    "2026",
                    true);
            return new DatasetSeriesEntry(
                    "explore-series",
                    "ÖV-Haltestellen Serie",
                    "Kleine Parquet-Fixture für Serienausgaben.",
                    office,
                    office,
                    List.of(theme),
                    List.of("Parquet"),
                    AccessLevel.OPEN,
                    CatalogEntryMetadata.empty(),
                    List.of(currentIssue));
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
                            new DatasetAttribute("gemeinde", "VARCHAR", Optional.of("Gemeinde"), Optional.empty(), false)),
                    Optional.empty());
        }
    }
}
