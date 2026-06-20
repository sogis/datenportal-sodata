@import java.util.List
@import ch.so.agi.datenportal.web.view.ResultItemVm
@param List<ResultItemVm> rows
<div class="dp-entry-table-wrapper">
  <table class="dp-entry-table">
    <colgroup>
      <col class="dp-entry-table__col-expand">
      <col class="dp-entry-table__col-summary">
      <col class="dp-entry-table__col-date">
      <col class="dp-entry-table__col-metadata">
      <col class="dp-entry-table__col-downloads">
    </colgroup>
    <thead>
      <tr>
        <th class="dp-entry-table__expand" scope="col"><span class="dp-visually-hidden">Ausgaben</span></th>
        <th scope="col">Thema / Datensatz</th>
        <th scope="col">Publiziert</th>
        <th scope="col">Details</th>
        <th scope="col">Daten herunterladen</th>
      </tr>
    </thead>
    @for(var row : rows)
      <tbody>
        @template.components.entryRow(row = row)
      </tbody>
      @if(row.expanded())
        <tbody id="issues-${row.id()}" class="dp-issue-group">
          @for(var issue : row.issueRows())
            @template.components.issueRow(issue = issue)
          @endfor
        </tbody>
      @endif
    @endfor
  </table>
</div>
