@import ch.so.agi.datenportal.web.CatalogQueryParams
@import ch.so.agi.datenportal.web.view.FilterGroupVm
@param FilterGroupVm group
@param CatalogQueryParams queryParams
<div class="dp-filter-overlay dp-filter-overlay--popover" data-filter-overlay="popover" data-filter-id="${group.id()}">
  <div
    class="dp-filter-popover"
    role="dialog"
    aria-modal="false"
    aria-labelledby="filter-popover-title-${group.id()}"
    data-filter-panel>
    <div class="dp-filter-popover__header">
      <h2 id="filter-popover-title-${group.id()}" class="dp-filter-popover__title">${group.label()}</h2>
      <button class="dp-filter-close" type="button" data-filter-close aria-label="Filter schliessen">×</button>
    </div>
    <form
      class="dp-filter-form"
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
        includeFilters = true,
        excludeFilterParameter = group.parameterName(),
        includeSort = true,
        includeView = true,
        includePage = false,
        includeSize = true,
        includeExpanded = false
      )
      @template.components.filterFieldList(group = group, inputIdPrefix = "popover-" + group.id())
      <div class="dp-filter-popover__actions">
        <a
          class="dp-button dp-button--secondary"
          href="${group.resetHref()}"
          hx-get="${group.resetHref()}"
          hx-target="#dataset-results-shell"
          hx-swap="outerHTML"
          hx-push-url="true"
          hx-indicator="#dataset-loading">Zurücksetzen</a>
        <button class="dp-button dp-button--primary" type="submit">Anwenden</button>
      </div>
    </form>
  </div>
</div>
