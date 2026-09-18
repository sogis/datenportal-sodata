package ch.so.agi.datenportal.explore;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.DatenportalApplication;
import ch.so.agi.datenportal.catalog.CatalogTestArtifacts;
import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.importxtf.CatalogBytes;
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
import com.microsoft.playwright.Response;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.BoundingBox;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
    void rLaboratoryLoadsWebRFromSameOriginAndReceivesSqlResult() {
        Assumptions.assumeTrue(Boolean.getBoolean("datenportal.playwright.webr"),
                "Real WebR browser runtime test is opt-in because WebR 0.6.0 can hang in Playwright Chromium during Wasm startup.");
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            List<String> browserErrors = collectBrowserErrors(page);
            List<String> externalWebRRequests = collectExternalWebRRequests(page);
            List<String> webRRequests = collectWebRRequests(page);
            List<String> webRResponses = collectWebRResponses(page);
            List<String> webRRequestFailures = collectWebRRequestFailures(page);
            List<String> webRWorkerEvents = collectWebRWorkerEvents(page);
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page, browserErrors);
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ausführen")).click();
            waitForSqlResult(page, browserErrors);
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Nach R übernehmen")).click();

            page.waitForSelector("[aria-label='WebR Status']:has-text('WebR wird geladen')",
                    new Page.WaitForSelectorOptions().setTimeout(30_000));
            waitForWebRDataFrame(page, browserErrors, webRRequests, webRResponses, webRRequestFailures,
                    webRWorkerEvents);
            assertThat(page.locator("[aria-label='Datenbasis R-Labor']:has-text('Data Frame')").count()).isEqualTo(1);
            assertThat(page.locator("[aria-label='Datenbasis R-Labor']:has-text('Zeilen')").count()).isEqualTo(1);

            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("R ausführen")).click();
            page.waitForSelector("[aria-label='R Konsole']:has-text('data.frame')",
                    new Page.WaitForSelectorOptions().setTimeout(60_000));

            assertThat(externalWebRRequests).isEmpty();
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
    void compactWorkbenchSupportsKeyboardRunWithLaboratoryTabs() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/explore-fixture/explore"));

            waitForExploreReady(page);
            assertThat(page.locator("button[role='tab']").count()).isEqualTo(2);
            assertThat(page.locator("button[role='tab']:has-text('SQL-Labor')").count()).isEqualTo(1);
            assertThat(page.locator("button[role='tab']:has-text('R-Labor')").count()).isEqualTo(1);

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

            page.waitForSelector(".dp-explore-workbench");
            page.waitForFunction("() => !document.querySelector('.dp-explore-runtime-overlay')");
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Ausführen")).click();
            page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT,
                    new Page.GetByRoleOptions().setName("Abfragefehler")).waitFor();

            assertThat(page.locator(".dp-explore-runtime-overlay__card.is-error").count()).isZero();
            assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT,
                            new Page.GetByRoleOptions().setName("Abfragefehler")).innerText())
                    .contains("Quelldatei nicht erreichbar.")
                    .contains("Technische Details");
            assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.PROGRESSBAR,
                    new Page.GetByRoleOptions().setName("Ladevorgang")).count()).isZero();
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
            assertThat(page.locator("[aria-label='SQL Resultat'] .dp-explore-result-export").count()).isEqualTo(1);
            assertThat(page.locator("[aria-label='SQL Aktionen'] .dp-explore-sql-toolbar__export").count()).isZero();

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
            page.keyboard().insertText("select 42 as answer;");
            page.keyboard().press("Escape");
            assertThat(page.locator("#dp-explore-sql-fallback").inputValue()).isEqualTo("select 42 as answer;");

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
            page.keyboard().insertText("SEL");
            waitForSuggestion(page, "SELECT", browserErrors);

            page.keyboard().press("Escape");
            page.keyboard().press("ControlOrMeta+A");
            page.keyboard().insertText("select * from " + tableName.substring(0, Math.min(5, tableName.length())));
            waitForSuggestion(page, tableName, browserErrors);

            page.keyboard().press("Escape");
            page.keyboard().press("ControlOrMeta+A");
            page.keyboard().insertText("select " + tableName + ".");
            waitForSuggestion(page, firstColumn, browserErrors);

            page.keyboard().press("Escape");
            page.keyboard().press("ControlOrMeta+A");
            page.keyboard().insertText("select " + tableName + ".we");
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
            page.keyboard().insertText("select range as n from range(1000);");
            page.keyboard().press("Escape");
            assertThat(page.locator("#dp-explore-sql-fallback").inputValue()).isEqualTo("select range as n from range(1000);");

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
    void resultChartViewRendersRechartsAndPieColors() throws IOException {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            List<String> browserErrors = collectBrowserErrors(page);
            page.navigate(baseUrl("/series/explore-series/issues/current/explore"));

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
            assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Diagramm als PNG herunterladen")).isEnabled()).isTrue();
            assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("CSV")).count()).isZero();

            Download pngDownload = page.waitForDownload(() -> page.getByRole(
                    com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Diagramm als PNG herunterladen")).click());
            assertThat(pngDownload.suggestedFilename()).isEqualTo("datenportal-explore-series-2026-diagramm.png");
            byte[] pngBytes = Files.readAllBytes(pngDownload.path());
            assertThat(pngBytes).startsWith(new byte[] { (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a });
            BufferedImage pngImage = ImageIO.read(new ByteArrayInputStream(pngBytes));
            assertThat(pngImage).isNotNull();
            assertThat(sampledRgbCount(pngImage)).as("PNG must contain rendered chart pixels").isGreaterThan(8);
            assertThat(containsRgb(pngImage, 0x104e8b)).as("PNG must contain the chart color").isTrue();
            assertThat(page.locator(".dp-explore-export-error").count()).isZero();
            assertThat(browserErrors)
                    .noneMatch(error -> error.contains("Error loading remote css")
                            || error.contains("Explore chart export failed"));

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

    @ParameterizedTest
    @ValueSource(ints = {1000, 600})
    void multipleYAttributesRenderAndExportAcrossChartTypes(int viewportWidth) throws IOException {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(viewportWidth, 800))) {
            Page page = context.newPage();
            List<String> errors = collectBrowserErrors(page);
            page.navigate(baseUrl("/series/explore-series/issues/current/explore"));
            waitForExploreReady(page, errors);
            page.waitForSelector("[data-testid='sql-monaco-editor'] .view-line:has-text('SELECT')");
            page.locator("[data-testid='sql-monaco-editor'] .monaco-editor").click();
            page.keyboard().press("ControlOrMeta+A");
            page.keyboard().insertText("""
                    select * from (values
                      (2020, 'A', 2, 4, 6), (2021, 'B', NULL, 5, 7), (2022, 'C', 4, 6, 8)
                    ) t(jahr, gemeinde, "Messung.A", "Messung B", "Messung C")
                    """);
            page.keyboard().press("Escape");
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Ausführen")).click();
            waitForSqlResult(page, errors);
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Diagramm")).click();
            page.waitForSelector("[data-chart-type='line'] .recharts-line");
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Y (Zahl)")).click();
            page.getByLabel("Messung B", new Page.GetByLabelOptions().setExact(true)).check();
            page.getByLabel("Messung C", new Page.GetByLabelOptions().setExact(true)).check();
            assertThat(page.getByLabel("Farbe").inputValue()).isEqualTo("multi");
            assertThat(page.locator(".recharts-line").count()).isEqualTo(3);
            assertThat(page.locator(".dp-explore-chart__legend--series li").allTextContents())
                    .containsExactly("Messung.A", "Messung B", "Messung C");
            page.keyboard().press("Escape");
            var before = legendSwatchColors(page);
            assertThat(new java.util.HashSet<>(before)).hasSize(3);
            page.getByLabel("Typ").selectOption("pie");
            assertThat(page.getByLabel("Wert").inputValue()).isEqualTo("Messung.A");
            page.getByLabel("Typ").selectOption("bar");
            page.waitForSelector(".recharts-bar");
            assertThat(page.locator(".recharts-bar").count()).isEqualTo(3);
            assertThat(legendSwatchColors(page)).isEqualTo(before);
            page.locator(".recharts-bar-rectangle").first().hover();
            page.waitForSelector(".recharts-tooltip-wrapper");
            String barTooltip = page.locator(".recharts-tooltip-wrapper").textContent();
            assertThat(barTooltip).contains("2020");
            int firstSeries = barTooltip.indexOf("Messung.A");
            assertThat(firstSeries).isGreaterThanOrEqualTo(0);
            assertThat(barTooltip.indexOf("Messung.A", firstSeries + "Messung.A".length())).isEqualTo(-1);
            page.getByLabel("Typ").selectOption("scatter");
            page.waitForSelector(".recharts-scatter");
            assertThat(page.locator(".recharts-scatter").count()).isEqualTo(3);
            assertThat(page.locator(".recharts-scatter-symbol").count()).isEqualTo(8);
            page.locator(".recharts-scatter-symbol").first().hover();
            page.waitForSelector(".dp-explore-series-tooltip");
            assertThat(page.locator(".dp-explore-series-tooltip").textContent()).contains("Messung.A", "jahr");
            page.getByLabel("Typ").selectOption("line");
            // Stay within the layout breakpoint: ExploreApp currently remounts the laboratory across it.
            page.setViewportSize(viewportWidth - 60, 800);
            if (viewportWidth > 896) {
                dragHandle(page, "Schema und SQL-Labor Grösse anpassen", 60, 0);
            }
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Y (Zahl)")).click();
            var popup = page.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG);
            var bounds = popup.boundingBox();
            assertThat(bounds.x).isGreaterThanOrEqualTo(0);
            assertThat(bounds.x + bounds.width).isLessThanOrEqualTo(viewportWidth - 60);
            assertThat(bounds.y + bounds.height).isLessThanOrEqualTo(800);
            assertThat(popup.evaluate("el => el.contains(document.elementFromPoint(el.getBoundingClientRect().x + 10, el.getBoundingClientRect().y + 10))"))
                    .isEqualTo(true);
            page.screenshot(new Page.ScreenshotOptions().setPath(Path.of("build/multi-series-selector-" + viewportWidth + ".png")).setFullPage(true));
            page.keyboard().press("Escape");
            assertThat(page.evaluate("document.documentElement.scrollWidth <= window.innerWidth")).isEqualTo(true);
            page.screenshot(new Page.ScreenshotOptions().setPath(Path.of("build/multi-series-chart-" + viewportWidth + ".png")).setFullPage(true));
            Download png = page.waitForDownload(() -> page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Diagramm als PNG herunterladen")).click());
            BufferedImage image = ImageIO.read(png.path().toFile());
            assertThat(image).isNotNull();
            assertThat(sampledRgbCount(image)).isGreaterThan(8);
            Files.copy(png.path(), Path.of("build/multi-series-export-" + viewportWidth + ".png"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            assertThat(page.locator(".dp-explore-export-error").count()).isZero();
            assertThat(errors).isEmpty();
        }
    }

    @Test
    void firefoxCanvasProtectionShowsHelpfulExportErrorWithoutDownload() {
        try (Browser firefoxBrowser = playwright.firefox().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setFirefoxUserPrefs(Map.of(
                        "privacy.resistFingerprinting", true,
                        "privacy.resistFingerprinting.randomDataOnCanvasExtract", true)));
                BrowserContext context = firefoxBrowser.newContext(new Browser.NewContextOptions()
                        .setViewportSize(1280, 900)
                        .setAcceptDownloads(true))) {
            Page page = context.newPage();
            List<String> browserErrors = collectBrowserErrors(page);
            prepareBarChart(page, browserErrors);

            List<Download> downloads = new ArrayList<>();
            page.onDownload(downloads::add);
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Diagramm als PNG herunterladen")).click();

            Locator exportError = page.locator(".dp-explore-export-error");
            exportError.waitFor();
            assertThat(exportError.innerText())
                    .contains("Canvas-Daten")
                    .contains("Fingerprinting-Schutz");
            assertThat(downloads).isEmpty();
            assertThat(browserErrors)
                    .noneMatch(error -> error.contains("Error loading remote css")
                            || error.contains("Explore chart export failed"));
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

    private void prepareBarChart(Page page, List<String> browserErrors) {
        page.navigate(baseUrl("/series/explore-series/issues/current/explore"));
        waitForExploreReady(page, browserErrors);
        page.waitForSelector("[data-testid='sql-monaco-editor'] .view-line:has-text('SELECT')");
        String chartSql = """
                select 'Solothurn' as gemeinde, 3 as anzahl
                union all select 'Olten' as gemeinde, 2 as anzahl
                union all select 'Grenchen' as gemeinde, 1 as anzahl;
                """;
        page.locator("#dp-explore-sql-fallback").fill(chartSql,
                new Locator.FillOptions().setForce(true));
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Ausführen")).click();
        waitForSqlResult(page, browserErrors);
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Diagramm")).click();
        page.waitForSelector("[aria-label='Diagramm aus Resultat']");
        page.waitForSelector("[aria-label='Diagrammsteuerung']");
        page.waitForSelector("[data-chart-type='bar']");
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

    private static List<String> collectExternalWebRRequests(Page page) {
        List<String> requests = new ArrayList<>();
        page.onRequest(request -> collectExternalWebRRequest(request, requests));
        return requests;
    }

    private static List<String> collectWebRRequests(Page page) {
        List<String> requests = new ArrayList<>();
        page.onRequest(request -> collectWebRRequest(request, requests));
        return requests;
    }

    private static List<String> collectWebRResponses(Page page) {
        List<String> responses = new ArrayList<>();
        page.onResponse(response -> collectWebRResponse(response, responses));
        return responses;
    }

    private static List<String> collectWebRRequestFailures(Page page) {
        List<String> failures = new ArrayList<>();
        page.onRequestFailed(request -> collectWebRRequestFailure(request, failures));
        return failures;
    }

    private static List<String> collectWebRWorkerEvents(Page page) {
        List<String> events = new ArrayList<>();
        page.onWorker(worker -> {
            if (worker.url().contains("/webr/")) {
                events.add("worker started " + worker.url());
                worker.onConsole(message -> events.add("worker console " + message.type() + " " + message.text()));
                worker.onClose(closedWorker -> events.add("worker closed " + closedWorker.url()));
            }
        });
        return events;
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

    private static void collectExternalWebRRequest(Request request, List<String> requests) {
        String url = request.url();
        if (url.contains("webr.r-wasm.org") || url.contains("repo.r-wasm.org")) {
            requests.add(url);
        }
    }

    private static void collectWebRRequest(Request request, List<String> requests) {
        String url = request.url();
        if (url.contains("/webr") || url.contains("/webr-packages")) {
            requests.add(url);
        }
    }

    private static void collectWebRResponse(Response response, List<String> responses) {
        String url = response.url();
        if (url.contains("/webr") || url.contains("/webr-packages")) {
            responses.add(response.status() + " " + url);
        }
    }

    private static void collectWebRRequestFailure(Request request, List<String> failures) {
        String url = request.url();
        if (url.contains("/webr") || url.contains("/webr-packages")) {
            failures.add(url + " " + request.failure());
        }
    }

    private static void waitForWebRDataFrame(Page page, List<String> browserErrors, List<String> webRRequests,
            List<String> webRResponses, List<String> webRRequestFailures, List<String> webRWorkerEvents) {
        try {
            page.waitForSelector("[aria-label='R Konsole']:has-text('daten ist bereit')",
                    new Page.WaitForSelectorOptions().setTimeout(180_000));
        } catch (TimeoutError error) {
            String bodyText = page.locator("body").innerText(new Locator.InnerTextOptions().setTimeout(1_000));
            String overlayText = String.join(" | ", page.locator(".dp-explore-runtime-overlay").allTextContents());
            String consoleText = String.join(" | ", page.locator("[aria-label='R Konsole']").allTextContents());
            throw new AssertionError("WebR did not receive the SQL result. Overlay: " + overlayText
                    + ". R console: " + consoleText
                    + ". WebR requests: " + webRRequests
                    + ". WebR responses: " + webRResponses
                    + ". WebR request failures: " + webRRequestFailures
                    + ". WebR worker events: " + webRWorkerEvents
                    + ". Browser errors: " + browserErrors
                    + ". Body: " + truncate(bodyText), error);
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

    private static int sampledRgbCount(BufferedImage image) {
        java.util.Set<Integer> colors = new java.util.HashSet<>();
        int xStep = Math.max(1, image.getWidth() / 100);
        int yStep = Math.max(1, image.getHeight() / 60);
        for (int y = 0; y < image.getHeight(); y += yStep) {
            for (int x = 0; x < image.getWidth(); x += xStep) {
                colors.add(image.getRGB(x, y) & 0x00ffffff);
            }
        }
        return colors.size();
    }

    private static boolean containsRgb(BufferedImage image, int expectedRgb) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) & 0x00ffffff) == expectedRgb) {
                    return true;
                }
            }
        }
        return false;
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
        try {
            page.waitForSelector("[data-testid='sql-monaco-editor'][data-autocomplete-ready='true']");
            page.keyboard().press("Control+Space");
            page.waitForSelector(
                    ".suggest-widget .monaco-list-row:has-text('" + label + "')",
                    new Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (TimeoutError error) {
            String editorText = page.locator("[data-testid='sql-monaco-editor'] .view-lines").textContent();
            String widgetText = String.join(" | ", page.locator(".suggest-widget").allTextContents());
            throw new AssertionError("Expected Monaco suggestion '" + label + "'. Editor text: " + editorText
                    + ". Suggest widget text: " + widgetText + ". Browser errors: " + browserErrors,
                    error);
        }
    }

    @TestConfiguration
    static class ExploreFixtureCatalogConfiguration {

        @Bean
        @Primary
        CatalogSnapshot exploreFixtureCatalogSnapshot() throws IOException {
            CatalogBytes duckDbCatalog;
            try {
                duckDbCatalog = new CatalogBytes(
                        Files.readAllBytes(Path.of("spec/fixtures/explore_fixture_catalog.duckdb")),
                        "file:spec/fixtures/explore_fixture_catalog.duckdb");
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to load Explore DuckDB fixture.", ex);
            }
            return CatalogSnapshot.of(
                    new Catalog(List.of(fixtureDataset(), brokenParquetDataset()), List.of(fixtureSeries())),
                    Instant.parse("2026-07-01T08:00:00Z"),
                    Duration.ZERO,
                    CatalogTestArtifacts.published("explore-parquet-fixture"),
                    duckDbCatalog,
                    ch.so.agi.datenportal.search.CatalogSearchIndex.empty());
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
