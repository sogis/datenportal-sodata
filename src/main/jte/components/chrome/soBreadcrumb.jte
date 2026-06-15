@import ch.so.agi.datenportal.web.view.BreadcrumbVm
@param BreadcrumbVm breadcrumb
@param boolean webComponentsEnabled
@if(!breadcrumb.isEmpty())
  @if(webComponentsEnabled)
    <so-breadcrumb>
      @for (var item : breadcrumb.items())
        @if(item.currentPage())
          <so-breadcrumb-item iscurrentpage>${item.label()}</so-breadcrumb-item>
        @elseif(item.href().isPresent())
          <so-breadcrumb-item href="${item.href().get()}">${item.label()}</so-breadcrumb-item>
        @else
          <so-breadcrumb-item>${item.label()}</so-breadcrumb-item>
        @endif
      @endfor
    </so-breadcrumb>
    <noscript>
      @template.components.chrome.breadcrumbFallback(breadcrumb = breadcrumb)
    </noscript>
  @else
    @template.components.chrome.breadcrumbFallback(breadcrumb = breadcrumb)
  @endif
@endif
