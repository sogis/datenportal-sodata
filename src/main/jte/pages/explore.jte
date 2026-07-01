@import ch.so.agi.datenportal.explore.ExplorePageVm
@param ExplorePageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-detail-page dp-explore-page">
    <div class="dp-container">
      <header class="dp-detail-hero dp-explore-hero">
        <p class="dp-detail-kicker">Erkunden</p>
        <h1 class="dp-detail-title">${page.title()} erkunden</h1>
        <p class="dp-detail-lead">Läuft lokal im Browser mit DuckDB-Wasm direkt auf den Parquet-Dateien.</p>
        <p class="dp-detail-meta">
          <a href="${page.canonicalDatasetUrl()}">Zur Datensatzseite</a>
        </p>
      </header>

      <div id="datenportal-explore-root" class="dp-detail-summary-layout dp-explore-placeholder">
        <div class="dp-detail-summary-main">
          @if(page.available())
            <section class="dp-metadata-section" aria-labelledby="explore-placeholder-title">
              <h2 id="explore-placeholder-title">SQL-Labor wird vorbereitet</h2>
              <p>Die interaktive Erkunden-Oberfläche wird in der nächsten Phase eingebunden. Die Metadaten für dieses Datenthema stehen bereits als JSON-Kontext bereit.</p>
              <noscript>
                <p>JavaScript ist deaktiviert. Für das spätere SQL-Labor wird JavaScript benötigt; die Downloads bleiben auf der Datensatzseite verfügbar.</p>
              </noscript>
            </section>
          @else
            <section class="dp-metadata-section" aria-labelledby="explore-unavailable-title">
              <h2 id="explore-unavailable-title">Erkunden nicht verfügbar</h2>
              <p>${page.unavailableReason()}</p>
              <p><a href="${page.canonicalDatasetUrl()}">Downloads und Metadaten auf der Datensatzseite anzeigen</a></p>
            </section>
          @endif
        </div>
      </div>

      <script id="datenportal-explore-context" type="application/json">$unsafe{page.contextJson()}</script>
      @if(page.assetLinks().hasStyle())
        <link rel="stylesheet" href="${page.assetLinks().styleSrc()}">
      @endif
      @if(page.assetLinks().hasScript())
        <script type="module" src="${page.assetLinks().scriptSrc()}"></script>
      @endif
    </div>
  </section>
`)
