@import ch.so.agi.datenportal.web.view.CatalogPageVm
@param CatalogPageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-catalog-intro">
    <div class="dp-container">
      <h1 class="dp-page-title">${page.title()}</h1>
      <p class="dp-lead">${page.lead()}</p>
      <p class="dp-lead-secondary">${page.secondaryLead()}</p>
      <div id="dataset-results" class="dp-results">
        <p class="dp-results-summary">${page.entries().size()} Einträge aus dem statischen Entwicklungskatalog.</p>
        @if(page.hasEntries())
          <div class="dp-entry-table-wrapper">
            <table class="dp-entry-table">
              <thead>
                <tr>
                  <th scope="col">Datensatz</th>
                  <th scope="col">Typ</th>
                  <th scope="col">Fachstelle</th>
                  <th scope="col">Publikationsdatum</th>
                  <th scope="col">Downloads</th>
                </tr>
              </thead>
              <tbody>
                @for(var entry : page.entries())
                  <tr>
                    <td>
                      <p class="dp-entry-title">${entry.title()}</p>
                      <p class="dp-entry-description">${entry.description()}</p>
                      @if(entry.currentIssueLabel().isPresent())
                        <p class="dp-entry-current-issue">Aktuelle Ausgabe: ${entry.currentIssueLabel().get()}</p>
                      @endif
                    </td>
                    <td>
                      <span class="dp-type-badge">${entry.typeLabel()}</span>
                    </td>
                    <td>${entry.officeLabel()}</td>
                    <td>${entry.modifiedLabel()}</td>
                    <td>
                      <ul class="dp-download-list">
                        @for(var download : entry.downloads())
                          <li>
                            <a class="dp-download-link" href="${download.href()}" aria-label="${download.ariaLabel()}">${download.label()}</a>
                          </li>
                        @endfor
                      </ul>
                    </td>
                  </tr>
                @endfor
              </tbody>
            </table>
          </div>
        @else
          <p class="dp-results-empty">Keine Einträge verfügbar.</p>
        @endif
      </div>
    </div>
  </section>
`)
