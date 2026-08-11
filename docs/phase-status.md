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

Commit: wird nach dem Commit eingetragen.

## Phase 7 — Bundle- und JAR-Größe

Status: ausstehend.

## Phase 8 — Explore-Kontext V4 und Zukunftscode-Abbau

Status: ausstehend.
