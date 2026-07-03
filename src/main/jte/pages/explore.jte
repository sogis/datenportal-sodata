@import ch.so.agi.datenportal.explore.ExplorePageVm
@param ExplorePageVm page
@template.layouts.explore(chrome = page.chrome(), content = @`
  <section class="dp-explore-host" aria-label="Erkunden">
    <div id="datenportal-explore-root" class="dp-explore-placeholder">
      @if(page.available())
        <section class="dp-explore-loading" aria-labelledby="explore-placeholder-title">
          <h1 id="explore-placeholder-title">SQL-Labor lädt</h1>
          <p>Das SQL-Labor läuft lokal im Browser mit DuckDB-Wasm.</p>
          <noscript>
            <p>JavaScript ist deaktiviert. Für das SQL-Labor wird JavaScript benötigt; die Downloads bleiben auf der Datensatzseite verfügbar.</p>
          </noscript>
        </section>
      @else
        <section class="dp-explore-loading" aria-labelledby="explore-unavailable-title">
          <h1 id="explore-unavailable-title">Erkunden nicht verfügbar</h1>
          <p>${page.unavailableReason()}</p>
          <p><a href="${page.canonicalDatasetUrl()}">Downloads und Metadaten auf der Datensatzseite anzeigen</a></p>
        </section>
      @endif
    </div>

    <script id="datenportal-explore-context" type="application/json">$unsafe{page.contextJson()}</script>
    @if(page.assetLinks().hasStyle())
      <link rel="stylesheet" href="${page.assetLinks().styleSrc()}">
    @endif
    @if(page.assetLinks().hasScript())
      <script type="module" src="${page.assetLinks().scriptSrc()}"></script>
    @endif
  </section>
`)
