@param String title
@param ch.so.agi.datenportal.support.metadata.MetadataTextRenderer.Html description
@param String titleBadgeLabel
<header class="dp-detail-hero">
  <h1 class="dp-detail-title">
    <span>${title}</span>
    @if(!titleBadgeLabel.isBlank())
      <span class="dp-status-badge dp-status-badge--info">${titleBadgeLabel}</span>
    @endif
  </h1>
  <div class="dp-detail-description dp-metadata-text">${description}</div>
</header>
