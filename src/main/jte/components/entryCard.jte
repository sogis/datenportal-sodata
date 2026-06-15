@import ch.so.agi.datenportal.web.view.CardResultVm
@param CardResultVm card
<article class="dp-result-card">
  <div class="dp-result-card__badges">
    <span class="dp-type-badge">${card.typeLabel()}</span>
    @if(card.openData())
      <span class="dp-status-badge dp-status-badge--open">Open Data</span>
    @endif
    @if(card.structureDescribed())
      <span class="dp-status-badge">Struktur beschrieben</span>
    @endif
  </div>
  <h2 class="dp-result-card__title">
    <a href="${card.detailHref()}">${card.title()}</a>
  </h2>
  <p class="dp-result-card__description">${card.description()}</p>
  @if(!card.keywords().isEmpty())
    <ul class="dp-keyword-list">
      @for(var keyword : card.keywords())
        <li>${keyword}</li>
      @endfor
    </ul>
  @endif
  @template.components.downloadLink(downloads = card.downloads())
  <div class="dp-result-card__footer">
    <span>${card.dateLabel()}</span>
    <a class="dp-card-detail-link" href="${card.detailHref()}" aria-label="Details anzeigen: ${card.title()}">&rarr;</a>
  </div>
</article>
