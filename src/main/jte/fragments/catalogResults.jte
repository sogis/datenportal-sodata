@import ch.so.agi.datenportal.web.CatalogQueryParams
@import ch.so.agi.datenportal.web.view.FilterPanelVm
@import ch.so.agi.datenportal.web.view.ResultsVm
@param ResultsVm results
@param FilterPanelVm filterPanel
@param CatalogQueryParams queryParams
<div id="dataset-results" class="dp-results">
  @template.components.activeFilterChips(filterPanel = filterPanel)
  @template.components.resultControls(results = results, queryParams = queryParams)
  @if(results.isEmpty())
    <p class="dp-results-empty">Keine Einträge gefunden.</p>
  @elseif(results.isCards())
    @template.components.entryCardGrid(cards = results.cards())
  @else
    @template.components.entryTable(rows = results.rows())
  @endif
</div>
