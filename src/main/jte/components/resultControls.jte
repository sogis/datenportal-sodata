@import ch.so.agi.datenportal.web.CatalogQueryParams
@import ch.so.agi.datenportal.web.view.FilterPanelVm
@import ch.so.agi.datenportal.web.view.ResultsVm
@param ResultsVm results
@param CatalogQueryParams queryParams
@param FilterPanelVm filterPanel
@param boolean outOfBand
<section id="result-controls" class="dp-result-controls" hx-swap-oob="${outOfBand ? "true" : null}">
  <div class="dp-result-controls__summary">
    <p id="results-summary" class="dp-results-summary">${results.totalLabel()}</p>
  </div>
  <div class="dp-result-controls__center">
    @template.components.mobileFilterButton(filterPanel = filterPanel)
    @template.components.viewToggle(results = results)
  </div>
  <div class="dp-result-controls__sort">
    <form
      class="dp-sort-form"
      method="get"
      action="/datasets"
      hx-get="/datasets"
      hx-target="#dataset-results-shell"
      hx-swap="outerHTML"
      hx-push-url="true"
      hx-indicator="#dataset-loading"
      hx-trigger="change from:#catalog-sort">
      <label for="catalog-sort">Sortieren nach</label>
      <select id="catalog-sort" name="sort" onchange="this.form.requestSubmit()">
        @for(var option : ch.so.agi.datenportal.search.SortMode.values())
          @if(option == results.sortMode())
            <option value="${option.parameterValue()}" selected>${option.label()}</option>
          @else
            <option value="${option.parameterValue()}">${option.label()}</option>
          @endif
        @endfor
      </select>
      @template.components.queryStateInputs(
        queryParams = queryParams,
        includeQuery = true,
        includeFilters = true,
        excludeFilterParameter = "",
        includeSort = false,
        includeView = true,
        includeExpanded = false
      )
    </form>
  </div>
</section>
