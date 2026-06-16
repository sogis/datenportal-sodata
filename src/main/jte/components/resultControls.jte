@import ch.so.agi.datenportal.web.CatalogQueryParams
@import ch.so.agi.datenportal.web.view.FilterPanelVm
@import ch.so.agi.datenportal.web.view.ResultsVm
@param ResultsVm results
@param CatalogQueryParams queryParams
@param FilterPanelVm filterPanel
@param boolean outOfBand
<section id="result-controls" class="dp-result-controls" hx-swap-oob="${outOfBand ? "true" : null}">
  <p id="results-summary" class="dp-results-summary">${results.controls().totalLabel()}</p>
  <div class="dp-result-controls__actions">
    @template.components.mobileFilterButton(filterPanel = filterPanel)
    @template.components.viewToggle(results = results)
    <form
      class="dp-sort-form"
      method="get"
      action="/datasets"
      hx-get="/datasets"
      hx-target="#dataset-results-shell"
      hx-swap="outerHTML"
      hx-push-url="true"
      hx-indicator="#dataset-loading">
      <label for="catalog-sort">Sortieren nach</label>
      <select id="catalog-sort" name="sort">
        @for(var option : results.controls().sortOptions())
          @if(option.selected())
            <option value="${option.value()}" selected>${option.label()}</option>
          @else
            <option value="${option.value()}">${option.label()}</option>
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
        includePage = false,
        includeSize = true,
        includeExpanded = false
      )
      <button class="dp-button dp-button--secondary" type="submit">Sortieren</button>
    </form>
  </div>
</section>
