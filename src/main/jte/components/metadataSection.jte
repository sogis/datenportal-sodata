@import ch.so.agi.datenportal.web.view.MetadataSectionVm
@param MetadataSectionVm section
<section class="dp-detail-panel dp-metadata-section" id="metadata-${section.id()}" aria-labelledby="metadata-title-${section.id()}">
  <h2 id="metadata-title-${section.id()}">${section.title()}</h2>
  <dl class="dp-metadata-list">
    @for(var item : section.items())
      <div class="dp-metadata-item">
        <dt>${item.label()}</dt>
        <dd>
          @if(item.href().isPresent())
            <a href="${item.href().get()}">${item.value()}</a>
          @else
            ${item.value()}
          @endif
        </dd>
      </div>
    @endfor
  </dl>
</section>
