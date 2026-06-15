@import java.util.List
@import ch.so.agi.datenportal.web.view.ResultItemVm
@param List<ResultItemVm> rows
<div class="dp-entry-table-wrapper">
  <table class="dp-entry-table">
    <thead>
      <tr>
        <th class="dp-entry-table__expand" scope="col"><span class="dp-visually-hidden">Ausgaben</span></th>
        <th scope="col">Thema / Datensatz</th>
        <th scope="col">Typ</th>
        <th scope="col">Publikationsdatum</th>
        <th scope="col">Metadaten</th>
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
