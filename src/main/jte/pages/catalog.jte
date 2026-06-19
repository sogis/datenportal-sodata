@import ch.so.agi.datenportal.web.view.CatalogPageVm
@param CatalogPageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-catalog-intro">
    <div class="dp-container">
      <h1 class="dp-page-title">${page.title()}</h1>
      <p class="dp-lead">${page.lead()}</p>
      <p class="dp-lead-secondary">${page.secondaryLead()}</p>
      <div class="dp-catalog-shell">
        <div class="dp-control-band">
          @template.components.searchForm(queryParams = page.queryParams())
          @template.components.filterBar(filterPanel = page.filterPanel(), queryParams = page.queryParams(), outOfBand = false)
        </div>
        @template.components.activeFilterChips(filterPanel = page.filterPanel(), outOfBand = false)
        <div class="dp-results-stack">
          @template.components.resultControls(results = page.results(), queryParams = page.queryParams(), filterPanel = page.filterPanel(), outOfBand = false)
          <div id="dataset-results-shell" class="dp-results-shell">
            @template.components.resultsShell(results = page.results(), queryParams = page.queryParams(), resetHref = page.filterPanel().resetHref())
          </div>
        </div>
      </div>
      <div id="filter-popover-host" class="dp-filter-host" hidden></div>
      <div id="mobile-filter-panel-host" class="dp-filter-host" hidden></div>
      <div id="dataset-loading" class="dp-loading-indicator htmx-indicator">Datensätze werden geladen ...</div>
    </div>
  </section>
`)
