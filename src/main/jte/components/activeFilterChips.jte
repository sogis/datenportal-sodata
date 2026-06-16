@import ch.so.agi.datenportal.web.view.FilterPanelVm
@param FilterPanelVm filterPanel
@param boolean outOfBand
<div id="active-filter-chips" class="dp-active-filters${filterPanel.hasActiveFilters() ? "" : " is-empty"}" aria-label="Aktive Filter" hx-swap-oob="${outOfBand ? "true" : null}">
  @if(filterPanel.hasActiveFilters())
    <span class="dp-active-filters__label">Aktive Filter</span>
    <ul class="dp-chip-list">
      @for(var chip : filterPanel.chips())
        <li>
          <a
            class="dp-filter-chip"
            href="${chip.removeHref()}"
            hx-get="${chip.removeHref()}"
            hx-target="#dataset-results-shell"
            hx-swap="outerHTML"
            hx-push-url="true"
            hx-indicator="#dataset-loading"
            aria-label="Filter entfernen: ${chip.groupLabel()} ${chip.label()}">
            <span>${chip.groupLabel()}: ${chip.label()}</span>
            <span aria-hidden="true">×</span>
          </a>
        </li>
      @endfor
    </ul>
    <a
      class="dp-filter-reset-link"
      href="${filterPanel.resetHref()}"
      hx-get="${filterPanel.resetHref()}"
      hx-target="#dataset-results-shell"
      hx-swap="outerHTML"
      hx-push-url="true"
      hx-indicator="#dataset-loading">Alle zurücksetzen</a>
  @endif
</div>
