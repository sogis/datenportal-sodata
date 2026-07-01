# Erkunden Tests

Status: Phase 1 backend tests

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

## Phase 1 am 2026-07-01

Phase 1 ergaenzt Backend-Unit- und MVC-Tests:

- Backend-Unit-Tests fuer SQL-Namen, Rollen, Rezepte und Kontextaufbau.
- MVC-Tests fuer `/datasets/{datasetId}/explore` und `/datasets/{datasetId}/explore/context.json`.
- MVC-Test fuer Datensaetze ohne Parquet-Distribution.

Ausgefuehrte Befehle:

```bash
./gradlew test --tests 'ch.so.agi.datenportal.explore.*'
```

Ergebnis: `BUILD SUCCESSFUL in 2s`

```bash
./gradlew test
```

Ergebnis: `BUILD SUCCESSFUL in 5s`

```bash
./gradlew clean check
```

Ergebnis: `BUILD SUCCESSFUL in 17s`

Hinweis: Der erste fokussierte Implementierungslauf schlug voruebergehend fehl, weil Jackson nicht im Application Compile Classpath verfuegbar ist. Phase 1 verwendet deshalb einen projektlokalen JSON Writer und der fokussierte Testlauf wurde danach erfolgreich wiederholt.

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
