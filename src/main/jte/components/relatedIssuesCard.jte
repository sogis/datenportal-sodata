@import java.util.List
@import ch.so.agi.datenportal.web.view.RelatedIssueVm
@param List<RelatedIssueVm> relatedIssues
<section class="dp-detail-panel dp-related-issues" aria-labelledby="related-issues-title">
  <h2 id="related-issues-title">Weitere Ausgaben</h2>
  @if(relatedIssues.isEmpty())
    <p>Zu dieser Datenreihe sind keine weiteren Ausgaben verfügbar.</p>
  @else
    <ul class="dp-related-issues__list">
      @for(var issue : relatedIssues)
        <li class="dp-related-issue">
          <a class="dp-related-issue__title" href="${issue.detailHref()}">${issue.title()}</a>
          <span class="dp-related-issue__meta">Ausgabe ${issue.issueLabel()} · Publiziert ${issue.publicationDateLabel()}</span>
          @if(issue.current())
            <span class="dp-status-badge dp-status-badge--info">Aktuelle Ausgabe</span>
          @endif
        </li>
      @endfor
    </ul>
  @endif
</section>
