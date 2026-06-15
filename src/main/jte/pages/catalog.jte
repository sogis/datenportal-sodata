@import ch.so.agi.datenportal.web.view.CatalogPageVm
@param CatalogPageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-catalog-intro">
    <div class="dp-container">
      <h1 class="dp-page-title">${page.title()}</h1>
      <p class="dp-lead">${page.lead()}</p>
      <p class="dp-lead-secondary">${page.secondaryLead()}</p>

      <form
        class="dp-catalog-form"
        method="get"
        action="/datasets"
        hx-get="/datasets"
        hx-target="#dataset-results"
        hx-select="#dataset-results"
        hx-push-url="true">
        @template.components.searchForm(queryParams = page.queryParams())
        @template.components.filterBar(filterPanel = page.filterPanel())
        <input type="hidden" name="view" value="${page.queryParams().viewValue()}">
        <input type="hidden" name="sort" value="${page.queryParams().sortValue()}">
        @for(var expanded : page.queryParams().expanded())
          <input type="hidden" name="expanded" value="${expanded}">
        @endfor
        <div class="dp-filter-actions">
          <button class="dp-button dp-button--primary" type="submit">Anwenden</button>
          <a class="dp-button dp-button--secondary" href="${page.filterPanel().resetHref()}">Filter zurücksetzen</a>
        </div>
      </form>

      @template.fragments.catalogResults(
        results = page.results(),
        filterPanel = page.filterPanel(),
        queryParams = page.queryParams()
      )
    </div>
  </section>
`)
