@import ch.so.agi.datenportal.web.view.HeaderVm
@param HeaderVm header
@param boolean webComponentsEnabled
@if(webComponentsEnabled)
  <so-header
    top-nav='${header.utilityNavJson()}'
    section-nav='${header.primaryNavJson()}'
    active-section="${header.activeSection()}"
    logo-href="${header.logoHref()}"
    site-name="${header.siteName()}"
    config='${header.componentConfigJson()}'>
  </so-header>
  <noscript>
    @template.components.chrome.headerFallback(header = header)
  </noscript>
@else
  @template.components.chrome.headerFallback(header = header)
@endif
