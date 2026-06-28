package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.BoundingBox;
import java.util.ArrayList;
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
            Locator themeTrigger = page.locator("#filter-trigger-theme");
            Locator themePanel = page.locator("#filter-panel-host-theme [data-filter-panel]");
            double initialTop = requireBoundingBox(resultsShell).y;

            page.click("#filter-trigger-theme");
            page.waitForSelector("#filter-panel-host-theme [data-filter-panel]");

            BoundingBox triggerBox = requireBoundingBox(themeTrigger);
            BoundingBox panelBox = requireBoundingBox(themePanel);
            double initialGap = panelBox.y - (triggerBox.y + triggerBox.height);

            assertThat(Math.abs(initialGap - 4.0d)).isLessThan(1.5d);
            assertThat(cssValue(themeTrigger, "border-bottom-left-radius")).isEqualTo("4px");
            assertThat(cssValue(themeTrigger, "border-bottom-right-radius")).isEqualTo("4px");
            assertThat(cssValue(themePanel, "border-top-left-radius")).isEqualTo("4px");
            assertThat(cssValue(themePanel, "border-top-right-radius")).isEqualTo("4px");

            double topWithPopover = requireBoundingBox(resultsShell).y;
            assertThat(Math.abs(topWithPopover - initialTop)).isLessThan(1.0d);

            page.evaluate("window.scrollTo(0, 200)");
            page.waitForTimeout(100);

            BoundingBox scrolledTriggerBox = requireBoundingBox(themeTrigger);
            BoundingBox scrolledPanelBox = requireBoundingBox(themePanel);
            double scrolledGap = scrolledPanelBox.y - (scrolledTriggerBox.y + scrolledTriggerBox.height);

            assertThat(Math.abs(scrolledGap - 4.0d)).isLessThan(1.5d);

            page.locator("#popover-theme-Bau_und_Wohnungswesen").check();
            page.locator("#popover-theme-Geografie").check();
            page.click("#filter-panel-host-theme button[type='submit']");

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
    void desktopPopoverKeepsActionsInsidePanelAndUsesScrollableContentAtReducedHeight() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 700))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            Locator panel = page.locator("#filter-panel-host-theme [data-filter-panel]");
            Locator options = panel.locator(".dp-filter-group__options");
            Locator actions = panel.locator(".dp-filter-popover__actions");
            Locator applyButton = panel.locator(".dp-button--primary");
            Locator resetButton = panel.locator(".dp-button--secondary");

            page.click("#filter-trigger-theme");
            page.waitForSelector("#filter-panel-host-theme [data-filter-panel]");

            BoundingBox panelBox = requireBoundingBox(panel);
            BoundingBox actionsBox = requireBoundingBox(actions);

            assertThat(actionsBox.y + actionsBox.height).isLessThanOrEqualTo(panelBox.y + panelBox.height + 1.5d);
            assertThat((Boolean) options.evaluate("element => element.scrollHeight > element.clientHeight")).isTrue();
            assertThat(fontSize(applyButton)).isEqualTo("16px");
            assertThat(fontSize(resetButton)).isEqualTo("16px");
            assertThat(cssValue(applyButton, "font-weight")).isEqualTo("400");
            assertThat(cssValue(resetButton, "font-weight")).isEqualTo("400");
        }
    }

    @Test
    void rightAlignedDesktopPopoverStaysWithinViewport() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            page.click("#filter-trigger-modified");
            page.waitForSelector("#filter-panel-host-modified [data-filter-panel]");

            BoundingBox triggerBox = requireBoundingBox(page.locator("#filter-trigger-modified"));
            BoundingBox panelBox = requireBoundingBox(page.locator("#filter-panel-host-modified [data-filter-panel]"));

            assertThat(panelBox.x + panelBox.width).isLessThanOrEqualTo(1428d);
            assertThat(Math.abs((triggerBox.x + triggerBox.width) - (panelBox.x + panelBox.width))).isLessThan(1.5d);
        }
    }

    @Test
    void desktopChromeUsesFullWidthWhileSearchAndFiltersShareControlBand() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets?theme=Geografie"));

            BoundingBox header = requireBoundingBox(page.locator("so-header"));
            BoundingBox breadcrumb = requireBoundingBox(page.locator("so-breadcrumb"));
            BoundingBox contentContainer = requireBoundingBox(page.locator("main .dp-container").first());
            BoundingBox search = requireBoundingBox(page.locator(".dp-search"));
            BoundingBox filterToolbar = requireBoundingBox(page.locator("#filter-toolbar"));
            BoundingBox activeFilters = requireBoundingBox(page.locator("#active-filter-chips"));
            BoundingBox themeTrigger = requireBoundingBox(page.locator("#filter-trigger-theme"));
            BoundingBox officeTrigger = requireBoundingBox(page.locator("#filter-trigger-office"));
            BoundingBox modifiedTrigger = requireBoundingBox(page.locator("#filter-trigger-modified"));
            BoundingBox resetButton = requireBoundingBox(page.locator(".dp-filter-reset-button"));
            BoundingBox resultControls = requireBoundingBox(page.locator("#result-controls"));
            BoundingBox viewToggle = requireBoundingBox(page.locator(".dp-view-toggle"));
            BoundingBox resultsShell = requireBoundingBox(page.locator("#dataset-results-shell"));
            double gapSearchToToolbar = requireGap(search, filterToolbar);
            double gapToolbarToActiveFilters = requireGap(filterToolbar, activeFilters);
            double gapAboveResults = requireGap(activeFilters, resultControls);
            double gapBeforeTable = requireGap(resultControls, resultsShell);

            assertThat(header.width).isGreaterThan(1430d);
            assertThat(breadcrumb.width).isGreaterThan(1430d);
            assertThat(contentContainer.width).isGreaterThan(1380d);
            assertThat(search.width).isLessThan(contentContainer.width - 250d);
            assertThat(search.width).isGreaterThan(980d);
            assertThat(Math.abs(search.width - filterToolbar.width)).isLessThan(1.5d);
            assertThat(Math.abs(search.x - filterToolbar.x)).isLessThan(1.5d);
            assertThat(Math.abs(search.width - activeFilters.width)).isLessThan(1.5d);
            assertThat(Math.abs(search.x - activeFilters.x)).isLessThan(1.5d);
            assertThat(Math.abs(gapSearchToToolbar - gapToolbarToActiveFilters)).isLessThan(1.5d);
            assertThat(resultsShell.width).isGreaterThan(1380d);
            assertThat(Math.abs(resultsShell.width - contentContainer.width)).isLessThan(1.5d);
            assertThat(Math.abs(themeTrigger.width - officeTrigger.width)).isLessThan(1.5d);
            assertThat(Math.abs(themeTrigger.width - modifiedTrigger.width)).isLessThan(1.5d);
            assertThat(resetButton.width).isLessThan(themeTrigger.width);
            assertThat(Math.abs(themeTrigger.y - resetButton.y)).isLessThan(1.5d);
            assertThat(Math.abs(horizontalCenter(resultControls) - horizontalCenter(viewToggle))).isLessThan(2.0d);
            assertThat(cssValue(page.locator(".dp-view-toggle__link--list"), "border-left-width")).isEqualTo("1px");
            assertThat(cssValue(page.locator(".dp-view-toggle__link--list"), "border-left-color"))
                    .isEqualTo("rgb(217, 224, 230)");
            assertThat(gapAboveResults).isGreaterThan(gapBeforeTable);
            assertThat(gapAboveResults).isGreaterThan(24d);
            assertThat(gapBeforeTable).isGreaterThan(8d).isLessThan(24d);
        }
    }

    @Test
    void datasetDetailUsesReadableHeroAndAlignedSummaryLayout() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/ch.so.bauinventar"));

            BoundingBox contentContainer = requireBoundingBox(page.locator("main .dp-container").first());
            BoundingBox hero = requireBoundingBox(page.locator(".dp-detail-hero"));
            BoundingBox description = requireBoundingBox(page.locator(".dp-detail-description"));
            BoundingBox summaryLayout = requireBoundingBox(page.locator(".dp-detail-summary-layout"));
            BoundingBox summaryCard = requireBoundingBox(page.locator(".dp-detail-summary-main"));
            BoundingBox featureStrip = requireBoundingBox(page.locator(".dp-detail-feature-downloads"));
            BoundingBox downloads = requireBoundingBox(page.locator(".dp-detail-feature-downloads__downloads"));
            BoundingBox actionPanel = requireBoundingBox(page.locator(".dp-detail-actions"));
            BoundingBox overviewCard = requireBoundingBox(page.locator("#metadata-overview"));
            BoundingBox temporalCoverageCard = requireBoundingBox(page.locator("#metadata-temporal-coverage"));
            BoundingBox topicsCard = requireBoundingBox(page.locator("#metadata-topics"));
            Locator availableFeatureIcon = page.locator(".dp-detail-feature--available .dp-detail-feature__icon svg").first();
            Locator actionLink = page.locator(".dp-detail-action-link").first();
            double gapDescriptionToSummary = requireGap(description, summaryLayout);

            assertThat(contentContainer.width).isGreaterThan(1380d);
            assertThat(hero.width).isGreaterThan(1380d);
            assertThat(Math.abs(hero.width - contentContainer.width)).isLessThan(1.5d);
            assertThat(description.width).isGreaterThan(700d);
            assertThat(description.width).isLessThan(1000d);
            assertThat(description.width).isLessThan(hero.width - 300d);
            assertThat(cssValue(page.locator(".dp-detail-description"), "color")).isEqualTo("rgb(47, 72, 88)");
            assertThat(page.locator(".dp-detail-facts").count()).isZero();
            assertThat(cssValue(page.locator(".dp-detail-feature-downloads"), "background-color")).isEqualTo("rgb(255, 255, 255)");
            assertThat(cssValue(page.locator(".dp-detail-feature-downloads"), "border-top-width")).isEqualTo("1px");
            assertThat(cssValue(page.locator(".dp-detail-feature-downloads"), "border-top-color")).isEqualTo("rgb(217, 224, 230)");
            assertThat(cssValue(page.locator(".dp-detail-feature-downloads"), "border-top-left-radius")).isEqualTo("4px");
            assertThat(cssValue(page.locator(".dp-detail-feature-downloads__downloads"), "border-left-width")).isEqualTo("1px");
            assertThat(fontSize(page.locator(".dp-detail-feature").first())).isEqualTo("18px");
            assertThat(cssValue(availableFeatureIcon, "width")).isEqualTo("24px");
            assertThat(cssValue(availableFeatureIcon, "height")).isEqualTo("24px");
            assertThat(cssValue(availableFeatureIcon, "color")).isEqualTo("rgb(231, 244, 231)");
            assertThat(fontSize(page.locator(".dp-detail-action-item__content h3").first())).isEqualTo("16px");
            assertThat(fontSize(page.locator(".dp-detail-action-item__content p").first())).isEqualTo("16px");
            assertThat(fontSize(actionLink)).isEqualTo("16px");
            assertThat(actionLink.textContent()).contains("→");
            assertThat(actionLink.getAttribute("href")).isEqualTo("#");
            assertThat(cssValue(actionLink, "color")).isEqualTo("rgb(210, 10, 17)");
            assertThat(cssValue(actionLink, "background-color")).isEqualTo("rgba(0, 0, 0, 0)");
            assertThat(cssValue(actionLink, "border-top-width")).isEqualTo("0px");
            assertThat(gapDescriptionToSummary).isGreaterThanOrEqualTo(32d);
            assertThat(Math.abs(summaryLayout.width - contentContainer.width)).isLessThan(1.5d);
            assertThat(Math.abs(featureStrip.y - actionPanel.y)).isLessThan(1.5d);
            assertThat(Math.abs(featureStrip.x - summaryCard.x)).isLessThan(1.5d);
            assertThat(downloads.x).isGreaterThan(featureStrip.x);
            assertThat(actionPanel.x).isGreaterThan(featureStrip.x + featureStrip.width - 1d);
            assertThat(overviewCard.y).isGreaterThan(lowerEdge(featureStrip));
            assertThat(requireGap(featureStrip, overviewCard)).isGreaterThanOrEqualTo(24d);
            assertThat(temporalCoverageCard.y).isGreaterThan(lowerEdge(overviewCard));
            assertThat(topicsCard.y).isGreaterThan(lowerEdge(temporalCoverageCard));
            assertThat(requireGap(overviewCard, temporalCoverageCard)).isGreaterThanOrEqualTo(24d);
            assertThat(requireGap(temporalCoverageCard, topicsCard)).isGreaterThanOrEqualTo(24d);
            assertThat(fontSize(page.locator("#metadata-title-overview"))).isEqualTo("18px");
            assertThat(fontSize(page.locator("#metadata-title-temporal-coverage"))).isEqualTo("18px");
            assertThat(fontSize(page.locator("#metadata-title-topics"))).isEqualTo("18px");
            assertThat(fontSize(page.locator("#metadata-overview .dp-metadata-list dt").first())).isEqualTo("14.4px");
            assertThat(fontSize(page.locator("#metadata-overview .dp-metadata-list dd").first())).isEqualTo("18px");
            assertThat(fontSize(page.locator("#metadata-temporal-coverage .dp-metadata-list dt").first())).isEqualTo("14.4px");
            assertThat(fontSize(page.locator("#metadata-temporal-coverage .dp-metadata-list dd").first())).isEqualTo("18px");
            assertThat(fontSize(page.locator("#metadata-topics .dp-metadata-list dt").first())).isEqualTo("14.4px");
            assertThat(fontSize(page.locator("#metadata-topics .dp-metadata-list dd").first())).isEqualTo("18px");
        }

        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(390, 844))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets/ch.so.bauinventar"));

            BoundingBox featureArea = requireBoundingBox(page.locator(".dp-detail-feature-downloads__features"));
            BoundingBox downloads = requireBoundingBox(page.locator(".dp-detail-feature-downloads__downloads"));
            BoundingBox featureStrip = requireBoundingBox(page.locator(".dp-detail-feature-downloads"));
            BoundingBox actionPanel = requireBoundingBox(page.locator(".dp-detail-actions"));
            BoundingBox firstAction = requireBoundingBox(page.locator(".dp-detail-action-item").nth(0));
            BoundingBox secondAction = requireBoundingBox(page.locator(".dp-detail-action-item").nth(1));

            assertThat(Math.abs(featureArea.x - downloads.x)).isLessThan(1.5d);
            assertThat(downloads.y).isGreaterThan(lowerEdge(featureArea));
            assertThat(actionPanel.y).isGreaterThan(lowerEdge(featureStrip));
            assertThat(Math.abs(firstAction.x - secondAction.x)).isLessThan(1.5d);
            assertThat(secondAction.y).isGreaterThanOrEqualTo(lowerEdge(firstAction));
        }
    }

    @Test
    void desktopPopoverEscapeAndOutsideClickDiscardDraftStateAndReturnFocusToTrigger() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            page.focus("#filter-trigger-theme");
            page.click("#filter-trigger-theme");
            page.waitForSelector("#filter-panel-host-theme [data-filter-panel]");
            page.locator("#popover-theme-Geografie").check();
            page.keyboard().press("Escape");

            page.waitForFunction("() => document.getElementById('filter-panel-host-theme').hidden");
            assertThat(page.locator("#filter-trigger-theme").getAttribute("aria-expanded")).isEqualTo("false");
            assertThat(isFocused(page, "#filter-trigger-theme")).isTrue();

            page.click("#filter-trigger-theme");
            page.waitForSelector("#filter-panel-host-theme [data-filter-panel]");
            assertThat(page.locator("#popover-theme-Geografie").isChecked()).isFalse();

            page.click(".dp-page-title");
            page.waitForFunction("() => document.getElementById('filter-panel-host-theme').hidden");
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
            assertThat(page.locator("#mobile-filter-panel-host summary:has-text('Ressourcentyp')").count()).isEqualTo(0);
            page.locator("#mobile-theme-Geografie").check();
            page.click("#mobile-filter-panel-host button[type='submit']");

            waitForLocationSearchContains(page, "theme=Geografie");
            waitForLocationSearchExcludes(page, "resourceType=");
            page.waitForFunction("() => document.getElementById('mobile-filter-panel-host').hidden");
            assertThat(page.locator("#mobile-filter-button").textContent()).contains("(1)");
            assertThat(page.locator("#active-filter-chips a:has-text('Thema: Geografie')").count()).isEqualTo(1);
            assertThat(page.locator("#active-filter-chips a:has-text('Ressourcentyp: Datenreihe')").count()).isEqualTo(0);

            page.click("#mobile-filter-button");
            page.waitForSelector("#mobile-filter-panel-host [data-filter-panel]");
            page.click("#mobile-filter-panel-host a:has-text('Alle zurücksetzen')");

            waitForLocationSearchExcludes(page, "theme=Geografie");
            waitForLocationSearchExcludes(page, "resourceType=");
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
            assertThat(cssValue(page.locator("#results-summary"), "font-weight")).isEqualTo("400");
            assertThat(fontSize(page.locator("label[for='catalog-sort']"))).isEqualTo("16px");
            assertThat(fontSize(page.locator("#catalog-sort"))).isEqualTo("16px");
            assertThat(fontSize(page.locator(".dp-entry-table thead th").nth(1))).isEqualTo("18px");
            assertThat(fontSize(page.locator(".dp-entry-title").first())).isEqualTo("18px");
            assertThat(fontSize(page.locator(".dp-entry-description").first())).isEqualTo("16px");
            assertThat(fontSize(page.locator(".dp-entry-publication-date").first())).isEqualTo("18px");
            assertThat(fontSize(page.locator(".dp-entry-downloads .dp-download-link").first())).isEqualTo("18px");
            page.click("#filter-trigger-theme");
            page.waitForSelector("#filter-panel-host-theme [data-filter-panel]");
            assertThat(fontSize(page.locator("#filter-panel-host-theme .dp-filter-option").first())).isEqualTo("14px");
            assertThat(fontSize(page.locator("#filter-panel-host-theme .dp-button--primary"))).isEqualTo("16px");
            assertThat(fontSize(page.locator("#filter-panel-host-theme .dp-button--secondary"))).isEqualTo("16px");
            assertThat(cssValue(page.locator("#filter-panel-host-theme .dp-button--primary"), "font-weight")).isEqualTo("400");
            assertThat(cssValue(page.locator("#filter-panel-host-theme .dp-button--secondary"), "font-weight")).isEqualTo("400");
            assertThat(page.locator(".dp-entry-themes").count()).isEqualTo(0);
        }

        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(390, 844))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            page.click("#mobile-filter-button");
            page.waitForSelector("#mobile-filter-panel-host [data-filter-panel]");

            assertThat(fontSize(page.locator("#mobile-filter-panel-host .dp-filter-option").first())).isEqualTo("14px");
            assertThat(fontSize(page.locator("#mobile-filter-panel-host .dp-button--primary"))).isEqualTo("16px");
            assertThat(fontSize(page.locator("#mobile-filter-panel-host .dp-button--secondary"))).isEqualTo("16px");
        }
    }

    @Test
    void uiPrimitivesUseSharedTypographyAndSemanticColorVariants() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets?view=cards&theme=Geografie"));

            Locator searchSurface = page.locator(".dp-search");
            Locator searchInput = page.locator(".dp-search__input");
            Locator filterChip = page.locator("#active-filter-chips .dp-filter-chip").first();
            Locator cardTitle = page.locator(".dp-result-card .dp-result-card__title").first();
            Locator cardDescription = page.locator(".dp-result-card .dp-result-card__description").first();
            Locator actionPill = page.locator(".dp-download-link.dp-action-pill").first();
            Locator typeBadge = page.locator(".dp-result-card .dp-type-badge").first();
            Locator openDataBadge = page.locator(".dp-result-card .dp-status-badge--positive").first();
            Locator keywordBadge = page.locator(".dp-result-card .dp-keyword-list li").first();

            String searchBackground = cssValue(searchSurface, "background-color");
            String neutralBackground = cssValue(filterChip, "background-color");
            String positiveBackground = cssValue(openDataBadge, "background-color");

            assertThat(searchBackground).isEqualTo("rgb(244, 247, 249)");
            assertThat(cssValue(searchInput, "background-color")).isEqualTo(searchBackground);
            assertThat(searchBackground).isNotEqualTo("rgb(255, 255, 255)");
            assertThat(searchBackground).isNotEqualTo(neutralBackground);

            assertThat(fontSize(filterChip)).isEqualTo("14px");
            assertThat(cssValue(filterChip, "font-weight")).isEqualTo("400");
            assertThat(cssValue(filterChip, "border-top-width")).isEqualTo("0px");
            assertThat(neutralBackground).isEqualTo("rgb(238, 242, 245)");
            assertThat(cssValue(filterChip, "background-color")).isEqualTo(cssValue(actionPill, "background-color"));

            assertThat(fontSize(actionPill)).isEqualTo("18px");
            assertThat(cssValue(actionPill, "font-weight")).isEqualTo("400");
            assertThat(cssValue(actionPill, "border-top-width")).isEqualTo("1px");
            assertThat(cssValue(actionPill, "border-top-color")).isEqualTo("rgb(217, 224, 230)");

            assertThat(fontSize(typeBadge)).isEqualTo("16px");
            assertThat(cssValue(typeBadge, "font-weight")).isEqualTo("400");
            assertThat(cssValue(typeBadge, "border-top-width")).isEqualTo("0px");

            assertThat(fontSize(cardTitle)).isEqualTo("18px");
            assertThat(fontSize(cardDescription)).isEqualTo("18px");

            assertThat(fontSize(keywordBadge)).isEqualTo("14px");
            assertThat(cssValue(keywordBadge, "font-weight")).isEqualTo("400");
            assertThat(cssValue(keywordBadge, "background-color")).isEqualTo(cssValue(typeBadge, "background-color"));
            assertThat(cssValue(keywordBadge, "padding-left")).isEqualTo("6px");
            assertThat(cssValue(keywordBadge, "padding-right")).isEqualTo("6px");
            assertThat(cssValue(keywordBadge, "min-height")).isEqualTo("25.6px");

            assertThat(fontSize(openDataBadge)).isEqualTo("16px");
            assertThat(cssValue(openDataBadge, "font-weight")).isEqualTo("400");
            assertThat(cssValue(openDataBadge, "border-top-width")).isEqualTo("0px");
            assertThat(positiveBackground).isNotEqualTo(neutralBackground);

            page.navigate(baseUrl("/datasets?view=cards&q=Baumkataster"));

            Locator warningBadge = page.locator(".dp-result-card .dp-status-badge--warning").first();
            Locator lockedCardDownload = page.locator(".dp-result-card .dp-access-lock").first();
            Locator lockedCardDownloadIcon = lockedCardDownload.locator("svg");

            assertThat(fontSize(warningBadge)).isEqualTo("16px");
            assertThat(cssValue(warningBadge, "font-weight")).isEqualTo("400");
            assertThat(cssValue(warningBadge, "border-top-width")).isEqualTo("0px");
            assertThat(cssValue(warningBadge, "background-color")).isEqualTo("rgb(254, 241, 222)");
            assertThat(page.locator(".dp-result-card .dp-download-link").count()).isZero();
            assertThat(cssValue(lockedCardDownloadIcon, "width")).isEqualTo("24px");
            assertThat(cssValue(lockedCardDownloadIcon, "height")).isEqualTo("24px");

            page.navigate(baseUrl("/series/ch.so.abstimmungsresultate"));

            Locator currentIssueBadge = page.locator(".dp-status-badge--info").first();

            assertThat(fontSize(currentIssueBadge)).isEqualTo("16px");
            assertThat(cssValue(currentIssueBadge, "font-weight")).isEqualTo("400");
            assertThat(cssValue(currentIssueBadge, "border-top-width")).isEqualTo("0px");
            assertThat(cssValue(currentIssueBadge, "background-color")).isNotEqualTo(neutralBackground);
            assertThat(cssValue(currentIssueBadge, "background-color")).isNotEqualTo(positiveBackground);

            page.navigate(baseUrl("/datasets?expanded=ch.so.abstimmungsresultate"));

            Locator issueActionPill = page.locator(".dp-issue-row .dp-download-link").first();

            assertThat(cssValue(issueActionPill, "border-top-width")).isEqualTo("1px");
            assertThat(cssValue(issueActionPill, "border-top-color")).isEqualTo("rgb(217, 224, 230)");
        }
    }

    @Test
    void cardsKeepBottomClusterAlignedWhileFlexibleSpaceStaysAboveDownloads() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1400))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets?view=cards"));

            List<Integer> rowIndexes = findCardRowWithMixedKeywordHeights(page.locator(".dp-result-card"));
            assertThat(rowIndexes).hasSizeGreaterThanOrEqualTo(2);

            Locator cards = page.locator(".dp-result-card");
            BoundingBox baselineBottom = requireBoundingBox(cards.nth(rowIndexes.getFirst()).locator(".dp-result-card__bottom"));
            BoundingBox baselineFooter = requireBoundingBox(cards.nth(rowIndexes.getFirst()).locator(".dp-result-card__footer"));

            for (int cardIndex : rowIndexes) {
                Locator card = cards.nth(cardIndex);
                BoundingBox cardBottom = requireBoundingBox(card.locator(".dp-result-card__bottom"));
                BoundingBox cardFooter = requireBoundingBox(card.locator(".dp-result-card__footer"));
                double keywordGap = requireGap(
                        requireBoundingBox(card.locator(".dp-keyword-list")),
                        requireBoundingBox(card.locator(".dp-download-list")));

                assertThat(Math.abs(lowerEdge(cardBottom) - lowerEdge(baselineBottom))).isLessThan(1.5d);
                assertThat(Math.abs(lowerEdge(cardFooter) - lowerEdge(baselineFooter))).isLessThan(1.5d);
                assertThat(keywordGap).isGreaterThanOrEqualTo(24.0d);
            }
        }
    }

    @Test
    void cardTypeBadgeIconsAlignVerticallyWithTheirLabels() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets?view=cards"));

            Locator datasetBadge = page.locator(".dp-result-card").filter(new Locator.FilterOptions()
                    .setHas(page.locator(".dp-result-card__title a:has-text('Schulstandorte')")))
                    .locator(".dp-result-card__type-badge");
            Locator seriesBadge = page.locator(".dp-result-card").filter(new Locator.FilterOptions()
                    .setHas(page.locator(".dp-result-card__title a:has-text('Energieverbrauch Gemeinden')")))
                    .locator(".dp-result-card__type-badge");

            BoundingBox datasetIcon = requireBoundingBox(datasetBadge.locator("svg"));
            BoundingBox datasetLabel = requireBoundingBox(datasetBadge.locator("span"));
            BoundingBox seriesIcon = requireBoundingBox(seriesBadge.locator("svg"));
            BoundingBox seriesLabel = requireBoundingBox(seriesBadge.locator("span"));

            assertThat(Math.abs(verticalCenter(datasetIcon) - verticalCenter(datasetLabel))).isLessThan(1.5d);
            assertThat(Math.abs(verticalCenter(seriesIcon) - verticalCenter(seriesLabel))).isLessThan(1.5d);
        }
    }

    @Test
    void sortSelectionAutoSubmitsAndKeepsViewAndFilterState() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets?view=cards&theme=Geografie"));

            assertThat(page.locator(".dp-card-grid").isVisible()).isTrue();
            assertThat(page.locator(".dp-view-toggle__link:has-text('Kachelansicht')").count()).isEqualTo(1);
            assertThat(page.locator(".dp-sort-form button").count()).isEqualTo(0);

            page.selectOption("#catalog-sort", "title-asc");

            waitForLocationSearchContains(page, "sort=title-asc", "view=cards", "theme=Geografie");
            assertThat(page.locator(".dp-card-grid").isVisible()).isTrue();
            assertThat(page.locator("#active-filter-chips a:has-text('Thema: Geografie')").count()).isEqualTo(1);
        }
    }

    @Test
    void viewToggleIconsAlignWithLabelsAndKeepVisibleSpacing() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            Locator cardsIcon = page.locator(".dp-view-toggle__link").first().locator(".dp-view-toggle__icon svg");
            Locator cardsLabel = page.locator(".dp-view-toggle__link").first().locator(".dp-view-toggle__label");
            Locator listIcon = page.locator(".dp-view-toggle__link--list .dp-view-toggle__icon svg");
            Locator listLabel = page.locator(".dp-view-toggle__link--list .dp-view-toggle__label");

            BoundingBox cardsIconBox = requireBoundingBox(cardsIcon);
            BoundingBox cardsLabelBox = requireBoundingBox(cardsLabel);
            BoundingBox listIconBox = requireBoundingBox(listIcon);
            BoundingBox listLabelBox = requireBoundingBox(listLabel);

            assertThat(Math.abs(verticalCenter(cardsIconBox) - verticalCenter(cardsLabelBox))).isLessThan(1.5d);
            assertThat(Math.abs(verticalCenter(listIconBox) - verticalCenter(listLabelBox))).isLessThan(1.5d);
            assertThat(cardsLabelBox.x - (cardsIconBox.x + cardsIconBox.width)).isGreaterThan(6.0d);
            assertThat(listLabelBox.x - (listIconBox.x + listIconBox.width)).isGreaterThan(6.0d);
        }
    }

    @Test
    void filterTriggerChevronUsesButtonTextColorAndStaysVerticallyCentered() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            Locator trigger = page.locator("#filter-trigger-theme");
            Locator chevron = page.locator("#filter-trigger-theme .dp-filter-trigger__chevron");
            Locator svg = page.locator("#filter-trigger-theme .dp-filter-trigger__chevron svg");

            assertThat(cssValue(trigger, "color")).isEqualTo(cssValue(chevron, "color"));
            assertThat(cssValue(svg, "fill")).isEqualTo(cssValue(trigger, "color"));
            assertThat(Math.abs(verticalCenter(requireBoundingBox(trigger)) - verticalCenter(requireBoundingBox(svg))))
                    .isLessThan(2.0d);
        }
    }

    @Test
    void expandAndMetadataIconsRenderBorderlessAtTwentyFourPixels() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            Locator expandButton = page.locator(".dp-expand-button").first();
            Locator expandIcon = expandButton.locator("svg");
            Locator metadataLink = page.locator(".dp-metadata-link").first();
            Locator metadataIcon = metadataLink.locator("svg");

            assertThat(cssValue(expandButton, "border-top-width")).isEqualTo("0px");
            assertThat(cssValue(expandButton, "border-right-width")).isEqualTo("0px");
            assertThat(cssValue(expandButton, "border-bottom-width")).isEqualTo("0px");
            assertThat(cssValue(expandButton, "border-left-width")).isEqualTo("0px");
            assertThat(cssValue(metadataLink, "border-top-width")).isEqualTo("0px");
            assertThat(cssValue(metadataLink, "border-right-width")).isEqualTo("0px");
            assertThat(cssValue(metadataLink, "border-bottom-width")).isEqualTo("0px");
            assertThat(cssValue(metadataLink, "border-left-width")).isEqualTo("0px");

            BoundingBox expandIconBox = requireBoundingBox(expandIcon);
            BoundingBox metadataIconBox = requireBoundingBox(metadataIcon);

            assertThat(Math.abs(expandIconBox.width - 24.0d)).isLessThan(0.5d);
            assertThat(Math.abs(expandIconBox.height - 24.0d)).isLessThan(0.5d);
            assertThat(Math.abs(metadataIconBox.width - 24.0d)).isLessThan(0.5d);
            assertThat(Math.abs(metadataIconBox.height - 24.0d)).isLessThan(0.5d);
        }
    }

    @Test
    void desktopListViewKeepsThreeDownloadPillsOnOneLineForSeriesRootAndIssueRows() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets?expanded=ch.so.abstimmungsresultate"));

            Locator seriesRootDownloads = page.locator(".dp-entry-row--series .dp-entry-downloads .dp-download-link");
            Locator issueDownloads = page.locator(".dp-issue-row").first().locator(".dp-entry-downloads .dp-download-link");

            assertSingleDownloadRow(seriesRootDownloads);
            assertSingleDownloadRow(issueDownloads);
        }
    }

    @Test
    void desktopListHeadersStaySeparatedAndReadableWithinTheirColumns() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1200, 1200))) {
            Page page = context.newPage();
            page.navigate(baseUrl("/datasets"));

            Locator publishedHeader = page.locator(".dp-entry-table thead th").nth(2);
            Locator detailsHeader = page.locator(".dp-entry-table thead th").nth(3);
            Locator downloadsHeader = page.locator(".dp-entry-table thead th").nth(4);

            assertThat(publishedHeader.textContent()).isEqualTo("Publiziert");
            assertThat(detailsHeader.textContent()).isEqualTo("Details");
            assertThat(downloadsHeader.textContent()).isEqualTo("Daten herunterladen");
            assertThat(detailsHeader.innerText()).isEqualTo("Details");

            assertThat(hasHorizontalOverflow(publishedHeader)).isFalse();
            assertThat(hasHorizontalOverflow(detailsHeader)).isFalse();
            assertThat(hasHorizontalOverflow(downloadsHeader)).isFalse();
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

    private static String cssValue(Locator locator, String property) {
        return ((String) locator.evaluate("(element, value) => getComputedStyle(element).getPropertyValue(value)", property))
                .trim();
    }

    private static double verticalCenter(BoundingBox boundingBox) {
        return boundingBox.y + (boundingBox.height / 2.0d);
    }

    private static double horizontalCenter(BoundingBox boundingBox) {
        return boundingBox.x + (boundingBox.width / 2.0d);
    }

    private static double lowerEdge(BoundingBox boundingBox) {
        return boundingBox.y + boundingBox.height;
    }

    private static List<Integer> findCardRowWithMixedKeywordHeights(Locator cards) {
        int cardCount = cards.count();
        List<RowCardMeasurement> currentRow = new ArrayList<>();
        List<Integer> fallbackRow = List.of();

        for (int index = 0; index < cardCount; index++) {
            Locator card = cards.nth(index);
            BoundingBox cardBox = requireBoundingBox(card);
            double keywordHeight = requireBoundingBox(card.locator(".dp-keyword-list")).height;

            if (currentRow.isEmpty() || Math.abs(currentRow.getFirst().top() - cardBox.y) < 1.5d) {
                currentRow.add(new RowCardMeasurement(index, cardBox.y, keywordHeight));
                continue;
            }

            if (currentRow.size() >= 2 && hasMixedKeywordHeights(currentRow)) {
                return currentRow.stream().map(RowCardMeasurement::index).toList();
            }
            if (fallbackRow.isEmpty() && currentRow.size() >= 2) {
                fallbackRow = currentRow.stream().map(RowCardMeasurement::index).toList();
            }

            currentRow = new ArrayList<>();
            currentRow.add(new RowCardMeasurement(index, cardBox.y, keywordHeight));
        }

        if (currentRow.size() >= 2 && hasMixedKeywordHeights(currentRow)) {
            return currentRow.stream().map(RowCardMeasurement::index).toList();
        }
        if (fallbackRow.isEmpty() && currentRow.size() >= 2) {
            return currentRow.stream().map(RowCardMeasurement::index).toList();
        }
        return fallbackRow;
    }

    private static boolean hasMixedKeywordHeights(List<RowCardMeasurement> row) {
        double minHeight = row.stream().mapToDouble(RowCardMeasurement::keywordHeight).min().orElse(0.0d);
        double maxHeight = row.stream().mapToDouble(RowCardMeasurement::keywordHeight).max().orElse(0.0d);
        return (maxHeight - minHeight) > 10.0d;
    }

    private record RowCardMeasurement(int index, double top, double keywordHeight) {
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

    private static boolean hasHorizontalOverflow(Locator locator) {
        return Boolean.TRUE.equals(locator.evaluate("element => element.scrollWidth > element.clientWidth + 1"));
    }

    private static void assertSingleDownloadRow(Locator downloadLinks) {
        assertThat(downloadLinks.count()).isGreaterThanOrEqualTo(3);

        BoundingBox first = requireBoundingBox(downloadLinks.nth(0));
        BoundingBox second = requireBoundingBox(downloadLinks.nth(1));
        BoundingBox third = requireBoundingBox(downloadLinks.nth(2));

        assertThat(Math.abs(first.y - second.y)).isLessThan(1.5d);
        assertThat(Math.abs(first.y - third.y)).isLessThan(1.5d);
    }
}
