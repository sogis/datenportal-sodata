@import ch.so.agi.datenportal.web.view.FilterPanelVm
@param FilterPanelVm filterPanel
<button
  id="mobile-filter-button"
  class="dp-mobile-filter-button"
  type="button"
  aria-controls="mobile-filter-panel-host"
  aria-expanded="false"
  data-mobile-filter-trigger
  hx-get="${filterPanel.mobileFilterHref()}"
  hx-target="#mobile-filter-panel-host"
  hx-swap="innerHTML">
  <span>Filter</span>
  @if(filterPanel.activeValueCount() > 0)
    <span class="dp-mobile-filter-button__count">(${filterPanel.activeValueCount()})</span>
  @endif
</button>
