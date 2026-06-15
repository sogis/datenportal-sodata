@import ch.so.agi.datenportal.web.view.DownloadSectionVm
@param DownloadSectionVm downloads
@if(!downloads.links().isEmpty())
  <section class="dp-detail-panel dp-detail-downloads" aria-labelledby="detail-downloads-title">
    <h2 id="detail-downloads-title">${downloads.title()}</h2>
    @if(!downloads.lead().isBlank())
      <p>${downloads.lead()}</p>
    @endif
    @template.components.downloadLink(downloads = downloads.links())
  </section>
@endif
