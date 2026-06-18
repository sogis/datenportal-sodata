@import ch.so.agi.datenportal.web.CatalogQueryParams
@import ch.so.agi.datenportal.web.view.ResultsVm
@param ResultsVm results
@param CatalogQueryParams queryParams
@param String resetHref
@if(results.isEmpty())
  <div class="dp-results-empty-state">
    @if(queryParams.q().isBlank())
      <p class="dp-results-empty">Keine Datensätze gefunden.</p>
    @else
      <p class="dp-results-empty">Keine Datensätze für „${queryParams.q()}“ gefunden.</p>
    @endif
    <p class="dp-results-empty-hint">Passen Sie die Suche oder die Filter an.</p>
    <a
      class="dp-button dp-button--secondary"
      href="${resetHref}"
      hx-get="${resetHref}"
      hx-target="#dataset-results-shell"
      hx-swap="outerHTML"
      hx-push-url="true"
      hx-indicator="#dataset-loading">
      <span class="dp-inline-icon-label">
        <svg xmlns="http://www.w3.org/2000/svg" width="1rem" height="1rem" fill="currentColor" class="bi bi-arrow-clockwise" viewBox="0 0 16 16" aria-hidden="true">
          <path fill-rule="evenodd" d="M8 3a5 5 0 1 0 4.546 2.914.5.5 0 0 1 .908-.417A6 6 0 1 1 8 2z"/>
          <path d="M8 4.466V.534a.25.25 0 0 1 .41-.192l2.36 1.966c.12.1.12.284 0 .384L8.41 4.658A.25.25 0 0 1 8 4.466"/>
        </svg>
        <span>Filter zurücksetzen</span>
      </span>
    </a>
  </div>
@elseif(results.isCards())
  @template.components.entryCardGrid(cards = results.cards())
@else
  @template.components.entryTable(rows = results.rows())
@endif
