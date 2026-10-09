@import ch.so.agi.datenportal.web.view.HeaderVm
@import ch.so.agi.datenportal.web.view.BreadcrumbVm
@param HeaderVm header
@param BreadcrumbVm breadcrumb
<header class="dp-print-header dp-print-only">
  <p class="dp-print-header__brand">${header.siteName()} · ${header.siteClaim()}</p>
  @if(!breadcrumb.isEmpty())
    <nav aria-label="Seitenpfad im Ausdruck">
      <ol class="dp-print-header__breadcrumb">
        @for(var item : breadcrumb.items())
          <li>${item.label()}</li>
        @endfor
      </ol>
    </nav>
  @endif
</header>
