@import java.util.List
@import ch.so.agi.datenportal.web.view.DownloadLinkVm
@param List<DownloadLinkVm> downloads
<ul class="dp-download-list">
  @for(var download : downloads)
    <li>
      <a class="dp-download-link dp-action-pill" href="${download.href()}" aria-label="${download.ariaLabel()}">${download.label()}</a>
    </li>
  @endfor
</ul>
