@import ch.so.agi.datenportal.web.view.IssueRowVm
@param IssueRowVm issue
<tr class="dp-entry-row dp-issue-row">
  <td class="dp-entry-table__expand"></td>
  <td class="dp-entry-summary">
    <p class="dp-entry-title">${issue.title()}</p>
    <p class="dp-entry-description">${issue.description()}</p>
  </td>
  <td class="dp-entry-publication-date">${issue.publicationDateLabel()}</td>
  <td>
    <a class="dp-metadata-link" href="${issue.detailHref()}" aria-label="Metadaten anzeigen: ${issue.title()}">i</a>
  </td>
  <td class="dp-entry-downloads">
    @template.components.downloadLink(downloads = issue.downloads())
  </td>
</tr>
