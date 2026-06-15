# Web Components

Die Anwendung integriert den kantonalen Header und das Breadcrumb über das vendorte npm-Paket `so-web-components@0.1.9`.

## Runtime Assets

Assets werden von Spring Boot aus statischen Ressourcen ausgeliefert:

```text
src/main/resources/static/vendor/so-web-components/0.1.9/
  index.js
  components/
  styles/reset.css
  styles/fonts.css
  styles/tokens.css
  LICENSE
  README.md
```

Das Paket wurde aus `so-web-components@0.1.9` übernommen. Die App benötigt standardmässig kein CDN.

## Konfiguration

```yaml
datenportal:
  web-components:
    enabled: true
    version: "0.1.9"
    asset-base-path: "/vendor/so-web-components/0.1.9"
    use-cdn: false
```

- `enabled=true` rendert `<so-header>` und `<so-breadcrumb>` als primären Page Chrome.
- `enabled=false` rendert nur semantische JTE-Fallbacks.
- `use-cdn=false` ist der produktionsnahe Standard.
- `use-cdn=true` ist nur für lokale Experimente mit gepinnter Version vorgesehen.

## Templates

Der Page Chrome ist in `src/main/jte/layouts/main.jte` zentralisiert.

- `components/chrome/webComponentsLoader.jte` lädt JS/CSS nur auf vollständigen Seiten.
- `components/chrome/soHeader.jte` rendert `<so-header>` und einen `noscript`-Fallback.
- `components/chrome/soBreadcrumb.jte` rendert `<so-breadcrumb>` und einen `noscript`-Fallback.
- `components/chrome/headerFallback.jte` und `components/chrome/breadcrumbFallback.jte` werden direkt verwendet, wenn Web Components deaktiviert sind.

HTMX-Fragmente enthalten keinen Page Chrome und keine Asset-Tags.

## Caching

`/vendor/so-web-components/**` wird mit langer TTL ausgeliefert:

```text
Cache-Control: public, max-age=31536000
```

Das ist akzeptabel, weil die Pfade die Web-Component-Version enthalten. Bei einem Versionswechsel wird der Pfad über `datenportal.web-components.version` beziehungsweise `asset-base-path` geändert.

## Fonts

Die vendorte `styles/fonts.css` stammt aus `so-web-components@0.1.9`. Aktuell werden keine zusätzlichen Font-Binaries unter `src/main/resources/static/assets/fonts/` committed.

Keine Platzhalter-Fontdateien hinzufügen. Falls Maintainer lizenzierte kantonale Fontdateien bereitstellen, werden sie unter `src/main/resources/static/assets/fonts/` abgelegt und `fonts.css` verwendet Web-Pfade, nie absolute lokale Pfade.

## Content Security Policy

Die globale CSP erlaubt Skripte und Assets von `self`. `style-src` erlaubt zusätzlich Inline-Styles, weil die aktuellen Web Components Shadow-DOM-Styles erzeugen. Externe CDN-Assets sollten produktiv nicht verwendet werden.
