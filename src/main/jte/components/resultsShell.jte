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
      hx-indicator="#dataset-loading">Filter zurücksetzen</a>
  </div>
@elseif(results.isCards())
  @template.components.entryCardGrid(cards = results.cards())
@else
  @template.components.entryTable(rows = results.rows())
@endif
