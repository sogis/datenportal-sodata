@import ch.so.agi.datenportal.web.CatalogQueryParams
@import ch.so.agi.datenportal.web.view.FilterGroupVm
@param FilterGroupVm group
@param CatalogQueryParams queryParams
<div
  id="filter-panel-${group.id()}"
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
      includeExpanded = false
    )
    @template.components.filterFieldList(group = group, inputIdPrefix = "popover-" + group.id())
    <div class="dp-filter-popover__actions">
      <button class="dp-button dp-button--secondary" type="button" data-filter-reset>Zurücksetzen</button>
      <button class="dp-button dp-button--primary" type="submit">Anwenden</button>
    </div>
  </form>
</div>
