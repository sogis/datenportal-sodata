@param String title
@param String description
@param String typeLabel
@param boolean openData
@param boolean structureDescribed
@param String modifiedLabel
@param String issuedLabel
<header class="dp-detail-hero">
  @template.components.detailBadges(
    typeLabel = typeLabel,
    openData = openData,
    structureDescribed = structureDescribed
  )
  <h1 class="dp-detail-title">${title}</h1>
  <p class="dp-detail-description">${description}</p>
  <dl class="dp-detail-facts" aria-label="Eckdaten">
    @if(!issuedLabel.isBlank())
      <div>
        <dt>Publiziert</dt>
        <dd>${issuedLabel}</dd>
      </div>
    @endif
    <div>
      <dt>Aktualisiert</dt>
      <dd>${modifiedLabel}</dd>
    </div>
  </dl>
</header>
