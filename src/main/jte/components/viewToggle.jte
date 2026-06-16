@import ch.so.agi.datenportal.web.view.ResultsVm
@param ResultsVm results
<nav class="dp-view-toggle" aria-label="Ansicht wählen">
  <a
    class="dp-view-toggle__link${results.controls().cardsActive() ? " is-active" : ""}"
    href="${results.controls().cardsHref()}"
    hx-get="${results.controls().cardsHref()}"
    hx-target="#dataset-results-shell"
    hx-swap="outerHTML"
    hx-push-url="true"
    hx-indicator="#dataset-loading"
    aria-current="${results.controls().cardsActive() ? "page" : null}">Kartenansicht</a>
  <a
    class="dp-view-toggle__link${results.controls().listActive() ? " is-active" : ""}"
    href="${results.controls().listHref()}"
    hx-get="${results.controls().listHref()}"
    hx-target="#dataset-results-shell"
    hx-swap="outerHTML"
    hx-push-url="true"
    hx-indicator="#dataset-loading"
    aria-current="${results.controls().listActive() ? "page" : null}">Listenansicht</a>
</nav>
