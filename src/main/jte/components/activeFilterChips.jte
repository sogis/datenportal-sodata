@import ch.so.agi.datenportal.web.view.FilterPanelVm
@param FilterPanelVm filterPanel
@if(filterPanel.hasActiveFilters())
  <div class="dp-active-filters" aria-label="Aktive Filter">
    <span class="dp-active-filters__label">Aktive Filter</span>
    <ul class="dp-chip-list">
      @for(var chip : filterPanel.chips())
        <li>
          <a class="dp-filter-chip" href="${chip.removeHref()}" aria-label="Filter entfernen: ${chip.groupLabel()} ${chip.label()}">
            <span>${chip.groupLabel()}: ${chip.label()}</span>
            <span aria-hidden="true">x</span>
          </a>
        </li>
      @endfor
    </ul>
    <a class="dp-filter-reset-link" href="${filterPanel.resetHref()}">Filter zurücksetzen</a>
  </div>
@endif
