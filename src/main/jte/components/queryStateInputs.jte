@import ch.so.agi.datenportal.web.CatalogQueryParams
@param CatalogQueryParams queryParams
@param boolean includeQuery
@param boolean includeFilters
@param String excludeFilterParameter
@param boolean includeSort
@param boolean includeView
@param boolean includePage
@param boolean includeSize
@param boolean includeExpanded
@if(includeQuery && !queryParams.q().isBlank())
  <input type="hidden" name="q" value="${queryParams.q()}">
@endif
@if(includeFilters && !"theme".equals(excludeFilterParameter))
  @for(var value : queryParams.theme())
    <input type="hidden" name="theme" value="${value}">
  @endfor
@endif
@if(includeFilters && !"office".equals(excludeFilterParameter))
  @for(var value : queryParams.office())
    <input type="hidden" name="office" value="${value}">
  @endfor
@endif
@if(includeFilters && !"modified".equals(excludeFilterParameter))
  @for(var value : queryParams.modified())
    <input type="hidden" name="modified" value="${value}">
  @endfor
@endif
@if(includeSort)
  <input type="hidden" name="sort" value="${queryParams.sortValue()}">
@endif
@if(includeView)
  <input type="hidden" name="view" value="${queryParams.viewValue()}">
@endif
@if(includePage && queryParams.page() > 1)
  <input type="hidden" name="page" value="${queryParams.page()}">
@endif
@if(includeSize && queryParams.size() > 0)
  <input type="hidden" name="size" value="${queryParams.size()}">
@endif
@if(includeExpanded)
  @for(var value : queryParams.expanded())
    <input type="hidden" name="expanded" value="${value}">
  @endfor
@endif
