@import ch.so.agi.datenportal.web.CatalogQueryParams
@import ch.so.agi.datenportal.web.view.ResultsVm
@param ResultsVm results
@param CatalogQueryParams queryParams
@param boolean outOfBand
@if(results.controls().pagination().visible())
  <nav id="pagination" class="dp-pagination" aria-label="Pagination" hx-swap-oob="${outOfBand ? "true" : null}">
    <div class="dp-pagination__pages">
      @if(results.controls().pagination().hasPrevious())
        <a
          class="dp-pagination__nav"
          href="${results.controls().pagination().previousHref()}"
          hx-get="${results.controls().pagination().previousHref()}"
          hx-target="#dataset-results-shell"
          hx-swap="outerHTML"
          hx-push-url="true"
          hx-indicator="#dataset-loading"
          aria-label="Vorherige Seite">‹</a>
      @endif
      @for(var item : results.controls().pagination().items())
        @if(item.ellipsis())
          <span class="dp-pagination__ellipsis">${item.label()}</span>
        @elseif(item.current())
          <span class="dp-pagination__page is-current" aria-current="page">${item.label()}</span>
        @else
          <a
            class="dp-pagination__page"
            href="${item.href()}"
            hx-get="${item.href()}"
            hx-target="#dataset-results-shell"
            hx-swap="outerHTML"
            hx-push-url="true"
            hx-indicator="#dataset-loading">${item.label()}</a>
        @endif
      @endfor
      @if(results.controls().pagination().hasNext())
        <a
          class="dp-pagination__nav"
          href="${results.controls().pagination().nextHref()}"
          hx-get="${results.controls().pagination().nextHref()}"
          hx-target="#dataset-results-shell"
          hx-swap="outerHTML"
          hx-push-url="true"
          hx-indicator="#dataset-loading"
          aria-label="Nächste Seite">›</a>
      @endif
    </div>
    <form
      class="dp-page-size-form"
      method="get"
      action="/datasets"
      hx-get="/datasets"
      hx-target="#dataset-results-shell"
      hx-swap="outerHTML"
      hx-push-url="true"
      hx-indicator="#dataset-loading">
      <label for="catalog-page-size">Zeilen pro Seite</label>
      <select id="catalog-page-size" name="size">
        @for(var value : results.controls().pagination().sizeOptions())
          @if(value == results.controls().pagination().currentSize())
            <option value="${value}" selected>${value}</option>
          @else
            <option value="${value}">${value}</option>
          @endif
        @endfor
      </select>
      @template.components.queryStateInputs(
        queryParams = queryParams,
        includeQuery = true,
        includeFilters = true,
        excludeFilterParameter = "",
        includeSort = true,
        includeView = true,
        includePage = false,
        includeSize = false,
        includeExpanded = false
      )
      <button class="dp-button dp-button--secondary" type="submit">Übernehmen</button>
    </form>
  </nav>
@else
  <div id="pagination" class="dp-pagination is-empty" hx-swap-oob="${outOfBand ? "true" : null}"></div>
@endif
