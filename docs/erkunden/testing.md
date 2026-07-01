# Erkunden Tests

Status: Phase 0 baseline

Dieses Dokument sammelt die Teststrategie fuer die Erkunden-Phasen und die Phase-0-Baseline des bestehenden Projekts.

## Bestehende Testlandschaft

- JUnit/Jupiter und Spring Boot Test Support fuer Backend-, Domain-, Parser-, Search-, MVC- und Konfigurationstests.
- Playwright ist als Java/JUnit-Testpfad integriert.
- `test` schliesst Tests mit Tag `playwright` aus.
- `check` haengt von `playwrightTest` ab und installiert Chromium bei Bedarf ueber `installPlaywrightChromium`.
- Es gibt aktuell kein separates Node/Vitest/Playwright-Frontend-Paket.

## Baseline am 2026-07-01

Die folgenden Befehle wurden vor Phase-0-Dokumentationsaenderungen ausgefuehrt:

```bash
./gradlew test
```

Ergebnis: `BUILD SUCCESSFUL in 5s`

```bash
./gradlew check
```

Ergebnis: `BUILD SUCCESSFUL in 15s`

```bash
./gradlew clean check
```

Ergebnis: `BUILD SUCCESSFUL in 16s`

Hinweis: `check` und `clean check` fuehrten den vorhandenen `playwrightTest`-Task aus. Ein separates Frontend-Testkommando existiert in Phase 0 noch nicht.

## Ziel fuer spaetere Phasen

Phase 1:

- Backend-Unit-Tests fuer SQL-Namen, Rollen, Rezepte und Kontextaufbau.
- MVC-Tests fuer `/datasets/{datasetId}/explore` und `/datasets/{datasetId}/explore/context.json`.

Phase 2:

- Frontend-Unit-Tests fuer Kontext-Parsing und initiales Rendering.
- Backend-Seitentest bleibt gruen.

Phase 3 und spaeter:

- Browser-/Integrationstest mit kleinem Parquet-Fixture.
- Tests fuer DuckDB-Wasm-Initialisierung, Tabellenregistrierung, Rezeptausfuehrung, Resultattabelle, Charting und Mobile-Layout.

## Standard-Verifikation

Fuer abgeschlossene Phasen gilt weiterhin:

```bash
./gradlew clean check
```

Wenn spaeter ein Frontend-Paket eingefuehrt wird, muss dieses Dokument um die konkreten `npm`-, `pnpm`- oder `yarn`-Befehle erweitert werden.
