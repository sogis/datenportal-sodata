@import ch.so.agi.datenportal.web.view.PageChromeVm
@import gg.jte.Content
@param PageChromeVm chrome
@param Content content
<!doctype html>
<html lang="de">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${chrome.pageTitle()}</title>
  <link rel="stylesheet" href="/css/app.css">
  <script src="/js/htmx.min.js" defer></script>
</head>
<body class="dp-page">
  <a class="dp-skip-link" href="#main-content">Zum Inhalt springen</a>
  @template.components.chrome.header(header = chrome.header())
  @template.components.chrome.breadcrumb(breadcrumb = chrome.breadcrumb())
  <main id="main-content" class="dp-main" tabindex="-1">
    ${content}
  </main>
  @template.components.footer()
</body>
</html>
