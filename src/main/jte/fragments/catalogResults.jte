@import ch.so.agi.datenportal.web.CatalogQueryParams
@import ch.so.agi.datenportal.web.view.FilterPanelVm
@import ch.so.agi.datenportal.web.view.ResultsVm
@param ResultsVm results
@param FilterPanelVm filterPanel
@param CatalogQueryParams queryParams
<div id="dataset-results-shell" class="dp-results-shell">
  @template.components.resultsShell(results = results, queryParams = queryParams, resetHref = filterPanel.resetHref())
</div>
@template.components.filterBar(filterPanel = filterPanel, queryParams = queryParams, outOfBand = true)
@template.components.activeFilterChips(filterPanel = filterPanel, outOfBand = true)
@template.components.resultControls(results = results, queryParams = queryParams, filterPanel = filterPanel, outOfBand = true)
@template.components.pagination(results = results, queryParams = queryParams, outOfBand = true)
