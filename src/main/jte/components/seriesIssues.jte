@import java.util.List
@import ch.so.agi.datenportal.web.view.SeriesIssueVm
@param List<SeriesIssueVm> issues
@param boolean showTitle
@if(!issues.isEmpty())
  <section class="dp-series-issues" aria-labelledby="${showTitle ? "series-issues-title" : null}" aria-label="${showTitle ? null : "Ausgaben"}">
    @if(showTitle)
      <h2 id="series-issues-title">Ausgaben</h2>
    @endif
    <div class="dp-series-issues__list">
      @for(var issue : issues)
        <article class="dp-series-issue">
          <div>
            <h2 class="dp-series-issue__title">
              <a href="${issue.detailHref()}">Ausgabe: ${issue.issueLabel()}</a>
              @if(issue.current())
                <span class="dp-status-badge dp-status-badge--info">Aktuelle Ausgabe</span>
              @endif
            </h2>
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
