@import ch.so.agi.datenportal.web.view.ContactMetadataSectionVm
@param ContactMetadataSectionVm section
<section class="dp-detail-panel dp-metadata-section" id="metadata-${section.id()}" aria-labelledby="metadata-title-${section.id()}">
  <h2 id="metadata-title-${section.id()}">${section.title()}</h2>
  <dl class="dp-metadata-list">
    @for(var item : section.items())
      <div class="dp-metadata-item">
        <dt>${item.label()}</dt>
        <dd>
          @for(var i = 0; i < item.lines().size(); i++)
            @if(i > 0)
              <br>
            @endif
            @if(item.lines().get(i).href().isPresent())
              <a href="${item.lines().get(i).href().get()}" target="_blank">${item.lines().get(i).value()}</a>
            @else
              ${item.lines().get(i).value()}
            @endif
          @endfor
        </dd>
      </div>
    @endfor
  </dl>
</section>
