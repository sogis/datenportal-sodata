@import ch.so.agi.datenportal.web.view.DatasetDetailPageVm
@param DatasetDetailPageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-detail-page">
    <div class="dp-container">
      @template.components.detailHero(
        title = page.title(),
        description = page.description(),
        typeLabel = page.typeLabel(),
        openData = page.openData(),
        structureDescribed = page.structureDescribed(),
        modifiedLabel = page.modifiedLabel(),
        issuedLabel = page.issuedLabel()
      )
      <div class="dp-detail-layout">
        <div class="dp-detail-main">
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
