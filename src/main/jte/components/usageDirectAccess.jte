@import java.util.List
@import ch.so.agi.datenportal.web.view.DirectAccessRowVm
@param List<DirectAccessRowVm> rows
<section class="dp-usage-section" aria-labelledby="usage-direct-access-title">
  <h2 id="usage-direct-access-title">Direktzugriff</h2>
  @if(rows.isEmpty())
    <p class="dp-usage-empty">Für diesen Eintrag sind keine Open-Data-Downloads verfügbar.</p>
  @else
    <div class="dp-attribute-table-scroll dp-usage-direct-scroll">
      <table class="dp-attribute-table dp-usage-direct-table">
        <colgroup>
          <col class="dp-usage-direct-table__format">
          <col class="dp-usage-direct-table__description">
          <col class="dp-usage-direct-table__action">
        </colgroup>
        <thead>
          <tr>
            <th scope="col">Format</th>
            <th scope="col">Beschreibung</th>
            <th scope="col">Aktion</th>
          </tr>
        </thead>
        <tbody>
          @for(var row : rows)
            <tr>
              <th scope="row"><span class="dp-usage-format-badge">${row.format()}</span></th>
              <td>${row.description()}</td>
              <td>
                <div class="dp-usage-actions">
                  <a class="dp-usage-action" href="${row.href()}" aria-label="${row.ariaLabel()}">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-download" viewBox="0 0 16 16" aria-hidden="true">
                      <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5"/>
                      <path d="M7.646 11.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 10.293V1.5a.5.5 0 0 0-1 0v8.793L5.354 8.146a.5.5 0 1 0-.708.708z"/>
                    </svg>
                    <span>Herunterladen</span>
                  </a>
                  <button class="dp-usage-action dp-usage-copy-action" type="button" data-copy-value="${row.href()}" data-copy-label="URL kopieren: ${row.format()}" data-copy-success-label="URL kopiert: ${row.format()}" aria-label="URL kopieren: ${row.format()}">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-clipboard dp-copy-icon dp-copy-icon--idle" viewBox="0 0 16 16" aria-hidden="true">
                      <path d="M4 1.5H3a2 2 0 0 0-2 2V14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V3.5a2 2 0 0 0-2-2h-1v1h1a1 1 0 0 1 1 1V14a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1V3.5a1 1 0 0 1 1-1h1z"/>
                      <path d="M9.5 1a.5.5 0 0 1 .5.5v1A.5.5 0 0 1 9.5 3h-3a.5.5 0 0 1-.5-.5v-1a.5.5 0 0 1 .5-.5zm-3-1A1.5 1.5 0 0 0 5 1.5v1A1.5 1.5 0 0 0 6.5 4h3A1.5 1.5 0 0 0 11 2.5v-1A1.5 1.5 0 0 0 9.5 0z"/>
                    </svg>
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-check-lg dp-copy-icon dp-copy-icon--success" viewBox="0 0 16 16" aria-hidden="true">
                      <path d="M12.736 3.97a.733.733 0 0 1 1.047 0c.286.289.29.756.01 1.05L7.88 12.01a.733.733 0 0 1-1.065.02L3.217 8.384a.757.757 0 0 1 0-1.06.733.733 0 0 1 1.047 0l3.052 3.093 5.4-6.425z"/>
                    </svg>
                    <span class="dp-copy-label dp-copy-label--idle">URL kopieren</span>
                    <span class="dp-copy-label dp-copy-label--success">URL kopiert</span>
                  </button>
                </div>
              </td>
            </tr>
          @endfor
        </tbody>
      </table>
    </div>
  @endif
</section>
