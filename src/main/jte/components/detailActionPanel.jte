@param String structureQualityOriginHref
@param String structureQualityOriginLabel
@param String usageHref
@param boolean openData
<section class="dp-detail-panel dp-detail-actions" aria-labelledby="detail-actions-title">
  <h2 id="detail-actions-title">Daten nutzen</h2>
  <div class="dp-detail-action-list">
    <article class="dp-detail-action-item">
      <div class="dp-detail-action-item__icon" aria-hidden="true">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" class="bi bi-shield-check" viewBox="0 0 16 16">
          <path d="M5.338 1.59a61 61 0 0 0-2.837.856.48.48 0 0 0-.328.39c-.554 4.157.726 7.19 2.253 9.188a10.7 10.7 0 0 0 2.287 2.233c.346.244.652.42.893.533q.18.085.293.118a1 1 0 0 0 .101.025 1 1 0 0 0 .1-.025q.114-.034.294-.118c.24-.113.547-.29.893-.533a10.7 10.7 0 0 0 2.287-2.233c1.527-1.997 2.807-5.031 2.253-9.188a.48.48 0 0 0-.328-.39c-.651-.213-1.75-.56-2.837-.855C9.552 1.29 8.531 1.067 8 1.067c-.53 0-1.552.223-2.662.524zM5.072.56C6.157.265 7.31 0 8 0s1.843.265 2.928.56c1.11.3 2.229.655 2.887.87a1.54 1.54 0 0 1 1.044 1.262c.596 4.477-.787 7.795-2.465 9.99a11.8 11.8 0 0 1-2.517 2.453 7 7 0 0 1-1.048.625c-.28.132-.581.24-.829.24s-.548-.108-.829-.24a7 7 0 0 1-1.048-.625 11.8 11.8 0 0 1-2.517-2.453C1.928 10.487.545 7.169 1.141 2.692A1.54 1.54 0 0 1 2.185 1.43 63 63 0 0 1 5.072.56"/>
          <path d="M10.854 5.146a.5.5 0 0 1 0 .708l-3 3a.5.5 0 0 1-.708 0l-1.5-1.5a.5.5 0 1 1 .708-.708L7.5 7.793l2.646-2.647a.5.5 0 0 1 .708 0"/>
        </svg>
      </div>
      <div class="dp-detail-action-item__content">
        <h3>${structureQualityOriginLabel}</h3>
        <p>Attribute, Datenmodell, Validierung und Qualitätshinweise einsehen.</p>
        <a class="dp-detail-action-link" href="${structureQualityOriginHref}" aria-label="Struktur, Qualität und Herkunft anzeigen">
          Struktur, Qualität und Herkunft anzeigen <span aria-hidden="true">&rarr;</span>
        </a>
      </div>
    </article>
    <article class="${openData ? "dp-detail-action-item" : "dp-detail-action-item dp-detail-action-item--disabled"}">
      <div class="dp-detail-action-item__icon" aria-hidden="true">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" class="bi bi-search" viewBox="0 0 16 16">
          <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001q.044.06.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1 1 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0"/>
        </svg>
      </div>
      <div class="dp-detail-action-item__content">
        <h3>Erkunden</h3>
        <p>Datenvorschau öffnen und Inhalte fachlich einordnen.</p>
        @if(openData)
          <a class="dp-detail-action-link" href="#" aria-label="Datenvorschau anzeigen: Daten erkunden">
            Datenvorschau anzeigen <span aria-hidden="true">&rarr;</span>
          </a>
        @else
          <span class="dp-detail-action-link dp-detail-action-link--disabled" aria-disabled="true">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-lock dp-detail-action-lock" viewBox="0 0 16 16" aria-hidden="true">
              <path fill-rule="evenodd" d="M8 0a4 4 0 0 1 4 4v2.05a2.5 2.5 0 0 1 2 2.45v5a2.5 2.5 0 0 1-2.5 2.5h-7A2.5 2.5 0 0 1 2 13.5v-5a2.5 2.5 0 0 1 2-2.45V4a4 4 0 0 1 4-4M4.5 7A1.5 1.5 0 0 0 3 8.5v5A1.5 1.5 0 0 0 4.5 15h7a1.5 1.5 0 0 0 1.5-1.5v-5A1.5 1.5 0 0 0 11.5 7zM8 1a3 3 0 0 0-3 3v2h6V4a3 3 0 0 0-3-3"/>
            </svg>
            <span class="dp-visually-hidden">Datenvorschau nicht verfügbar</span>
          </span>
        @endif
      </div>
    </article>
    <article class="${openData ? "dp-detail-action-item" : "dp-detail-action-item dp-detail-action-item--disabled"}">
      <div class="dp-detail-action-item__icon" aria-hidden="true">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" class="bi bi-code-slash" viewBox="0 0 16 16">
          <path d="M10.478 1.647a.5.5 0 1 0-.956-.294l-4 13a.5.5 0 0 0 .956.294zM4.854 4.146a.5.5 0 0 1 0 .708L1.707 8l3.147 3.146a.5.5 0 0 1-.708.708l-3.5-3.5a.5.5 0 0 1 0-.708l3.5-3.5a.5.5 0 0 1 .708 0m6.292 0a.5.5 0 0 0 0 .708L14.293 8l-3.147 3.146a.5.5 0 0 0 .708.708l3.5-3.5a.5.5 0 0 0 0-.708l-3.5-3.5a.5.5 0 0 0-.708 0"/>
        </svg>
      </div>
      <div class="dp-detail-action-item__content">
        <h3>Verwenden</h3>
        <p>Downloads, Formate und Codebeispiele anzeigen.</p>
        @if(openData)
          <a class="dp-detail-action-link" href="${usageHref}" aria-label="Downloads anzeigen: Daten verwenden">
            Downloads anzeigen <span aria-hidden="true">&rarr;</span>
          </a>
        @else
          <span class="dp-detail-action-link dp-detail-action-link--disabled" aria-disabled="true">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-lock dp-detail-action-lock" viewBox="0 0 16 16" aria-hidden="true">
              <path fill-rule="evenodd" d="M8 0a4 4 0 0 1 4 4v2.05a2.5 2.5 0 0 1 2 2.45v5a2.5 2.5 0 0 1-2.5 2.5h-7A2.5 2.5 0 0 1 2 13.5v-5a2.5 2.5 0 0 1 2-2.45V4a4 4 0 0 1 4-4M4.5 7A1.5 1.5 0 0 0 3 8.5v5A1.5 1.5 0 0 0 4.5 15h7a1.5 1.5 0 0 0 1.5-1.5v-5A1.5 1.5 0 0 0 11.5 7zM8 1a3 3 0 0 0-3 3v2h6V4a3 3 0 0 0-3-3"/>
            </svg>
            <span class="dp-visually-hidden">Downloads nicht verfügbar</span>
          </span>
        @endif
      </div>
    </article>
  </div>
</section>
