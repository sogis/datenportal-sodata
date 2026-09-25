@import ch.so.agi.datenportal.web.view.EntryCardVm
@param EntryCardVm card
<article class="dp-result-card">
  <div class="dp-result-card__badges">
    <span class="dp-type-badge dp-result-card__type-badge dp-status-badge--info">
      @if("Datenreihe".equals(card.typeLabel()))
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-collection" viewBox="0 0 16 16" aria-hidden="true">
          <path d="M2.5 3.5a.5.5 0 0 1 0-1h11a.5.5 0 0 1 0 1zm2-2a.5.5 0 0 1 0-1h7a.5.5 0 0 1 0 1zM0 13a1.5 1.5 0 0 0 1.5 1.5h13A1.5 1.5 0 0 0 16 13V6a1.5 1.5 0 0 0-1.5-1.5h-13A1.5 1.5 0 0 0 0 6zm1.5.5A.5.5 0 0 1 1 13V6a.5.5 0 0 1 .5-.5h13a.5.5 0 0 1 .5.5v7a.5.5 0 0 1-.5.5z"/>
        </svg>
      @else
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-file-earmark" viewBox="0 0 16 16" aria-hidden="true">
          <path d="M14 4.5V14a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V2a2 2 0 0 1 2-2h5.5zm-3 0A1.5 1.5 0 0 1 9.5 3V1H4a1 1 0 0 0-1 1v12a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1V4.5z"/>
        </svg>
      @endif
      <span>${card.typeLabel()}</span>
    </span>
  </div>
  <h2 class="dp-result-card__title">
    <a href="${card.detailHref()}">${card.title()}</a>
  </h2>
  <p class="dp-result-card__description">${card.description()}@if(card.descriptionTruncated())... <a class="dp-result-card__description-link" href="${card.detailHref()}" aria-label="Details anzeigen: ${card.title()}">[Details anzeigen]</a>@endif</p>
  @if(!card.keywords().isEmpty())
    <ul class="dp-keyword-list">
      @for(var keyword : card.keywords())
        <li>${keyword}</li>
      @endfor
    </ul>
  @endif
  <div class="dp-result-card__bottom">
    @template.components.downloadLink(accessState = card.accessState(), downloads = card.downloads())
    <div class="dp-result-card__footer">
      <span>${card.dateLabel()}</span>
      <a class="dp-card-detail-link" href="${card.detailHref()}" aria-label="Details anzeigen: ${card.title()}">&rarr;</a>
    </div>
  </div>
</article>
