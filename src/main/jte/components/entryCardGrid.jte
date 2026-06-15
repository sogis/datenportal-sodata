@import java.util.List
@import ch.so.agi.datenportal.web.view.CardResultVm
@param List<CardResultVm> cards
<div class="dp-card-grid">
  @for(var card : cards)
    @template.components.entryCard(card = card)
  @endfor
</div>
