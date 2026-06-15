# Web Components

Phase 6 integrates the canton chrome through the vendored npm package `so-web-components@0.1.9`.

## Runtime Assets

Assets are served by Spring Boot from:

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

The package was copied from npm package `so-web-components@0.1.9` with integrity:

```text
sha512-y9jqygazx5PVvSxDQWMp+JLV29ajZaJWP5UvCQOTL6WAMcBrocxUbHsira3/Hz8EliVlW1reixuVLDl45SD+BQ==
```

No CDN is required for the default application runtime.

## Configuration

```yaml
datenportal:
  web-components:
    enabled: true
    version: "0.1.9"
    asset-base-path: "/vendor/so-web-components/0.1.9"
    use-cdn: false
```

- `enabled=true` renders `<so-header>` and `<so-breadcrumb>` as the primary page chrome.
- `enabled=false` renders only semantic JTE fallback markup and does not include Web Component assets.
- `use-cdn=false` is the production-like default. `use-cdn=true` is only for local experiments with a pinned version.

## Templates

The page chrome is centralized in `src/main/jte/layouts/main.jte`.

- `components/chrome/webComponentsLoader.jte` includes Web Component JS/CSS once per full page.
- `components/chrome/soHeader.jte` renders `<so-header>` and a `noscript` fallback.
- `components/chrome/soBreadcrumb.jte` renders `<so-breadcrumb>` and a `noscript` fallback.
- `components/chrome/headerFallback.jte` and `components/chrome/breadcrumbFallback.jte` are used directly when Web Components are disabled.

HTMX fragments must not include layout chrome or asset tags.

## Fonts

The vendored `styles/fonts.css` is the stylesheet published by `so-web-components@0.1.9`. No additional font binaries are committed under `src/main/resources/static/assets/fonts/`.

Do not add placeholder font files. If maintainers provide separate licensed canton font files later, place them under `src/main/resources/static/assets/fonts/` and update `fonts.css` with web paths only, never absolute local paths.
