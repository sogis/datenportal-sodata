@import ch.so.agi.datenportal.web.view.WebAssetsVm
@param WebAssetsVm assets
@for (var stylesheet : assets.webComponentStylesheets())
  <link rel="stylesheet" href="${stylesheet}">
@endfor
@if(assets.webComponentsIndexJs().isPresent())
  <script type="module" src="${assets.webComponentsIndexJs().get()}"></script>
@endif
