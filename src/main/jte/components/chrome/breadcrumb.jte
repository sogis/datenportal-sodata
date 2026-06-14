@import ch.so.agi.datenportal.web.view.BreadcrumbVm
@param BreadcrumbVm breadcrumb
@if(!breadcrumb.isEmpty())
  <nav class="dp-breadcrumb" aria-label="Breadcrumb">
    <div class="dp-container">
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
    </div>
  </nav>
@endif
