@import ch.so.agi.datenportal.web.view.AccessStateVm
@param String typeLabel
@param AccessStateVm accessState
@param boolean structureDescribed
<div class="dp-detail-badges" aria-label="Status">
  <span class="dp-type-badge">${typeLabel}</span>
  @template.components.accessBadge(accessState = accessState)
  @if(structureDescribed)
    <span class="dp-status-badge dp-status-badge--neutral">Struktur beschrieben</span>
  @endif
</div>
