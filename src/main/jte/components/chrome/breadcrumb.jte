@import ch.so.agi.datenportal.web.view.BreadcrumbVm
@param BreadcrumbVm breadcrumb
@if(!breadcrumb.isEmpty())
  <nav class="dp-breadcrumb" aria-label="Breadcrumb">
    <ol class="dp-breadcrumb__list">
      @for (var item : breadcrumb.items())
        <li class="dp-breadcrumb__item">
          @if(item.currentPage())
            <span aria-current="page">${item.label()}</span>
          @elseif(item.href().isPresent())
            <a href="${item.href().get()}">${item.label()}</a>
          @else
            <span>${item.label()}</span>
          @endif
        </li>
      @endfor
    </ol>
  </nav>
@endif
