@import ch.so.agi.datenportal.web.CatalogQueryParams
@import ch.so.agi.datenportal.web.view.FilterPanelVm
@param FilterPanelVm filterPanel
@param CatalogQueryParams queryParams
@param boolean outOfBand
<div id="filter-toolbar" class="dp-filter-toolbar" aria-label="Filter" hx-swap-oob="${outOfBand ? "true" : null}">
  <div class="dp-filter-toolbar__desktop">
    @for(var group : filterPanel.groups())
      <button
        id="filter-trigger-${group.id()}"
        class="dp-filter-trigger${group.selectedCount() > 0 ? " is-active" : ""}"
        type="button"
        aria-expanded="false"
        aria-controls="filter-popover-host"
        data-filter-trigger
        data-filter-id="${group.id()}"
        hx-get="${group.triggerHref()}"
        hx-target="#filter-popover-host"
        hx-swap="innerHTML">
        <span class="dp-filter-trigger__text">
          <span class="dp-filter-trigger__label">${group.label()}</span>
          <span class="dp-filter-trigger__state">${group.collapsedLabel()}</span>
        </span>
        <span class="dp-filter-trigger__chevron" aria-hidden="true">⌄</span>
      </button>
    @endfor
    <a
      class="dp-filter-reset-button"
      href="${filterPanel.resetHref()}"
      hx-get="${filterPanel.resetHref()}"
      hx-target="#dataset-results-shell"
      hx-swap="outerHTML"
      hx-push-url="true"
      hx-indicator="#dataset-loading">Filter zurücksetzen</a>
  </div>

  <noscript>
    <form class="dp-filter-fallback" method="get" action="/datasets">
      @template.components.queryStateInputs(
        queryParams = queryParams,
        includeQuery = true,
        includeFilters = false,
        excludeFilterParameter = "",
        includeSort = true,
        includeView = true,
        includePage = false,
        includeSize = true,
        includeExpanded = false
      )
      <div class="dp-filter-fallback__groups">
        @for(var group : filterPanel.groups())
          <details class="dp-filter-fallback__group">
            <summary>${group.label()} · ${group.collapsedLabel()}</summary>
            @template.components.filterFieldList(group = group, inputIdPrefix = "noscript-" + group.id())
          </details>
        @endfor
      </div>
      <div class="dp-filter-fallback__actions">
        <button class="dp-button dp-button--primary" type="submit">Anwenden</button>
        <a class="dp-button dp-button--secondary" href="${filterPanel.resetHref()}">Filter zurücksetzen</a>
      </div>
    </form>
  </noscript>
</div>
