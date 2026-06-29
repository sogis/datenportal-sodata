@import ch.so.agi.datenportal.web.view.SeriesDetailPageVm
@param SeriesDetailPageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-detail-page dp-series-detail-page">
    <div class="dp-container">
      @template.components.detailHero(
        title = page.title(),
        description = page.description(),
        typeLabel = page.typeLabel(),
        accessState = page.accessState(),
        structureDescribed = page.structureDescribed(),
        modifiedLabel = page.modifiedLabel(),
        issuedLabel = page.issuedLabel(),
        showFacts = false,
        titleBadgeLabel = ""
      )
      <p class="dp-series-issues-intro">Zu dieser Serie sind folgende Ausgaben verfügbar:</p>
      <div class="dp-detail-summary-main">
        @template.components.seriesIssues(issues = page.issues(), showTitle = false)
      </div>
    </div>
  </section>
`)
