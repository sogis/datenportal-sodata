@import ch.so.agi.datenportal.web.view.PageChromeVm
@param PageChromeVm chrome
@param String title
@param String message
@template.layouts.main(chrome = chrome, content = @`
  <section class="dp-detail-page">
    <div class="dp-container">
      <header class="dp-detail-hero">
        <h1 class="dp-detail-title">${title}</h1>
        <p class="dp-detail-description">${message}</p>
        <p><a class="dp-detail-action" href="/datasets">Zurück zu Daten &amp; Statistiken</a></p>
      </header>
    </div>
  </section>
`)
