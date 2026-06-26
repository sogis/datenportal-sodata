@import ch.so.agi.datenportal.web.view.AccessStateVm
@param AccessStateVm accessState
@if(accessState.openData())
  <span class="dp-status-badge dp-status-badge--positive">Open Data</span>
@else
  <span class="dp-status-badge dp-status-badge--warning">${accessState.label()}</span>
@endif
