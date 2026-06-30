@import java.util.List
@import ch.so.agi.datenportal.web.view.CodeExampleVm
@param List<CodeExampleVm> examples
<section class="dp-usage-section dp-usage-code" aria-labelledby="usage-code-title">
  <h2 id="usage-code-title">Codebeispiele</h2>
  <div class="dp-usage-tabs" role="tablist" aria-label="Codebeispiele">
    @for(var example : examples)
      <button
        id="usage-tab-${example.id()}"
        class="dp-usage-tab${example.selected() ? " dp-usage-tab--active" : ""}"
        type="button"
        role="tab"
        aria-selected="${example.selected()}"
        aria-controls="usage-panel-${example.id()}"
        data-usage-tab="${example.id()}">
        ${example.label()}
      </button>
    @endfor
  </div>
  @for(var example : examples)
    <div
      id="usage-panel-${example.id()}"
      class="dp-usage-code-panel"
      role="tabpanel"
      aria-labelledby="usage-tab-${example.id()}"
      data-usage-panel="${example.id()}"
      hidden="${!example.selected()}">
      <div class="dp-usage-code-block">
        <pre><code>${example.code()}</code></pre>
        <button class="dp-usage-copy" type="button" data-copy-value="${example.code()}" data-copy-label="${example.label()}-Code kopieren" data-copy-success-label="${example.label()}-Code kopiert" aria-label="${example.label()}-Code kopieren">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-clipboard dp-copy-icon dp-copy-icon--idle" viewBox="0 0 16 16" aria-hidden="true">
            <path d="M4 1.5H3a2 2 0 0 0-2 2V14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V3.5a2 2 0 0 0-2-2h-1v1h1a1 1 0 0 1 1 1V14a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1V3.5a1 1 0 0 1 1-1h1z"/>
            <path d="M9.5 1a.5.5 0 0 1 .5.5v1A.5.5 0 0 1 9.5 3h-3a.5.5 0 0 1-.5-.5v-1a.5.5 0 0 1 .5-.5zm-3-1A1.5 1.5 0 0 0 5 1.5v1A1.5 1.5 0 0 0 6.5 4h3A1.5 1.5 0 0 0 11 2.5v-1A1.5 1.5 0 0 0 9.5 0z"/>
          </svg>
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-check-lg dp-copy-icon dp-copy-icon--success" viewBox="0 0 16 16" aria-hidden="true">
            <path d="M12.736 3.97a.733.733 0 0 1 1.047 0c.286.289.29.756.01 1.05L7.88 12.01a.733.733 0 0 1-1.065.02L3.217 8.384a.757.757 0 0 1 0-1.06.733.733 0 0 1 1.047 0l3.052 3.093 5.4-6.425z"/>
          </svg>
          <span class="dp-visually-hidden dp-copy-label dp-copy-label--idle">${example.label()}-Code kopieren</span>
          <span class="dp-visually-hidden dp-copy-label dp-copy-label--success">${example.label()}-Code kopiert</span>
        </button>
      </div>
    </div>
  @endfor
</section>
