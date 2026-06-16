@import ch.so.agi.datenportal.web.CatalogQueryParams
@import ch.so.agi.datenportal.web.view.FilterPanelVm
@param FilterPanelVm filterPanel
@param CatalogQueryParams queryParams
<div class="dp-filter-overlay dp-filter-overlay--sheet" data-filter-overlay="sheet">
  <button class="dp-filter-sheet__backdrop" type="button" data-filter-close aria-label="Filteransicht schliessen"></button>
  <div
    class="dp-filter-sheet"
    role="dialog"
    aria-modal="true"
    aria-labelledby="mobile-filter-title"
    data-filter-panel>
    <div class="dp-filter-sheet__header">
      <h2 id="mobile-filter-title" class="dp-filter-sheet__title">Filter</h2>
      <button class="dp-filter-close" type="button" data-filter-close aria-label="Filter schliessen">×</button>
    </div>
    <form
      class="dp-filter-sheet__form"
      method="get"
      action="/datasets"
      hx-get="/datasets"
      hx-target="#dataset-results-shell"
      hx-swap="outerHTML"
      hx-push-url="true"
      hx-indicator="#dataset-loading"
      data-filter-apply-form>
      @template.components.queryStateInputs(
        queryParams = queryParams,
        includeQuery = true,
        includeFilters = false,
        excludeFilterParameter = "",
        includeSort = true,
        includeView = true,
        includePage = false,
        includeSize = true,
        includeExpanded = false
      )
      <div class="dp-filter-sheet__groups">
        @for(var group : filterPanel.groups())
          @if(group.selectedCount() > 0 || "theme".equals(group.id()))
            <details class="dp-mobile-filter-group" open>
              <summary>
                <span>${group.label()}</span>
                <span>${group.collapsedLabel()}</span>
              </summary>
              @template.components.filterFieldList(group = group, inputIdPrefix = "mobile-" + group.id())
            </details>
          @else
            <details class="dp-mobile-filter-group">
              <summary>
                <span>${group.label()}</span>
                <span>${group.collapsedLabel()}</span>
              </summary>
              @template.components.filterFieldList(group = group, inputIdPrefix = "mobile-" + group.id())
            </details>
          @endif
        @endfor
      </div>
      <div class="dp-filter-sheet__actions">
        <a
          class="dp-button dp-button--secondary"
          href="${filterPanel.resetHref()}"
          hx-get="${filterPanel.resetHref()}"
          hx-target="#dataset-results-shell"
          hx-swap="outerHTML"
          hx-push-url="true"
          hx-indicator="#dataset-loading">Alle zurücksetzen</a>
        <button class="dp-button dp-button--primary" type="submit">Ergebnisse anzeigen</button>
      </div>
    </form>
  </div>
</div>
