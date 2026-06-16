@import ch.so.agi.datenportal.web.view.FilterGroupType
@import ch.so.agi.datenportal.web.view.FilterGroupVm
@param FilterGroupVm group
@param String inputIdPrefix
<div class="dp-filter-group__options">
  @if(group.type() == FilterGroupType.SINGLE_SELECT)
    <label class="dp-filter-option" for="${inputIdPrefix}-all">
      <input
        id="${inputIdPrefix}-all"
        type="radio"
        name="${group.parameterName()}"
        value=""
        checked="${group.selectedCount() == 0}">
      <span>Alle Zeiträume</span>
    </label>
  @endif
  @for(var option : group.options())
    <label class="dp-filter-option" for="${inputIdPrefix}-${option.value()}">
      <input
        id="${inputIdPrefix}-${option.value()}"
        type="${group.type() == FilterGroupType.SINGLE_SELECT ? "radio" : "checkbox"}"
        name="${group.parameterName()}"
        value="${option.value()}"
        checked="${option.selected()}"
        disabled="${option.disabled()}">
      <span>${option.label()}</span>
      <span class="dp-filter-option__count">${option.resultCount()}</span>
    </label>
  @endfor
</div>
