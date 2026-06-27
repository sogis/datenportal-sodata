@import ch.so.agi.datenportal.web.view.IssueDetailPageVm
@param IssueDetailPageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-detail-page">
    <div class="dp-container">
      <p class="dp-detail-kicker">
        Datenreihe:
        <a href="${page.seriesHref()}">${page.seriesTitle()}</a>
      </p>
      @template.components.detailHero(
        title = page.title(),
        description = page.description(),
        typeLabel = "Ausgabe",
        accessState = page.accessState(),
        structureDescribed = page.structureDescribed(),
        modifiedLabel = page.modifiedLabel(),
        issuedLabel = page.issuedLabel(),
        showFacts = true
      )
      @if(page.currentIssue())
        <p class="dp-current-issue-note">Diese Ausgabe ist aktuell.</p>
      @endif
      <div class="dp-detail-layout">
        <div class="dp-detail-main">
          @template.components.seriesIssues(issues = page.relatedIssues())
          @for(var section : page.metadataSections())
            @template.components.metadataSection(section = section)
          @endfor
        </div>
        <aside class="dp-detail-side" aria-label="Downloads">
          @template.components.detailDownloadPanel(downloads = page.downloads())
        </aside>
      </div>
    </div>
  </section>
`)
