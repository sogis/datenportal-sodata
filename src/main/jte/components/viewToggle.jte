@import ch.so.agi.datenportal.web.view.ResultsVm
@param ResultsVm results
<nav class="dp-view-toggle" aria-label="Ansicht wählen">
  <a class="dp-view-toggle__link${results.controls().cardsActive() ? " is-active" : ""}" href="${results.controls().cardsHref()}" aria-current="${results.controls().cardsActive() ? "page" : null}">Kartenansicht</a>
  <a class="dp-view-toggle__link${results.controls().listActive() ? " is-active" : ""}" href="${results.controls().listHref()}" aria-current="${results.controls().listActive() ? "page" : null}">Listenansicht</a>
</nav>
