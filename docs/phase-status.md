# Code-Quality Remediation: Phase Status

Autoritative Statusübersicht für `docs/code-quality-remediation-agent-spec.md`.
Die Phasen werden strikt in der Reihenfolge 1 bis 8 abgeschlossen. Jede Phase
hat einen eigenen Commit; es wird nichts gepusht.

## Phase 1 — Runtime-Lifecycle und Cancellation

Status: abgeschlossen am 2026-08-11.

Schwerpunkte: kontrolliertes DuckDB-/WebR-Cleanup, Abort-Signale,
veraltete React- und WebR-Operationen, Query-Cancellation, Monaco-Ready-Zustand
und deterministischer Autocomplete-Smoke.

Verifikation:

- fokussierte Vitest-Suite: PASS, 6 Dateien / 45 Tests
- vollständige Vitest-Suite: PASS, 23 Dateien / 124 Tests
- `npm --prefix src/main/frontend/explore run typecheck`: PASS
- `npm --prefix src/main/frontend/explore run build`: PASS
- `git diff --check`: PASS
- `./gradlew clean check`: PASS, einschließlich Frontend, Backend und Playwright
- realer WebR-Playwright-Smoke: PASS

Commit: `c8a895e`

## Phase 2 — Suchfehler und vollständige Treffermenge

Status: abgeschlossen am 2026-08-11.

Schwerpunkte: Lucene-Fehler als sichtbarer 503 statt künstlicher Leermenge,
vollständige Treffermenge vor Java-Filtern, Snapshot-Lock-Nutzung und
transparente Index-Dokumentzahl.

Verifikation:

- fokussierte Gradle-Suite: PASS, 41 Tests
- `git diff --check`: PASS
- `./gradlew clean check`: PASS, `BUILD SUCCESSFUL in 57s`

Commit: `6b0db07`

## Phase 3 — Produktionssichere Konfiguration

Status: abgeschlossen am 2026-08-11.

Schwerpunkte: fail-fast Quellenbindung, explizite `local`-/`test`-Profile,
produktionssichere JTE- und Health-Defaults, keine Fixture-/localhost-
Fallbacks und keine internen Health-Details in der öffentlichen Antwort.

Verifikation:

- Konfigurations-, Profil-, Actuator- und Starttests: PASS
- fünf fail-fast Binding-Fälle in `ConfigurationStartupTest`: PASS
- `git diff --check`: PASS
- `./gradlew clean check`: PASS, `BUILD SUCCESSFUL in 55s`

Commit: `95c57b8`

## Phase 4 — UI-Vertrag und Interaktion

Status: abgeschlossen am 2026-08-11.

Schwerpunkte: fachliches Struktur-Badge-Mapping, deklarative Serienzeilen-
Expansion über das bestehende Disclosure-Element, Schutz interaktiver
Nachfahren und echte Qualitätsreport-Links ohne Platzhalter-URLs.

Verifikation:

- fokussierte MVC-/Factory-Suite: PASS, 74 Tests
- gezielter Serienzeilen-Playwright-Test: PASS
- `git diff --check`: PASS
- `./gradlew clean check`: PASS, `BUILD SUCCESSFUL in 55s`

Commit: `c57dbd8`

## Phase 5 — ViewModel- und Template-Konsolidierung

Status: abgeschlossen am 2026-08-11.

Schwerpunkte: ungenutzte Server-ViewModels, alte Detail-Templates, die
vorbereiteten Starter-Rezepte und die zugehörigen PNG-Assets entfernen;
produktive SQL-Rezepte und das R-Labor bleiben erhalten.

Verifikation:

- `./gradlew test`: PASS, 271 Tests
- `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.web.CatalogFiltersPlaywrightTest'`: PASS, 24 Tests
- Referenzprüfung auf entfernte Typen/Templates/Assets: PASS
- exakt 34 Java-ViewModels im konsolidierten View-Paket: PASS
- `git diff --check`: PASS
- `./gradlew clean check`: PASS, `BUILD SUCCESSFUL in 57s`

Commit: `7a1e744`

## Phase 6 — Atomarer Katalog-Snapshot und Artefakte

Status: abgeschlossen am 2026-08-11.

Schwerpunkte: gemeinsam geladene XTF-/DuckDB-Artefakte, atomare Snapshot-
Veröffentlichung mit Lucene, Snapshot-only-Artefakt-Requests, Hash-
Versionierung, ETag/304/409, Content-Length und derselbe Stand im Explore-
Kontext.

Verifikation:

- Katalog-, Reload-, Artefakt-, Explore- und Health-Suites: PASS
- `git diff --check`: PASS
- `./gradlew clean check`: PASS, `BUILD SUCCESSFUL in 1m 46s`

Commit: `f747965`

## Phase 7 — Bundle- und JAR-Größe

Status: abgeschlossen am 2026-08-11.

Schwerpunkte: ausschließlich MVP-DuckDB-Bundle, keine EH-/COI- oder
Source-Map-Artefakte im Boot-JAR und lazy geladene R-Komponenten erst nach
Aktivierung des R-Labors.

Verifikation:

- Vitest: PASS, 23 Dateien / 125 Tests
- TypeScript: PASS
- realer WebR-Playwright-Smoke: PASS
- `./gradlew clean bootJar`: PASS
- JAR-Inhaltsprüfung: PASS, nur MVP-DuckDB/R-Chunks, keine EH/COI/Maps
- JAR-Größe: 174.656.030 Bytes, unter 180 MiB; Reduktion 54.323.613 Bytes
- `git diff --check`: PASS
- `./gradlew clean check`: PASS, `BUILD SUCCESSFUL in 57s`

Commit: `9b727a7`

## Phase 8 — Explore-Kontext V4 und Zukunftscode-Abbau

Status: abgeschlossen am 2026-08-11.

Schwerpunkte: vorbereiteten Zukunftscode, generische Feature-Flags,
manuellen JSON-Writer, lokale Query-History und den Dependency-Guard entfernen;
den Explore-Kontext auf V4 mit direkten Booleans und Spring-Jackson halten;
produktive SQL-Rezepte und das R-Labor erhalten.

Verifikation:

- fokussierte Explore-Gradle-Suite: PASS
- Vitest: PASS, 20 Dateien / 114 Tests
- TypeScript: PASS
- Referenzprüfung auf entfernte Typen, Flags, Komponenten und Guard: PASS
- `git diff --check`: PASS
- `./gradlew clean check --no-daemon`: PASS, zweimal abschließend je 55s
- WebR-aktivierter Gesamtcheck: PASS, 59s
- realer WebR-Playwright-Smoke: PASS, 13s
- `./gradlew clean bootJar --no-daemon`: PASS, 15s
- finales JAR: 174.644.557 Bytes, keine EH/COI/Maps, MVP und R-Chunks vorhanden

Commit: `9bb7957`

## Beschreibende Metadaten mit eingeschränktem Markdown

Status: abgeschlossen am 2026-10-06.

Beschreibungen verwenden ein einheitliches Textprofil für technische Begriffe,
Hervorhebungen, Absätze, Zeilenumbrüche und Listen. CommonMark wird in eine
unveränderliche Dokumentstruktur überführt; ein kontrollierter Renderer erzeugt
Detail-HTML, kompakte Vorschauen und Klartext für Lucene und Explore.
Die Katalogbeschreibung bleibt im Snapshot erhalten und wird als Einführung
gerendert. Der XTF-Vertrag erhält kein zusätzliches Formatfeld.

Erfassungsregeln: [Beschreibende Metadaten erfassen](metadata-text.md).
UI-Vertrag, Component Map, Architektur, Suche und Nutzungsdokumentation wurden
entsprechend aktualisiert. Verwendete visuelle Referenzen:
`startseite_liste.png`, `cards.png`, `web-components.png`.

Verifikation:

- Fokussierte Parser-, Renderer-, ViewModel-, MVC- und Suchtests: PASS.
- Prüfung vorhandener XTF-Dateien: 2'959 Beschreibungsfelder, keine möglichen Markdown-Auszeichnungen gefunden.
- `./gradlew clean check`: PASS, `BUILD SUCCESSFUL in 2m 30s`.
- Java: 352 Tests, keine Fehler.
- Vitest: 24 Dateien / 152 Tests, TypeScript: PASS.
- Playwright: 55 ausgeführte Tests, keine Fehler; ein vorhandener Test übersprungen.
- Manuelle Browserprüfung mit temporärem XTF: Detailtext, Katalogeinführung und kompakte Kartenvorschau korrekt dargestellt.
- `git diff --check`: PASS.

Die in `AGENTS.md` genannte v5-Gesamtspezifikation fehlt im Checkout;
Umsetzungsgrundlage waren der freigegebene Plan und die vorhandenen verbindlichen
Dokumente. Ein späterer PDF-Renderer kann die Dokumentstruktur verwenden;
PDF-Erzeugung ist nicht Bestandteil dieser Umsetzung.
