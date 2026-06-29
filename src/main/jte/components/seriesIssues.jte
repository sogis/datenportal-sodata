@import ch.so.agi.datenportal.web.view.SeriesIssuesVm
@param SeriesIssuesVm issues
@if(!issues.issues().isEmpty())
  <section class="dp-detail-panel dp-series-issues" aria-labelledby="series-issues-title">
    <h2 id="series-issues-title">${issues.title()}</h2>
    <div class="dp-series-issues__list">
      @for(var issue : issues.issues())
        <article class="dp-series-issue">
          <div>
            <h2>
              <a href="${issue.detailHref()}">${issue.title()}</a>
            </h2>
            <p class="dp-series-issue__meta">
              <span>Ausgabe ${issue.issueLabel()} · Publiziert ${issue.publicationDateLabel()}</span>
              @if(issue.current())
                <span class="dp-status-badge dp-status-badge--info">Aktuelle Ausgabe</span>
              @endif
            </p>
            <p class="dp-series-issue__detail">
              <a class="dp-series-issue__detail-link" href="${issue.detailHref()}">Detailseite anzeigen <span aria-hidden="true">→</span></a>
            </p>
          </div>
          @template.components.downloadLink(accessState = issue.accessState(), downloads = issue.downloads())
        </article>
      @endfor
    </div>
  </section>
@endif
