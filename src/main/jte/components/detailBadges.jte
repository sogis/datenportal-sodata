@param String typeLabel
@param boolean openData
@param boolean structureDescribed
<div class="dp-detail-badges" aria-label="Status">
  <span class="dp-type-badge">${typeLabel}</span>
  @if(openData)
    <span class="dp-status-badge dp-status-badge--open">Open Data</span>
  @endif
  @if(structureDescribed)
    <span class="dp-status-badge">Struktur beschrieben</span>
  @endif
</div>
