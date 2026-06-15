@import ch.so.agi.datenportal.web.view.ErrorPageVm
@param ErrorPageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-detail-page">
    <div class="dp-container">
      <header class="dp-detail-hero">
        <p class="dp-kicker">Fehler ${page.statusCode()}</p>
        <h1 class="dp-detail-title">${page.title()}</h1>
        <p class="dp-detail-description">${page.message()}</p>
        <p><a class="dp-detail-action" href="${page.actionHref()}">${page.actionLabel()}</a></p>
      </header>
    </div>
  </section>
`)
