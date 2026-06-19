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
    <span class="dp-search__icon" aria-hidden="true">
      <svg xmlns="http://www.w3.org/2000/svg" width="1.2rem" height="1.2rem" viewBox="0 0 16 16" fill="currentColor">
        <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001q.044.06.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1 1 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0"/>
      </svg>
    </span>
    <input
      id="catalog-search"
      class="dp-search__input"
      type="text"
      name="q"
      value="${queryParams.q()}"
      autocomplete="off"
      placeholder="Suche nach Datensätzen, Themen, Fachstellen, Schlagworten ...">
    <button
      id="catalog-search-clear"
      class="dp-search__clear"
      type="button"
      aria-label="Suche zurücksetzen">
      <svg xmlns="http://www.w3.org/2000/svg" width="1.2rem" height="1.2rem" viewBox="0 0 16 16" fill="currentColor">
        <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708"/>
      </svg>
    </button>
  </div>
  @template.components.queryStateInputs(
    queryParams = queryParams,
    includeQuery = false,
    includeFilters = true,
    excludeFilterParameter = "",
    includeSort = true,
    includeView = true,
    includeExpanded = false
  )
</form>
