@import ch.so.agi.datenportal.web.view.FooterVm
@param FooterVm footer
<footer class="dp-footer">
  <div class="dp-container dp-footer__inner">
    <p>${footer.providerNotice()}</p>
    <p>${footer.buildLabel()}</p>
  </div>
</footer>
