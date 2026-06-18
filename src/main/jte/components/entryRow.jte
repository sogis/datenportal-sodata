@import ch.so.agi.datenportal.web.view.ResultItemVm
@param ResultItemVm row
<tr class="${row.series() ? "dp-entry-row dp-entry-row--series" : "dp-entry-row"}">
  <td class="dp-entry-table__expand">
    @if(row.series())
      <a
        class="dp-expand-button"
        href="${row.expandHref()}"
        hx-get="${row.expandHref()}"
        hx-target="#dataset-results-shell"
        hx-swap="outerHTML"
        hx-push-url="true"
        hx-indicator="#dataset-loading"
        role="button"
        aria-expanded="${row.expanded() ? "true" : "false"}"
        aria-controls="issues-${row.id()}"
        aria-label="${row.expandLabel()}: ${row.title()}">
        <span aria-hidden="true">${row.expanded() ? "-" : "+"}</span>
      </a>
    @endif
  </td>
  <td class="dp-entry-summary">
    <p class="dp-entry-title">${row.title()}</p>
    <p class="dp-entry-description">${row.description()}</p>
  </td>
  <td class="dp-entry-publication-date">${row.publicationDateLabel()}</td>
  <td>
    <a class="dp-metadata-link" href="${row.detailHref()}" aria-label="Metadaten anzeigen: ${row.title()}">i</a>
  </td>
  <td class="dp-entry-downloads">
    @template.components.downloadLink(downloads = row.downloads())
  </td>
</tr>
