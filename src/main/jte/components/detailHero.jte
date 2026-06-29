@import ch.so.agi.datenportal.web.view.AccessStateVm
@param String title
@param String description
@param String typeLabel
@param AccessStateVm accessState
@param boolean structureDescribed
@param String modifiedLabel
@param String issuedLabel
@param boolean showFacts
@param String titleBadgeLabel
<header class="dp-detail-hero">
  <h1 class="dp-detail-title">
    <span>${title}</span>
    @if(!titleBadgeLabel.isBlank())
      <span class="dp-status-badge dp-status-badge--info">${titleBadgeLabel}</span>
    @endif
  </h1>
  <p class="dp-detail-description">${description}</p>
  @if(showFacts)
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
  @endif
</header>
