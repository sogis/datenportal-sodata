@import ch.so.agi.datenportal.web.view.CatalogPageVm
@param CatalogPageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-catalog-intro">
    <div class="dp-container">
      <h1 class="dp-page-title">${page.title()}</h1>
      <p class="dp-lead">${page.lead()}</p>
      <p class="dp-lead-secondary">${page.secondaryLead()}</p>
      <div id="dataset-results" class="dp-results-placeholder" role="status">
        <p>${page.placeholderMessage()}</p>
      </div>
    </div>
  </section>
`)
