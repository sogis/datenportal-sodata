package ch.so.agi.datenportal.explore;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.DatenportalApplication;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.AriaRole;
import java.nio.file.Path;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@Tag("playwright")
@SpringBootTest(
        classes = {DatenportalApplication.class, ExploreIslandParquetPlaywrightTest.ExploreFixtureCatalogConfiguration.class},
        properties = "datenportal.catalog.duckdb.classpath-location=explore_fixture_catalog.duckdb",
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExploreEditorPlaywrightTest {

    @LocalServerPort
    private int port;

    @ParameterizedTest(name = "{0} editor toolbar and undo at {1}x{2}")
    @CsvSource({"chromium,1280,900", "firefox,1280,900", "chromium,1280,650",
            "firefox,1280,650", "chromium,390,844", "firefox,390,844"})
    void editorsKeepToolbarVisibleAndSupportUndoRedo(String engine, int width, int height) {
        try (Playwright playwright = Playwright.create();
                Browser browser = (engine.equals("firefox") ? playwright.firefox() : playwright.chromium())
                        .launch(new BrowserType.LaunchOptions().setHeadless(true));
                BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(width, height))) {
            // These are editor/layout tests, independent of the optional real WebR/Wasm integration test.
            context.route("**/webr/0.6.0/webr.js", route -> route.fulfill(new com.microsoft.playwright.Route.FulfillOptions()
                    .setContentType("text/javascript")
                    .setBody("""
                            export const ChannelType = {PostMessage: 1};
                            export class WebR {
                              async init() {}
                              async installPackages() {}
                              close() {}
                            }
                            """)));
            Page page = context.newPage();
            page.navigate("http://localhost:" + port + "/datasets/explore-fixture/explore");
            page.waitForSelector("[data-autocomplete-ready='true']");
            page.waitForFunction("() => !document.querySelector('.dp-explore-runtime-overlay')");

            exerciseEditor(page, false, width);
            page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("R-Labor")).click();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.getByLabel("R bearbeiten")).isEnabled();
            exerciseEditor(page, true, width);
        }
    }

    private static void exerciseEditor(Page page, boolean r, int width) {
        Locator pane = page.locator(".dp-explore-query-pane:visible");
        Locator editor = r ? page.getByLabel("R bearbeiten")
                : pane.locator(".monaco-editor");
        Locator value = r ? editor : page.locator("#dp-explore-sql-fallback");
        String initial = value.inputValue();
        pane.scrollIntoViewIfNeeded();
        editor.click();
        page.keyboard().press("ControlOrMeta+A");
        page.keyboard().press("ArrowRight");
        page.keyboard().type("x");
        assertValue(value, initial + "x");
        undo(page);
        assertValue(value, initial);
        redo(page);
        assertValue(value, initial + "x");
        undo(page);

        String longCode = (r ? "# Eine lange eingefügte R-Zeile\n" : "-- Eine lange eingefügte SQL-Zeile\n").repeat(100);
        paste(page, editor, longCode);
        assertValue(value, longCode);
        assertEditorFits(pane);
        undo(page);
        assertValue(value, initial);
        redo(page);
        assertValue(value, longCode);

        if (width > 896) {
            Locator handle = page.getByLabel(r ? "R-Editor und R-Ausgabe Grösse anpassen"
                    : "SQL-Editor und Resultattabelle Grösse anpassen");
            BoundingBox box = handle.boundingBox();
            double before = pane.boundingBox().height;
            page.mouse().move(box.x + box.width / 2, box.y + box.height / 2);
            page.mouse().down();
            page.mouse().move(box.x + box.width / 2, box.y + box.height / 2 - 100);
            page.mouse().up();
            assertThat(pane.boundingBox().height).isLessThan(before);
        }
        editor.click();
        page.keyboard().press("ControlOrMeta+A");
        page.keyboard().press("ArrowRight");
        assertEditorFits(pane);
        page.screenshot(new Page.ScreenshotOptions().setPath(Path.of("build/reports/editor-screenshots",
                page.context().browser().browserType().name() + "-" + width + "-" + page.viewportSize().height
                        + (r ? "-r.png" : "-sql.png"))));

        page.keyboard().press("ControlOrMeta+A");
        page.keyboard().type("x");
        assertValue(value, "x");
        undo(page);
        assertValue(value, longCode);
        redo(page);
        assertValue(value, "x");
        assertEditorFits(pane);

        // Trial clicks check hit testing without running code or replacing the editor value.
        pane.locator("select").first().click(new Locator.ClickOptions().setTrial(true));
        pane.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(r ? "R ausführen" : "Ausführen").setExact(true))
                .click(new Locator.ClickOptions().setTrial(true));
        pane.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(r ? "R kopieren" : "SQL kopieren").setExact(true))
                .click(new Locator.ClickOptions().setTrial(true));
    }

    private static void paste(Page page, Locator editor, String text) {
        // Use the actual browser copy/paste path: fill()/value assignment bypasses native undo history.
        page.evaluate("""
                text => {
                  const source = document.createElement('textarea');
                  source.id = 'editor-test-clipboard';
                  source.style.cssText = 'position:fixed;top:0;left:0;width:100px;height:40px;z-index:99999';
                  source.value = text;
                  document.body.append(source);
                  source.focus();
                  source.select();
                }
                """, text);
        page.keyboard().press("ControlOrMeta+C");
        page.locator("#editor-test-clipboard").evaluate("el => el.remove()");
        editor.click();
        page.keyboard().press("ControlOrMeta+A");
        page.keyboard().press("ControlOrMeta+V");
    }

    private static void undo(Page page) {
        page.keyboard().press("Escape");
        page.keyboard().press("ControlOrMeta+z");
    }

    private static void redo(Page page) {
        page.keyboard().press("ControlOrMeta+Shift+z");
    }

    private static void assertValue(Locator value, String expected) {
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(value).hasValue(expected);
    }

    private static void assertEditorFits(Locator pane) {
        // isVisible() alone also passes for content clipped by an overflow:hidden ancestor.
        assertThat((Boolean) pane.evaluate("""
                pane => {
                  const bounds = pane.getBoundingClientRect();
                  const header = pane.querySelector('.dp-explore-query-pane__header').getBoundingClientRect();
                  const editor = pane.querySelector('.dp-explore-editor').getBoundingClientRect();
                  return pane.scrollTop === 0 && header.top >= bounds.top - 1
                    && editor.top >= header.bottom - 1 && editor.bottom <= bounds.bottom + 1
                    && editor.height > 0;
                }
                """)).as("toolbar stays in the panel; only the editor content scrolls").isTrue();
    }
}
