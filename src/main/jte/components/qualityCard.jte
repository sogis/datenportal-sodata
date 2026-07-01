@import ch.so.agi.datenportal.web.view.QualityVm
@param QualityVm quality
<section class="dp-quality-card" aria-labelledby="quality-title">
  <h2 id="quality-title">Qualität</h2>
  @if(quality.modelName().isPresent())
    <dl class="dp-metadata-list">
      <div class="dp-metadata-item">
        <dt>Datenmodell</dt>
        <dd><a href="${quality.modelHref()}">${quality.modelName().get()}</a></dd>
      </div>
      <div class="dp-metadata-item">
        <dt>Validierungsreport</dt>
        <dd><a href="${quality.validationReportHref()}">${quality.validationReportName()}</a></dd>
      </div>
    </dl>
  @else
    <p class="dp-quality-card__empty">${quality.missingModelMessage().orElse("")}</p>
  @endif
</section>
