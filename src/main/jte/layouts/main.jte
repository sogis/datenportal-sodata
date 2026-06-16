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
  @template.components.chrome.webComponentsLoader(assets = chrome.assets())
  <link rel="stylesheet" href="${chrome.assets().appCss()}">
  <script src="${chrome.assets().htmxJs()}" defer></script>
  <script src="${chrome.assets().appJs()}" defer></script>
</head>
<body class="dp-page">
  <a class="dp-skip-link" href="#main-content">Zum Inhalt springen</a>
  @template.components.chrome.soHeader(header = chrome.header(), webComponentsEnabled = chrome.assets().webComponentsEnabled())
  @template.components.chrome.soBreadcrumb(breadcrumb = chrome.breadcrumb(), webComponentsEnabled = chrome.assets().webComponentsEnabled())
  <main id="main-content" class="dp-main" tabindex="-1">
    ${content}
  </main>
  @template.components.footer()
</body>
</html>
