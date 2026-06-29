@import ch.so.agi.datenportal.web.view.DataModelVm
@param DataModelVm model
<section class="dp-detail-panel dp-data-model-card" aria-labelledby="data-model-title">
  <h2 id="data-model-title">Datenmodell</h2>
  <dl class="dp-metadata-list">
    <div class="dp-metadata-item">
      <dt>Datenmodell</dt>
      <dd><a href="${model.modelHref()}">${model.modelName()}</a></dd>
    </div>
    <div class="dp-metadata-item">
      <dt>Validierungsreport</dt>
      <dd><a href="${model.validationReportHref()}">${model.validationReportName()}</a></dd>
    </div>
  </dl>
</section>
