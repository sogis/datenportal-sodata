@import ch.so.agi.datenportal.web.view.FilterPanelVm
@param FilterPanelVm filterPanel
<div class="dp-filter-bar" aria-label="Filter">
  @for(var group : filterPanel.groups())
    <details class="dp-filter-group">
      <summary class="dp-filter-group__summary">
        <span class="dp-filter-group__label">${group.label()}</span>
        <span class="dp-filter-group__state">${group.collapsedLabel()}</span>
      </summary>
      <div class="dp-filter-group__options">
        @for(var option : group.options())
          <label class="dp-filter-option">
            @if(option.selected())
              <input type="checkbox" name="${group.parameterName()}" value="${option.value()}" checked>
            @else
              <input type="checkbox" name="${group.parameterName()}" value="${option.value()}">
            @endif
            <span>${option.label()}</span>
            <span class="dp-filter-option__count">${option.resultCount()}</span>
          </label>
        @endfor
      </div>
    </details>
  @endfor
</div>
