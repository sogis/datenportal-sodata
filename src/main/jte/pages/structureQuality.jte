@import ch.so.agi.datenportal.web.view.StructureQualityPageVm
@param StructureQualityPageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-detail-page dp-structure-quality-page">
    <div class="dp-container">
      <header class="dp-detail-hero dp-structure-hero">
        <h1 class="dp-detail-title">${page.title()}</h1>
      </header>
      <div class="dp-structure-content">
        @template.components.attributeTable(attributes = page.attributes())
        @if(page.dataModel().isPresent())
          @template.components.dataModelCard(model = page.dataModel().get())
        @endif
      </div>
    </div>
  </section>
`)
