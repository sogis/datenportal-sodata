@import ch.so.agi.datenportal.web.view.AttributeRowVm
@import java.util.List
@param List<AttributeRowVm> attributes
@param String emptyText
<section class="dp-structure-section" aria-labelledby="structure-attributes-title">
  <h2 id="structure-attributes-title">Attribute</h2>
  @if(attributes.isEmpty())
    <p class="dp-structure-empty">${emptyText}</p>
  @else
    <div class="dp-attribute-table-scroll">
      <table class="dp-attribute-table">
        <thead>
          <tr>
            <th scope="col">Attribut</th>
            <th scope="col">Typ</th>
            <th scope="col">Pflicht</th>
            <th scope="col">Einheit</th>
            <th scope="col">Beschreibung</th>
          </tr>
        </thead>
        <tbody>
          @for(var attribute : attributes)
            <tr>
              <th scope="row">${attribute.name()}</th>
              <td>${attribute.dataType()}</td>
              <td>${attribute.mandatoryLabel()}</td>
              <td>${attribute.unit()}</td>
              <td>${attribute.description()}</td>
            </tr>
          @endfor
        </tbody>
      </table>
    </div>
  @endif
</section>
