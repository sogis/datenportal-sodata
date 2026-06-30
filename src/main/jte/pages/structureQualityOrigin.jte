@import ch.so.agi.datenportal.web.view.StructureQualityOriginPageVm
@param StructureQualityOriginPageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-detail-page dp-structure-quality-origin-page">
    <div class="dp-container">
      <header class="dp-detail-hero dp-structure-hero">
        <h1 class="dp-detail-title">${page.title()}</h1>
      </header>
      <div class="dp-structure-content">
        <div class="dp-detail-summary-layout">
          <div class="dp-detail-summary-main">
            @template.components.structureKpis(kpis = page.kpis())
          </div>
        </div>
        @template.components.attributeTable(attributes = page.attributes(), emptyText = page.emptyAttributesText())
        <div class="dp-detail-summary-layout">
          <div class="dp-detail-summary-main">
            @template.components.qualityCard(quality = page.quality())
            @if(page.originUsage().isPresent())
              @template.components.metadataSection(section = page.originUsage().get())
            @endif
          </div>
        </div>
      </div>
    </div>
  </section>
`)
