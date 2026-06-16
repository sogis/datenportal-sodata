@import ch.so.agi.datenportal.web.CatalogQueryParams
@param CatalogQueryParams queryParams
<form
  id="catalog-search-form"
  class="dp-search-form"
  method="get"
  action="/datasets"
  hx-get="/datasets"
  hx-target="#dataset-results-shell"
  hx-swap="outerHTML"
  hx-push-url="true"
  hx-indicator="#dataset-loading">
  <div class="dp-search">
    <label class="dp-visually-hidden" for="catalog-search">Suche</label>
    <input
      id="catalog-search"
      class="dp-search__input"
      type="search"
      name="q"
      value="${queryParams.q()}"
      placeholder="Suche nach Datensätzen, Themen, Fachstellen, Schlagworten ...">
    <button class="dp-search__button" type="submit" aria-label="Suche ausführen">Suchen</button>
  </div>
  @template.components.queryStateInputs(
    queryParams = queryParams,
    includeQuery = false,
    includeFilters = true,
    excludeFilterParameter = "",
    includeSort = true,
    includeView = true,
    includePage = false,
    includeSize = true,
    includeExpanded = false
  )
</form>
