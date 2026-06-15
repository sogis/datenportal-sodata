@import ch.so.agi.datenportal.web.CatalogQueryParams
@param CatalogQueryParams queryParams
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
