@param String title
@param String description
@param String titleBadgeLabel
<header class="dp-detail-hero">
  <h1 class="dp-detail-title">
    <span>${title}</span>
    @if(!titleBadgeLabel.isBlank())
      <span class="dp-status-badge dp-status-badge--info">${titleBadgeLabel}</span>
    @endif
  </h1>
  <p class="dp-detail-description">${description}</p>
</header>
