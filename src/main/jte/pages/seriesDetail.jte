@import ch.so.agi.datenportal.web.view.SeriesDetailPageVm
@param SeriesDetailPageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-detail-page">
    <div class="dp-container">
      @template.components.detailHero(
        title = page.title(),
        description = page.description(),
        typeLabel = page.typeLabel(),
        accessState = page.accessState(),
        structureDescribed = page.structureDescribed(),
        modifiedLabel = page.modifiedLabel(),
        issuedLabel = page.issuedLabel()
      )
      <section class="dp-detail-panel dp-current-issue" aria-labelledby="current-issue-title">
        <p class="dp-detail-kicker">Aktuelle Ausgabe</p>
        <h2 id="current-issue-title">Ausgabe ${page.currentIssueLabel()}</h2>
        <p><a class="dp-detail-action" href="${page.currentIssueHref()}">Detailseite der aktuellen Ausgabe öffnen</a></p>
      </section>
      <div class="dp-detail-layout">
        <div class="dp-detail-main">
          @template.components.seriesIssues(issues = page.issues())
          @for(var section : page.metadataSections())
            @template.components.metadataSection(section = section)
          @endfor
        </div>
        <aside class="dp-detail-side" aria-label="Downloads">
          @template.components.detailDownloadPanel(downloads = page.currentIssueDownloads())
        </aside>
      </div>
    </div>
  </section>
`)
