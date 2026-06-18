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
        <span class="dp-filter-trigger__chevron" aria-hidden="true">
          <svg xmlns="http://www.w3.org/2000/svg" width="1rem" height="1rem" fill="currentColor" class="bi bi-chevron-down" viewBox="0 0 16 16">
            <path fill-rule="evenodd" d="M1.646 4.646a.5.5 0 0 1 .708 0L8 10.293l5.646-5.647a.5.5 0 0 1 .708.708l-6 6a.5.5 0 0 1-.708 0l-6-6a.5.5 0 0 1 0-.708"/>
          </svg>
        </span>
      </button>
    @endfor
    <a
      class="dp-filter-reset-button"
      href="${filterPanel.resetHref()}"
      hx-get="${filterPanel.resetHref()}"
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
        <a class="dp-button dp-button--secondary" href="${filterPanel.resetHref()}">
          <span class="dp-inline-icon-label">
            <svg xmlns="http://www.w3.org/2000/svg" width="1rem" height="1rem" fill="currentColor" class="bi bi-arrow-clockwise" viewBox="0 0 16 16" aria-hidden="true">
              <path fill-rule="evenodd" d="M8 3a5 5 0 1 0 4.546 2.914.5.5 0 0 1 .908-.417A6 6 0 1 1 8 2z"/>
              <path d="M8 4.466V.534a.25.25 0 0 1 .41-.192l2.36 1.966c.12.1.12.284 0 .384L8.41 4.658A.25.25 0 0 1 8 4.466"/>
            </svg>
            <span>Filter zurücksetzen</span>
          </span>
        </a>
      </div>
    </form>
  </noscript>
</div>
