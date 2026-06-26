@import ch.so.agi.datenportal.web.view.SeriesIssuesVm
@param SeriesIssuesVm issues
@if(!issues.issues().isEmpty())
  <section class="dp-detail-panel dp-series-issues" aria-labelledby="series-issues-title">
    <h2 id="series-issues-title">${issues.title()}</h2>
    <div class="dp-series-issues__list">
      @for(var issue : issues.issues())
        <article class="dp-series-issue">
          <div>
            <h3>
              <a href="${issue.detailHref()}">${issue.title()}</a>
            </h3>
            <p>
              Ausgabe ${issue.issueLabel()} · Publiziert ${issue.publicationDateLabel()}
              @if(issue.current())
                <span class="dp-status-badge dp-status-badge--info">Aktuelle Ausgabe</span>
              @endif
            </p>
          </div>
          @template.components.downloadLink(accessState = issue.accessState(), downloads = issue.downloads())
        </article>
      @endfor
    </div>
  </section>
@endif
