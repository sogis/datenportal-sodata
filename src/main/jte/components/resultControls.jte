@import ch.so.agi.datenportal.web.CatalogQueryParams
@import ch.so.agi.datenportal.web.view.ResultsVm
@param ResultsVm results
@param CatalogQueryParams queryParams
<div class="dp-result-controls">
  <p class="dp-results-summary">${results.controls().totalLabel()}</p>
  <div class="dp-result-controls__actions">
    @template.components.viewToggle(results = results)
    <form
      class="dp-sort-form"
      method="get"
      action="/datasets"
      hx-get="/datasets"
      hx-target="#dataset-results"
      hx-select="#dataset-results"
      hx-push-url="true"
      hx-trigger="change">
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
      @if(!queryParams.q().isBlank())
        <input type="hidden" name="q" value="${queryParams.q()}">
      @endif
      @for(var value : queryParams.theme())
        <input type="hidden" name="theme" value="${value}">
      @endfor
      @for(var value : queryParams.office())
        <input type="hidden" name="office" value="${value}">
      @endfor
      @for(var value : queryParams.modified())
        <input type="hidden" name="modified" value="${value}">
      @endfor
      @for(var value : queryParams.resourceType())
        <input type="hidden" name="resourceType" value="${value}">
      @endfor
      <input type="hidden" name="view" value="${queryParams.viewValue()}">
      @for(var value : queryParams.expanded())
        <input type="hidden" name="expanded" value="${value}">
      @endfor
      <button class="dp-button dp-button--secondary" type="submit">Sortieren</button>
    </form>
  </div>
</div>
