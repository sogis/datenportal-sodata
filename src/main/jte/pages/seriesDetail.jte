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
        issuedLabel = page.issuedLabel(),
        showFacts = false
      )
      <div class="dp-detail-summary-main">
        @template.components.seriesIssues(issues = page.issues())
      </div>
    </div>
  </section>
`)
