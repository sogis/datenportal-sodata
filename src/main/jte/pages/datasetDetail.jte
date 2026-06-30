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
        showFacts = false,
        titleBadgeLabel = ""
      )
      <div class="dp-detail-summary-layout">
        <div class="dp-detail-summary-main">
          @template.components.detailFeatureDownloadStrip(features = page.features(), downloads = page.downloads())
          @template.components.metadataSection(section = page.overview())
          @if(!page.temporalCoverage().items().isEmpty())
            @template.components.metadataSection(section = page.temporalCoverage())
          @endif
          @if(!page.topics().items().isEmpty())
            @template.components.metadataSection(section = page.topics())
          @endif
          @template.components.contactMetadataSection(section = page.responsibilitiesContact())
        </div>
        <aside class="dp-detail-side" aria-label="Daten nutzen">
          @template.components.detailActionPanel(
            structureQualityOriginHref = page.structureQualityOriginHref(),
            structureQualityOriginLabel = "Struktur, Qualität und Herkunft"
          )
        </aside>
      </div>
    </div>
  </section>
`)
