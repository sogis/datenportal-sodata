@import ch.so.agi.datenportal.web.view.DatasetDetailPageVm
@param DatasetDetailPageVm page
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
      <div class="dp-detail-summary-layout">
        <div class="dp-detail-summary-main">
          @template.components.detailFeatureDownloadStrip(features = page.features(), downloads = page.downloads())
        </div>
        <aside class="dp-detail-side" aria-label="Aktionen">
          @template.components.detailActionPanel()
        </aside>
      </div>
    </div>
  </section>
`)
