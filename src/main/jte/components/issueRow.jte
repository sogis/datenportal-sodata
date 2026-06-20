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
    <a class="dp-metadata-link" href="${issue.detailHref()}" aria-label="Metadaten anzeigen: ${issue.title()}">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" class="bi bi-info-circle" viewBox="0 0 16 16" aria-hidden="true">
        <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14m0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16"/>
        <path d="m8.93 6.588-2.29.287-.082.38.45.083c.294.07.352.176.288.469l-.738 3.468c-.194.897.105 1.319.808 1.319.545 0 1.178-.252 1.465-.598l.088-.416c-.2.176-.492.246-.686.246-.275 0-.375-.193-.304-.533zM9 4.5a1 1 0 1 1-2 0 1 1 0 0 1 2 0"/>
      </svg>
    </a>
  </td>
  <td class="dp-entry-downloads">
    @template.components.downloadLink(downloads = issue.downloads())
  </td>
</tr>
