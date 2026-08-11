@import ch.so.agi.datenportal.web.view.QualityVm
@param QualityVm quality
<section class="dp-quality-card" aria-labelledby="quality-title">
  <h2 id="quality-title">Qualität</h2>
  @if(quality.missingModelMessage().isPresent())
    <p class="dp-quality-card__empty">${quality.missingModelMessage().get()}</p>
  @endif
  @if(quality.modelName().isPresent() || quality.validationReportName().isPresent())
    <dl class="dp-metadata-list">
      @if(quality.modelName().isPresent())
        <div class="dp-metadata-item">
          <dt>Datenmodell</dt>
          <dd>${quality.modelName().get()}</dd>
        </div>
      @endif
      @if(quality.validationReportName().isPresent() && quality.validationReportHref().isPresent())
        <div class="dp-metadata-item">
          <dt>Validierungsreport</dt>
          <dd><a href="${quality.validationReportHref().get()}">${quality.validationReportName().get()}</a></dd>
        </div>
      @endif
    </dl>
  @endif
</section>
