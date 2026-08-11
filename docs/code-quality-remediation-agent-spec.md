# Agenten-Spezifikation zur Codebereinigung

## 1. Zweck und Ergebnis

Dieses Dokument ist die verbindliche, ausführbare Arbeitsanweisung für einen LLM-Agenten zur schrittweisen Verbesserung der Codequalität der Datenportal-Webanwendung und der Explore-Insel.

Die Bereinigung besteht aus acht strikt aufeinanderfolgenden Phasen:

1. Explore-Lebenszyklen und Testdeterminismus
2. Java-Suche ohne stille Fehler und abgeschnittene Filterresultate
3. Produktionssichere und transparente Konfiguration
4. Korrekter UI-Vertrag und korrekte Abbildung sichtbarer Daten
5. Verkleinerung der ViewModels und der vorbereiteten Server-UI
6. Einmaliges Laden und konsistentes Veröffentlichen der fertigen `catalog.duckdb`
7. Verkleinerung der ausgelieferten Explore-Artefakte
8. Entfernung vorbereiteten Explore-Zukunftscodes

Ziel ist kein vollständiges Refactoring. Die Anwendung soll korrekt, robust und transparent funktionieren. Dabei ist Ockhams Razor verbindlich: Die kleinste verständliche Änderung, die das konkrete Problem vollständig behebt, ist einer allgemeineren oder abstrakteren Lösung vorzuziehen.

## 2. Verbindlicher Scope

Die Spezifikation umfasst vier Bereinigungen für den Java-/JTE-/HTMX-Teil und vier für die Explore-Insel. Sie beschreibt die Änderungen bis auf Klassen-, Methoden-, Record-, Template- und Testebene.

Die Herstellung von `catalog.duckdb` ist ausdrücklich nicht Teil dieser Arbeiten. Die Anwendung behandelt die Datei ausschließlich als fertig erzeugtes externes Artefakt. Erzeugung, Pipeline, Python-Skripte, fachliche Transformation und Datenmodellierung dieser Datei bleiben außerhalb des Scopes.

Der Agent darf keine zusätzlichen Bereinigungsprojekte eröffnen. Erkenntnisse außerhalb des beschriebenen Scopes werden dokumentiert, aber nicht umgesetzt.

## 3. Verbindliche Leitplanken

- Ockhams Razor ist bei jeder Entwurfsentscheidung das verbindliche Entscheidungskriterium.
- Vorhandener Code ist zu vereinfachen oder zu löschen, bevor neue Abstraktionen eingeführt werden.
- Eine kleine Menge gut verständlicher Redundanz ist einer zusätzlichen Abstraktionsschicht vorzuziehen.
- Keine neuen Schichten, Interfaces, Manager, Provider, Registries, Wrapper oder State-Machine-Klassen ohne nachgewiesenen konkreten Bedarf.
- Keine generischen Frameworks für Probleme erstellen, die nur an einer Stelle auftreten.
- Jede Phase hat einen klaren Scope, gezielte Tests und eine eigene Definition of Done.
- Die nächste Phase darf erst beginnen, wenn alle Tests und Abnahmekriterien der vorherigen Phase erfolgreich sind.
- Ein Test gilt nicht als erfolgreich, wenn er nur nach Wiederholungen zufällig grün wird.
- Bestehende Tests dürfen nur entfernt werden, wenn auch das ausschließlich von ihnen getestete Verhalten entfernt wird.
- Produktionscode darf nicht nur deshalb erweitert werden, um Tests leichter schreiben zu können. Test-Seams müssen klein und fachlich begründbar bleiben.
- Der blaue Info-Stil des Typ-Badges in der Kartenansicht bleibt bestehen. Der widersprüchliche UI-Vertrag wird an den korrekten Ist- und Sollzustand angepasst.
- „Daten validiert“ bleibt weiterhin an das Vorhandensein eines Datenmodells gekoppelt.
- Keine Commits ohne gesonderten Auftrag.
- Keine Erzeugung, Aktualisierung oder fachliche Validierung des Inhalts von `catalog.duckdb`.

## 4. Strikte Ausführungsreihenfolge und Quality Gates

Da aktuell ein nicht deterministischer Monaco-Autocomplete-Test beobachtet wurde, beginnt die Ausführung mit der Stabilisierung der Explore-Insel. Damit erhalten alle nachfolgenden Phasen ein verlässliches Quality Gate.

### 4.1 Vor jeder Phase

Der Agent muss vor jeder Phase in dieser Reihenfolge:

1. `git status --short` ausführen.
2. `git diff` vollständig prüfen.
3. Sicherstellen, dass keine fremden oder nicht zur Phase gehörenden Änderungen überschrieben werden.
4. Das Ergebnis, die Definition of Done und das Testprotokoll der vorherigen Phase prüfen.
5. Die für die Phase relevanten Spezifikationen, Skills und Dokumente erneut prüfen.
6. Einen kurzen, auf die Phase begrenzten Implementierungsplan formulieren.
7. Sicherstellen, dass nur Dateien der aktuellen Phase bearbeitet werden.

Wenn der Arbeitsbaum fremde Änderungen enthält, muss der Agent um diese Änderungen herumarbeiten. Ist das nicht sicher möglich, muss er stoppen und den Konflikt benennen.

### 4.2 Nach jeder Phase

Der Agent muss nach jeder Phase in dieser Reihenfolge:

1. Die gezielten Unit-, MVC-, Vitest- oder Playwright-Tests der Phase ausführen.
2. Die von der Phase betroffene Dokumentation aktualisieren.
3. `git diff --check` ausführen.
4. `./gradlew clean check` erfolgreich ausführen.
5. Änderungen, entfernten Code, Testergebnisse und bekannte Grenzen zusammenfassen.
6. Die Definition of Done der Phase Punkt für Punkt bestätigen.

Bei einem Fehler ist zu stoppen. Der Agent darf keine Arbeit an der nächsten Phase beginnen. Fehler sind ursächlich zu beheben; bloße Wiederholungen eines flakigen Tests gelten nicht als Behebung.

### 4.3 Phasenprotokoll

Für jede Phase ist ein kurzes Protokoll zu führen, das mindestens enthält:

- bearbeitete und gelöschte Dateien;
- bewusst nicht bearbeitete angrenzende Bereiche;
- ausgeführte Testbefehle und deren Ergebnis;
- Ergebnis von `git diff --check`;
- Ergebnis von `./gradlew clean check`;
- Abgleich mit der phasenspezifischen Definition of Done;
- verbleibende Einschränkungen oder Risiken.

## 5. Phase 1 – Explore-Lebenszyklen und Testdeterminismus

### 5.1 Ziel

DuckDB- und WebR-Runtimes müssen beim Abbruch, Timeout, Wechsel des Datensatzes und Unmount kontrolliert beendet werden. Eine gemeinsame DuckDB-Connection darf nie gleichzeitig von einer abgebrochenen und einer neuen Query verwendet werden. Der Monaco-Autocomplete-Test darf keine Retry-Schleife mehr benötigen.

### 5.2 Änderungen auf Komponenten-, Klassen- und Methodenebene

#### `ExploreApp.tsx`

Der Initialisierungs-`useEffect` wird so umgebaut, dass jeder Lauf einen eigenen `AbortController` und eine eindeutige Generation besitzt.

- Eine lokale asynchrone Funktion `initializeDuckDb(signal)` verwenden.
- Die Generation bei jedem neuen Initialisierungslauf monoton erhöhen.
- Vor jedem `setState` nach einem `await` prüfen, ob die Generation noch aktuell und die Komponente noch gemountet ist.
- Ein veralteter Initialisierungslauf darf weder erfolgreichen Zustand noch Fehlerzustand publizieren.
- Beim Cleanup:
  - den Fetch über den `AbortController` abbrechen;
  - die laufende Initialisierung als inaktiv markieren;
  - `room.roomStore.getState().db.destroy()` genau einmal nach Abschluss des Initialisierungsversuchs aufrufen.
- Die Zerstörung muss auch dann erfolgen, wenn das Cleanup während eines noch laufenden Initialisierungsversuchs eintritt.
- Keine neue Runtime-State-Klasse, keinen Lifecycle-Manager und keine State Machine einführen.

#### `attachCatalogDatabase.ts`

Die Signatur wird erweitert zu:

```ts
attachCatalogDatabase(
  connector: DuckDbConnector,
  catalogDatabase: ExploreCatalogDatabaseDto,
  baseUrl?: string,
  signal?: AbortSignal
): Promise<CatalogDatabaseRegistration>
```

- Das optionale `signal` an `fetch()` weitergeben.
- Einen Fetch-Abbruch weiterhin als kontrollierten Registrierungsfehler zurückgeben.
- Keine ungefangene Promise-Rejection erzeugen.
- Vorhandene Fehlerdetails beibehalten, sofern sie keine sensiblen Informationen offenlegen.
- Keine eigene Fetch-Abstraktion nur für diesen Abbruchfall einführen.

#### `WebRRuntime.ts`

`WebRRuntime` erhält nur die minimal notwendigen Lifecycle-Felder:

- `webRPromise`
- `webR`
- `closed`

`initialize()` muss:

- eine Initialisierung nach `close()` verweigern;
- bei parallelen Aufrufen dieselbe laufende Initialisierung teilen;
- nach erfolgreicher Initialisierung dieselbe Runtime zurückgeben;
- eine fehlgeschlagene Promise aus `webRPromise` entfernen, damit ein kontrollierter Retry möglich bleibt;
- sicherstellen, dass eine nach `close()` spät fertig werdende Initialisierung keine verwendbare Runtime mehr publiziert.

`load()` muss:

- die erzeugte WebR-Instanz bereits vor `init()` in `webR` speichern;
- die Instanz bei Init-Timeout, Init-Fehler, Paket-Timeout oder Paketfehler schließen;
- nach `close()` keine weiteren Fortschrittsschritte melden;
- bei einer während des Ladens geschlossenen Runtime nach dem nächsten asynchronen Schritt kontrolliert abbrechen;
- den ursprünglichen Fehler beziehungsweise einen aussagekräftigen Timeoutfehler weitergeben.

`close()` muss:

- idempotent sein;
- eine bereits erzeugte Runtime genau einmal schließen;
- auch eine Runtime schließen, deren Initialisierung erst nach dem Cleanup fertig wird;
- eine spätere Initialisierung zuverlässig verhindern;
- keine zusätzliche öffentliche Lifecycle-API einführen.

`withTimeout()` erhält einen optionalen Timeout-Cleanup-Callback.

- Der Callback wird ausschließlich beim tatsächlichen Timeout ausgeführt.
- Fehler des Cleanup-Callbacks dürfen den primären Timeoutfehler nicht verschleiern.
- Es wird keine allgemeine Timeout-Serviceklasse ergänzt.

#### `RPanel.tsx`

Ein unmount-spezifischer Effect wird ergänzt. Sein Cleanup muss:

- `bridgeRef.current` leeren;
- `runtimeRef.current?.close()` aufrufen;
- `runtimeRef.current` leeren;
- die Komponente als unmounted markieren.

Laufende Initialisierungen und Transfers werden mit einer monotonen Operations-ID geschützt.

- `initializeRuntimeWithoutData(operationId)` erhält die Operations-ID.
- `transferSnapshot(operationId)` erhält die Operations-ID.
- Nach jedem `await` prüfen beide Methoden, ob die Operation noch aktuell und die Komponente noch gemountet ist.
- Ein alter Transfer darf weder Daten noch Status eines neueren Transfers überschreiben.
- `updateStep()` ignoriert Updates nach Unmount und Updates einer veralteten Operation.
- Keine allgemeine Task- oder Operation-Manager-Abstraktion einführen.

#### `executeDuckDbQuery.ts`

Das Query-Ergebnis darf nicht mehr durch ein sofortiges `Promise.race()` von einer weiterhin laufenden DuckDB-Query entkoppelt werden.

Bei Abbruch oder Timeout muss der Query-Handle:

- `cancelSent()` genau einmal auslösen;
- auf das Ende der ursprünglichen Query-Promise warten;
- erst danach einen `AbortError` beziehungsweise den vorhandenen Timeoutstatus liefern;
- den ursprünglichen Queryfehler nur dann weitergeben, wenn kein Abbruchstatus Vorrang hat.

`cancel()` muss:

- idempotent sein;
- bei parallelen Aufrufen dieselbe Cancel-Abwicklung teilen;
- erst zurückkehren, wenn die aktive Query nicht mehr auf der gemeinsamen Connection läuft;
- auch bei einem Fehler von `cancelSent()` das Ende der Query-Promise abwarten, soweit technisch möglich.

Es ist keine neue Query-Scheduler- oder Connection-Pool-Abstraktion einzuführen.

#### `SqlLaboratory.tsx`

`runSql()` muss:

- einen zweiten Start verweigern, solange `activeQuery.current` gesetzt ist;
- den eigenen Query-Handle lokal behalten;
- im `finally` die Referenz nur löschen, wenn `activeQuery.current` noch auf genau diesen Handle zeigt;
- Timeout, manuellen Abbruch und normalen Fehler als unterschiedliche Resultatstatus sichtbar lassen.

Beim Unmount wird eine aktive Query abgebrochen.

`cancelQuery()` muss:

- den aktuellen Handle lesen;
- auf `handle.cancel()` warten;
- erst danach die UI wieder für eine neue Query freigeben;
- wiederholte Cancel-Klicks kontrolliert behandeln.

#### `SqlEditorField.tsx`

- Nach erfolgreichem `handleEditorMount()` einen lokalen Zustand `editorReady` setzen.
- Auf dem Monaco-Host `data-autocomplete-ready="true"` erst setzen, wenn:
  - Monaco gemountet ist;
  - die Completion-Registrierung abgeschlossen ist;
  - aktuelle Tabellenschemas vorhanden sind.
- Bei Schemawechsel muss die Bereitschaft den neuen Registrierungszustand korrekt widerspiegeln.
- Das Attribut dient der beobachtbaren fachlichen Bereitschaft und nicht als künstliche Zeitverzögerung für Tests.

#### Playwright-Helfer

`waitForSuggestion()` wird deterministisch:

- die dreifache `Control+Space`-Retry-Schleife entfernen;
- zuerst auf `[data-autocomplete-ready="true"]` warten;
- danach genau einmal Completion öffnen;
- auf den erwarteten Eintrag warten;
- bei Fehlern weiterhin Editorinhalt, Widgetinhalt und Browserfehler ausgeben.

Keine zusätzlichen Sleeps oder pauschalen Timeoutverlängerungen einführen.

### 5.3 Tests

#### Neue `WebRRuntime.test.ts`

Mindestens folgende Fälle testen:

- doppeltes `initialize()` teilt dieselbe Promise;
- doppeltes `close()` schließt die Runtime nur einmal;
- Unmount beziehungsweise `close()` während `init()` schließt eine spät aufgelöste Runtime;
- Init-Timeout schließt die Runtime;
- Paket-Timeout schließt die Runtime;
- fehlgeschlagene Initialisierung kann kontrolliert wiederholt werden;
- nach `close()` werden keine Fortschrittsmeldungen mehr publiziert.

#### `RPanel.test.tsx`

Mindestens folgende Fälle ergänzen:

- Unmount während Initialisierung erzeugt keine State-Updates;
- ein alter Transfer überschreibt keinen neueren Transfer;
- die Runtime wird beim Unmount geschlossen;
- ein später Abschluss nach Unmount verändert die UI nicht.

#### Neue `executeDuckDbQuery.test.ts`

Mindestens folgende Fälle testen:

- manueller Abbruch ruft `cancelSent()` genau einmal auf;
- mehrere `cancel()`-Aufrufe teilen dieselbe Abwicklung;
- das Query-Ende wird vor Abschluss von `cancel()` abgewartet;
- Timeout und normaler Fehler bleiben unterscheidbar;
- nach abgeschlossenem Cancel läuft keine alte Query mehr auf der Connection.

#### `SqlLaboratory.test.tsx`

Mindestens folgende Fälle ergänzen:

- keine parallelen Queries;
- ein zweiter Start während der aktiven Query wird ignoriert oder kontrolliert abgelehnt;
- nach vollständigem Cancel kann eine neue Query laufen;
- ein altes `finally` löscht keinen neueren aktiven Handle;
- Unmount bricht die aktive Query ab.

#### Playwright

- Autocomplete ohne Retry testen.
- Der Test wartet auf das fachliche Ready-Attribut und öffnet Completion genau einmal.
- Der reale WebR-Smoke-Test bleibt separat teuer, ist aber ein zwingendes Phasengate.

### 5.4 Definition of Done

- `./gradlew clean check` läuft zweimal hintereinander erfolgreich.
- `./gradlew clean check -Ddatenportal.playwright.webr=true` ist erfolgreich.
- Keine Retry-Schleife im Autocomplete-Test.
- Keine React-State-Updates nach Unmount.
- Keine zweite Query auf einer noch laufenden DuckDB-Connection.
- DuckDB und WebR werden bei allen beschriebenen Cleanup-Pfaden kontrolliert beendet.
- Die Phasendokumentation und das Testprotokoll sind vollständig.
- Erst danach darf Phase 2 beginnen.

## 6. Phase 2 – Java-Suche: keine stillen Fehler und keine abgeschnittenen Filterresultate

### 6.1 Ziel

Ein Lucene-Fehler darf niemals als „0 Treffer“ erscheinen. Filter dürfen keine fachlich passenden Treffer verlieren, nur weil vor der Java-Filterung maximal 500 Lucene-Treffer gelesen wurden.

### 6.2 Schnittstellenänderungen

`CatalogSearchIndex` wird auf die tatsächlich benötigte API reduziert:

```java
interface CatalogSearchIndex {
    List<SearchHit> search(String userQuery);
    int documentCount();
    void close();
}
```

- `isEmpty()` entfällt.
- Der Parameter `maxResults` entfällt.
- Es werden keine paginierten oder streamingbasierten Suchabstraktionen ergänzt.

Als einzige neue Exception wird eingeführt:

```java
final class CatalogSearchException extends RuntimeException
```

Die Exception erhält nur die zum Wrapping notwendigen Konstruktoren. Keine Exception-Hierarchie ergänzen.

### 6.3 Änderungen auf Klassen- und Methodenebene

#### `LuceneCatalogSearchIndex`

`search(String)` muss:

- bei leerer oder ausschließlich aus Whitespace bestehender Eingabe weiterhin eine leere Liste liefern;
- bei geschlossenem Index `CatalogSearchException` werfen;
- Lucene-Treffer bis `reader.numDocs()` lesen;
- die Trefferrangfolge unverändert in `SearchHit` abbilden;
- `IOException` als `CatalogSearchException` mit verständlichem Kontext weitergeben;
- unerwartete Runtimefehler als `CatalogSearchException` weitergeben, sofern sie nicht bereits diesen Typ haben;
- die bisherige Warnung mit sinngemäß „returning no results“ entfernen.

`documentCount()` muss:

- `reader.numDocs()` liefern;
- bei geschlossenem Index kontrolliert `CatalogSearchException` werfen;
- Lucene-Fehler nicht in `0` umwandeln.

`close()` bleibt idempotent. Es darf keine zweite Close-Abstraktion eingeführt werden.

#### `EmptyCatalogSearchIndex`

- `search(String)` liefert eine leere Liste.
- `documentCount()` liefert `0`.
- `close()` bleibt ein kontrollierter No-op.

#### `CatalogSearchService`

`luceneEntries()` ruft auf:

```java
snapshot.searchIndex().search(query.q())
```

Danach wird die vollständige Lucene-Treffermenge mit den bestehenden Java-Filtern verarbeitet.

- Bestehende AND-Semantik zwischen Filterkategorien beibehalten.
- Bestehende OR-Semantik innerhalb einer Kategorie beibehalten.
- Ranking und Reihenfolge der Lucene-Treffer beibehalten, soweit kein expliziter Sortiermodus greift.
- Suchfehler nicht abfangen oder in leere Trefferlisten umwandeln.
- Leere Suchanfragen verwenden weiterhin direkt `visibleEntries` und umgehen Lucene.

#### `SearchProperties`

Die Signatur wird reduziert auf:

```java
SearchProperties(int defaultPageSize, int maxPageSize)
```

- `maxResults` entfernen.
- `datenportal.search.max-results` aus allen Konfigurationsdateien entfernen.
- Die Einstellung aus Dokumentation und Tests entfernen.
- Keine Ersatzgrenze unter anderem Namen einführen.

#### `CatalogSearchIndexHealthIndicator`

- Erwartete Dokumentzahl: `snapshot.visibleEntries().size()`.
- Tatsächliche Dokumentzahl: `snapshot.searchIndex().documentCount()`.
- Nur identische Zahlen ergeben `UP`.
- Abweichende Zahlen ergeben `DOWN`.
- Fehler aus `documentCount()` ergeben `DOWN` und werden als Fehlerdetail intern sichtbar, nicht als `0` kaschiert.
- Detailnamen:
  - `expectedDocuments`
  - `indexedDocuments`

#### `CatalogService`

`currentSnapshot()` wird entfernt.

Begründung: Die Methode gibt nach Freigabe des Read-Locks einen Snapshot zurück, dessen Index durch einen parallelen Reload geschlossen werden kann.

- Alle Produktionsaufrufer verwenden ausschließlich `withSnapshot()`.
- Tests werden ebenfalls auf `withSnapshot()` umgestellt.
- Keine alternative ungeschützte Getter-Methode ergänzen.

#### `CatalogErrorControllerAdvice`

Eine Methode `searchUnavailable(...)` wird ergänzt.

- Sie behandelt `CatalogSearchException`.
- Die HTTP-Antwort erhält Status 503.
- Die Benutzeroberfläche zeigt eine verständliche Fehlerseite und niemals die normale Leermengenansicht.
- Interne Lucene-Details oder Stacktraces werden nicht gerendert.
- Bei HTMX-Anfragen wird `HX-Refresh: true` gesetzt, damit ein vollständiger Reload die 503-Seite zeigt und nicht still alte Resultate stehen bleiben.

#### `ErrorPageVmFactory`

Eine Methode wird ergänzt:

```java
serviceUnavailable(String message)
```

Sie erzeugt das bestehende Fehlerseiten-ViewModel mit Status und Text für einen nicht verfügbaren Suchdienst. Keine neue Fehlerseitenhierarchie erstellen.

#### `CatalogSearchFields` und `CatalogDocumentMapper`

Alle ausschließlich geschriebenen, aber nie gelesenen Felder werden entfernt, insbesondere:

- `ENTRY_TYPE`
- `IDENTIFIER_TEXT`
- `TITLE`
- `DESCRIPTION`
- `KEYWORDS`
- `THEME_TEXT`
- `THEME_EXACT`
- `OFFICE_TEXT`
- `OFFICE_EXACT`
- `FORMATS`
- beide Datumsfelder
- `OPEN_DATA`
- `STRUCTURE_DESCRIBED`
- `ISSUE_YEARS`
- `ISSUE_TEXT`
- `ALL_TEXT`

Erhalten bleiben ausschließlich:

- `ENTRY_ID` für die Zuordnung zum Snapshot;
- die Felder, die `buildQuery()` tatsächlich liest.

Damit wird auch das irreführende hartcodierte `structure_described=false` entfernt. Es werden keine neuen Felder vorsorglich ergänzt.

### 6.4 Tests

Mindestens folgende Tests ergänzen oder anpassen:

- Mehr als 500 Texttreffer indexieren; nur ein Treffer nach Position 500 erfüllt den gewählten Java-Filter; dieser Treffer muss gefunden werden.
- Ein geschlossener Index wirft `CatalogSearchException` statt eine leere Liste zurückzugeben.
- Ein simulierter Lucene-I/O-Fehler ergibt HTTP 503.
- Eine HTMX-Fehlerantwort enthält `HX-Refresh: true`.
- Eine normale Vollseitenanfrage erhält eine verständliche 503-Seite.
- Eine leere Suchanfrage verwendet weiterhin direkt `visibleEntries`.
- Bestehende Sonderzeichen-, Teilwort-, AND- und Rankingtests bleiben bestehen.
- Health ist `DOWN`, wenn Dokument- und Snapshotanzahl abweichen.
- Health ist `DOWN`, wenn `documentCount()` fehlschlägt.
- `withSnapshot()` schützt einen Suchzugriff während eines simulierten Reloads.

### 6.5 Definition of Done

- Kein `max-results` mehr in Code, Konfiguration, Tests oder Dokumentation.
- Kein Catch-Pfad liefert bei Suchfehlern `List.of()` oder eine andere künstliche Leermenge.
- Keine unbenutzten Indexfelder.
- Kein produktiver Aufruf von `currentSnapshot()`; die Methode existiert nicht mehr.
- Suchfehler sind für Vollseiten- und HTMX-Anfragen transparent als 503 sichtbar.
- Alle gezielten Such-, MVC- und Health-Tests sind erfolgreich.
- `git diff --check` ist erfolgreich.
- `./gradlew clean check` ist erfolgreich.
- Erst danach darf Phase 3 beginnen.

## 7. Phase 3 – Produktionssichere und transparente Konfiguration

### 7.1 Ziel

Ohne explizite Produktionskonfiguration darf die Anwendung nicht unbemerkt mit Demo-XTF, localhost-Downloads, Entwicklungs-JTE oder öffentlich sichtbaren Health-Details starten.

### 7.2 Profile und Konfigurationsdateien

#### `application.yml`

Die Basis-Konfiguration enthält ausschließlich produktionssichere Grundwerte.

- `gg.jte.development-mode: false`
- `management.endpoint.health.show-details: never`
- keine localhost-Download-URL;
- keine Fixture-XTF als Fallback;
- keine Fixture-`catalog.duckdb` als Fallback;
- keine implizite produktive Quellart.

#### `application-local.yml`

Das lokale Profil enthält explizit:

- die bestehende XTF-Fixture mit 62 Einträgen;
- die lokale fertige `catalog.duckdb`;
- die localhost-Downloadbasis;
- JTE Development Mode.

#### `application-test.yml`

Das Testprofil enthält explizit die deterministischen Test-Fixtures und alle zum Start benötigten Testwerte.

#### `src/test/resources/application.properties`

Das Profil `test` wird global für Tests aktiviert, sofern ein einzelner Test nicht bewusst ein anderes Profil prüft.

### 7.3 Änderungen auf Klassen- und Methodenebene

#### `CatalogProperties`

- Das Legacy-Feld `source` entfernen.
- `effectiveSourceType()` entfernen.
- Die Legacy-Erkennung und zugehörige Kompatibilitätslogik entfernen.
- `sourceType` ist zwingend.
- Für die gewählte Quellart ist genau die zugehörige Location zwingend.
- Nicht zur gewählten Quellart gehörende optionale Felder dürfen leer bleiben.
- HTTP-Timeouts und Größenlimit behalten sinnvolle technische Defaults.
- `downloadUrl` erhält keinen localhost-Default.
- `downloadUrl == null` bleibt zulässig, solange die geladene XTF keinen `${DOWNLOAD_URL}`-Platzhalter enthält.
- Ungültige HTTP-Schemes werden früh und verständlich abgelehnt.

Die Validierung bleibt in der vorhandenen Property-Struktur. Keine separate Validator-Klassenhierarchie ergänzen.

#### `CatalogImportConfiguration.catalogSource()`

- Einen direkten `switch` über `sourceType()` verwenden.
- Für jede Quellart genau die benötigte Source mit ihrer validierten Location erzeugen.
- Keine Legacy-Fallbacks und keine automatische Quellarterkennung beibehalten.

#### `CatalogDuckDbProperties`

- Den impliziten Classpath-Default entfernen.
- `sourceType` explizit verlangen.
- Die zur Quellart gehörende Location explizit verlangen.
- Schema `opendata`, Timeouts und Größenlimit dürfen technische Defaults behalten.
- Nur das Laden eines fertigen Artefakts konfigurieren.
- Keine Einstellung zur Erzeugung oder Transformation der Datei ergänzen.

#### `CatalogSnapshotHealthIndicator`

Der Indicator darf intern weiterhin für den Betrieb nützliche Werte ausgeben, darunter:

- Counts;
- Ladezeit beziehungsweise Zeitpunkt;
- Inhalts-Hashes.

Er darf keine absoluten Datei- oder Quellpfade ausgeben.

#### Actuator-Konfiguration

- `/actuator/health` zeigt öffentlich nur den Gesamtstatus.
- Keine `components`, internen Details oder Pfade in der öffentlichen Antwort.
- Detaillierter Reload-Status bleibt über den geschützten Admin-Endpunkt verfügbar.
- Keine zweite öffentliche Diagnose-Route ergänzen.

#### `docs/configuration.md`

Die Dokumentation beschreibt vollständig:

- alle benötigten Umgebungsvariablen;
- zulässige Quellarten;
- die pro Quellart benötigte Location;
- XTF- und DuckDB-Konfiguration;
- Timeouts und Größenlimits;
- optionales `downloadUrl` und dessen Platzhalterbedingung;
- den lokalen Start mit `SPRING_PROFILES_ACTIVE=local`;
- das Verhalten bei fehlender oder ungültiger Produktionskonfiguration;
- die Trennung zwischen extern erzeugter DuckDB-Datei und deren Laden durch die Anwendung.

### 7.4 Tests

#### Neuer Configuration-Startup-Test

Mindestens folgende isolierte Startfälle prüfen:

- fehlender `source-type` verhindert den Start;
- fehlende source-spezifische Location verhindert den Start;
- fehlende DuckDB-Quellart verhindert den Start;
- fehlende DuckDB-Location verhindert den Start;
- ungültige HTTP-Schemes werden abgelehnt;
- das Local-Profil startet mit den lokalen Fixtures;
- das Testprofil startet mit den Test-Fixtures;
- die Basiskonfiguration startet nicht unbemerkt mit Demo-Daten.

#### `CatalogPropertiesTest`

- Alle Legacy-Kompatibilitätstests entfernen.
- Direkte Validierung der expliziten Quellarten testen.
- `downloadUrl == null` ohne Platzhalter testen.
- Fehlende `downloadUrl` bei vorhandenem Platzhalter als verständlichen Fehler testen.

#### `CatalogDuckDbPropertiesTest`

- Keinen Classpath-Default mehr erwarten.
- Quellart und passende Location testen.
- Technische Defaults separat testen.

#### `CatalogActuatorMvcTest`

- Öffentliche Health-Antwort enthält keine `components`.
- Öffentliche Health-Antwort enthält keine Detailobjekte.
- Öffentliche Health-Antwort enthält keine absoluten oder relativen Quellpfade.
- Der Gesamtstatus bleibt sichtbar.

#### Indicator-Unit-Tests

- Interne Zustände und Counts separat testen.
- Hashes und Zeitangaben testen, ohne öffentliche Sichtbarkeit anzunehmen.
- Keine Pfade in Indicator-Details zulassen.

#### Anwendungstest

- Nur mit `withSnapshot()` auf den Startsnapshot zugreifen.
- Kein Test darf die entfernte Methode `currentSnapshot()` wieder einführen oder umgehen.

### 7.5 Definition of Done

- Ein Start ohne Produktionsquelle scheitert früh mit verständlicher Meldung.
- Keine Fixture-Datei und keine localhost-URL dient als produktiver Fallback.
- `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun` bleibt der dokumentierte lokale Einstieg.
- Öffentliche Health-Antwort enthält keine Quellpfade und keine Detailkomponenten.
- Alle Umgebungsvariablen sind in `docs/configuration.md` dokumentiert.
- Keine Legacy-Quellkonfiguration verbleibt.
- `git diff --check` ist erfolgreich.
- `./gradlew clean check` ist erfolgreich.
- Erst danach darf Phase 4 beginnen.

## 8. Phase 4 – UI-Vertrag und sichtbare Daten korrekt abbilden

### 8.1 Ziel

Dokumentation, ViewModel-Mapping und Interaktion müssen dasselbe Verhalten beschreiben. Die Oberfläche darf keine Scheinlinks anzeigen und vorhandene Strukturinformationen nicht als fehlend darstellen.

Für jede Änderung an JTE, CSS oder JavaScript sind vorab die verbindlichen UI-Skills und UI-Verträge zu lesen.

### 8.2 Blauer Typ-Badge

Folgende Quellen werden auf den gewünschten blauen Info-Stil angepasst:

- `AGENTS.md`
- `.agents/skills/datenportal-ui-contract/SKILL.md`
- `docs/ui-primitives.md`
- `docs/ui-implementation-contract.md`

Der verbindliche Vertrag lautet anschließend:

- `Datensatz` und `Datenreihe` verwenden in Karten denselben blauen Info-Stil.
- `Struktur beschrieben` bleibt neutralgrau.
- Keyword-Badges bleiben neutralgrau.
- Der blaue Typ-Badge ist kein Fehlerzustand und kein anklickbares Element.

`entryCard.jte` bleibt hinsichtlich `dp-status-badge--info` unverändert. Es wird nicht der korrekte Code an den alten Vertrag zurückgebaut, sondern der widersprüchliche Vertrag korrigiert.

### 8.3 Änderungen auf Klassen-, Methoden-, Template- und JavaScript-Ebene

#### `ResultsVmFactory.card()`

`structureDescribed` wird belegt mit:

```java
entry.metadata().hasStructureInformation()
```

Damit gilt:

- Einträge mit Attributbeschreibungen zeigen `Struktur beschrieben`.
- Einträge mit Datenmodell zeigen `Struktur beschrieben`.
- Einträge mit beidem zeigen den Badge einmal.
- Einträge ohne beides zeigen ihn nicht.
- Diese Abbildung gilt für Datensätze und Datenreihen.

Es wird keine parallele UI-spezifische Strukturheuristik eingeführt.

#### `entryRow.jte`

Serienzeilen erhalten ein Attribut `data-series-expand-href` mit dem bereits vorhandenen Expand-/Collapse-Ziel.

- Das Attribut wird nur auf expandierbaren Serienzeilen gesetzt.
- Der bestehende Plus-/Minus-Link bleibt erhalten.
- Der Link bleibt der zugängliche Tastaturmechanismus.
- Ohne JavaScript navigiert nur der Plus-/Minus-Link.
- Es wird kein zusätzliches unsichtbares Overlay und kein zweiter Button ergänzt.

#### `catalog-filters.js`

Die bestehende zentrale Eventbehandlung reagiert auf Klicks in nicht interaktiven Bereichen einer Serienzeile.

- Das nächste Element mit `data-series-expand-href` ermitteln.
- Bei einem erlaubten Zeilenklick dieselbe Navigation beziehungsweise HTMX-Aktion wie der Plus-/Minus-Link auslösen.
- Ein zweiter Klick auf die aktivierte Zeile klappt die Serie wieder ein.
- Klicks auf folgende Ziele dürfen niemals expandieren oder einklappen:
  - `a`
  - `button`
  - Download-Aktionen
  - Info-Links
  - `input`
  - `select`
  - `textarea`
  - sonstige Formelemente
  - `label`
- Die Prüfung muss auch für Kindknoten dieser interaktiven Elemente gelten.
- Keine neue JavaScript-Komponente oder Client-State-Verwaltung einführen.

#### Qualitätsinformationen: konkreter Ist-Fehler

Der Ausgangszustand ist ausdrücklich wie folgt zu verstehen:

- Das XTF enthält `QualitySummary.reportUrl`.
- `DetailPageVmFactory.quality()` verwirft diese URL derzeit.
- Stattdessen wird ein `href="#"` erzeugt.
- Das Datenmodell selbst besitzt keine URL.

Daraus folgt der verbindliche Sollzustand:

- Der Modellname wird als normaler Text gerendert, nicht als Link.
- Ein Validierungsreport wird nur gerendert, wenn `qualitySummary` vorhanden ist.
- Der Reportlink verwendet `qualitySummary.reportUrl()`.
- Der sichtbare Dateiname wird aus dem URI-Pfad abgeleitet.
- Kann kein sinnvoller Dateiname abgeleitet werden, lautet der Fallback `Validierungsreport`.
- „Daten validiert“ bleibt unverändert an `metadata.model().isPresent()` gekoppelt.
- Ein vorhandener Quality Report ohne Datenmodell verändert die Semantik des Badges „Daten validiert“ nicht.

#### `QualityVm`

`QualityVm` wird auf die tatsächlich dargestellten Informationen reduziert:

- Modelltext;
- optionaler Reportname;
- optionaler Report-Href.

Konkrete Record-Felder sind so zu wählen, dass Modelltext und optionaler Report ohne Scheinwerte repräsentiert werden. Es dürfen weder `#` noch leere Strings als Ersatz für fehlende Links verwendet werden. Keine allgemeine Link-ViewModel-Hierarchie einführen.

#### `DetailPageVmFactory.quality()`

Die Methode muss:

- den Modellnamen als Text übernehmen;
- `qualitySummary.reportUrl()` als echte URL übernehmen;
- den Reportnamen robust aus dem URI-Pfad ableiten;
- bei fehlendem oder nicht ableitbarem Dateinamen `Validierungsreport` verwenden;
- fehlende Werte mit `Optional` beziehungsweise dem bestehenden optionalen Muster abbilden;
- niemals `href="#"` erzeugen.

### 8.4 Tests

Mindestens folgende Tests ergänzen oder anpassen:

- MVC-Test für den blauen Typ-Badge bei Datensatzkarten.
- MVC-Test für den blauen Typ-Badge bei Datenreihenkarten.
- Struktur-Badge bei vorhandener Attributbeschreibung.
- Struktur-Badge bei vorhandenem Datenmodell.
- Struktur-Badge bei weder Attributbeschreibung noch Datenmodell nicht vorhanden.
- Sicherstellen, dass der Struktur-Badge nicht doppelt erscheint, wenn beide Quellen vorhanden sind.
- Playwright: Klick auf Serientitel expandiert.
- Playwright: Zweiter Klick auf die Zeile klappt ein.
- Playwright: Info-Link expandiert nicht.
- Playwright: Download expandiert nicht.
- Playwright oder MVC: Plus-/Minus-Link bleibt vorhanden und funktionsfähig.
- Factory-Test übernimmt die echte `reportUrl`.
- MVC-Test rendert die echte `reportUrl`.
- Dateiname wird korrekt aus einem normalen URI-Pfad abgeleitet.
- Fallback `Validierungsreport` bei nicht ableitbarem Dateinamen.
- Kein `href="#"` im Qualitätsbereich.
- Bestehende Tests für „Daten validiert“ bleiben unverändert und erfolgreich.

### 8.5 Definition of Done

- UI-Vertrag und Code stimmen beim blauen Badge überein.
- `entryCard.jte` verwendet weiterhin `dp-status-badge--info` für den Typ.
- Der Struktur-Badge wird fachlich korrekt über `hasStructureInformation()` gemappt.
- Die Serienzeile erfüllt den dokumentierten Klickvertrag.
- Info- und Download-Klicks verändern den Expand-Zustand nicht.
- Der Qualitätsbereich enthält keine Scheinlinks.
- Modellnamen sind Text, Report-URLs sind echte Links.
- Die Semantik von „Daten validiert“ ist unverändert.
- `git diff --check` ist erfolgreich.
- `./gradlew clean check` ist erfolgreich.
- Erst danach darf Phase 5 beginnen.

## 9. Phase 5 – ViewModels und vorbereitete Server-UI konsequent verkleinern

### 9.1 Ziel

Die Zahl der ViewModel-Typen wird von 45 beziehungsweise aktuell gezählten 46 Dateien auf höchstens 34 Dateien im Package `web/view` reduziert. Es entsteht keine neue ViewModel-Hierarchie und kein generisches Rendering-Framework.

Diese Phase ist eine gezielte Entfernung unnötiger Typen, Wrapper und vorbereiteter UI. Sie ist kein Auftrag, alle ViewModels neu zu entwerfen.

### 9.2 Zu löschende Typen und Produktionsklassen

Folgende Typen werden gelöscht:

- `CatalogEntrySummaryVm`
- `DownloadButtonVm`
- `StarterRecipeVm`
- `ContactMetadataSectionVm`
- `ContactMetadataItemVm`
- `ContactMetadataLineVm`
- `DatasetDetailPageVm`
- `IssueDetailPageVm`
- `DownloadSectionVm`
- `RelatedIssuesVm`
- `SeriesIssuesVm`
- `ResultControlsVm`
- `SortOptionVm`
- `FilterGroupType`

Zusätzlich wird die unbenutzte Produktionsklasse `StaticCatalogFactory` gelöscht.

Vor der Löschung ist mit `rg` zu prüfen, dass alle Verwendungen entweder in dieser Phase umgestellt oder ebenfalls entfernt werden. Keine Deprecated-Weiterleitungen und keine Kompatibilitätswrapper beibehalten.

### 9.3 Neue, begründete Typen

Nur zwei neue ViewModels sind zulässig.

#### `MetadataLineVm`

```java
record MetadataLineVm(String value, Optional<String> href)
```

- `value` ist verpflichtend und darf nicht leer normalisiert werden, wenn fachlicher Text vorhanden ist.
- `href` ist nur für tatsächlich navigierbare HTTP- oder Mail-Ziele vorhanden.
- Keine Untertypen für Text, Weblink und Mail-Link ergänzen.

#### `EntryDetailPageVm`

```java
record EntryDetailPageVm(
    PageChromeVm chrome,
    Optional<String> seriesTitle,
    Optional<String> seriesHref,
    String title,
    String description,
    AccessStateVm accessState,
    String structureQualityOriginHref,
    String exploreHref,
    String usageHref,
    boolean currentIssue,
    List<DetailFeatureVm> features,
    List<DownloadLinkVm> downloads,
    MetadataSectionVm overview,
    MetadataSectionVm temporalCoverage,
    MetadataSectionVm topics,
    MetadataSectionVm responsibilitiesContact,
    List<RelatedIssueVm> relatedIssues)
```

- Alle Listen werden im kompakten Konstruktor defensiv mit `List.copyOf(...)` kopiert.
- Optionals bleiben explizit und werden nicht durch leere Strings ersetzt.
- Keine Basisklasse und kein Interface ergänzen.
- Kein separates Dataset- oder Issue-Submodell beibehalten.

### 9.4 Bestehende Typen vereinfachen

#### `MetadataItemVm`

Neue Signatur:

```java
record MetadataItemVm(String label, List<MetadataLineVm> lines)
```

- `lines` wird defensiv kopiert.
- Eine normale Metadatenzeile enthält genau eine `MetadataLineVm`.
- Kontaktmetadaten dürfen mehrere Text-, HTTP- und Mail-Zeilen enthalten.
- `metadataSection.jte` rendert beide Fälle.
- `contactMetadataSection.jte` entfällt.

#### `SeriesDetailPageVm`

Reduzieren auf:

```java
record SeriesDetailPageVm(
    PageChromeVm chrome,
    String title,
    String description,
    List<SeriesIssueVm> issues)
```

`issues` wird defensiv kopiert. Weitere Darstellungswrapper werden nicht ergänzt.

#### `UsagePageVm`

- `starterRecipes` entfernen.
- Starter-Rezepte vollständig aus der Usage-Seite entfernen.
- Begründung: Ihre Links zeigen ausschließlich auf `#` und repräsentieren vorbereiteten Zukunftscode ohne funktionierende Ziele.
- Direktzugriff und vorhandene echte Codebeispiele bleiben erhalten.

#### `ResultsVm`

`ResultControlsVm` und `SortOptionVm` werden inline ersetzt:

```java
record ResultsVm(
    int totalElements,
    ViewMode viewMode,
    String listHref,
    String cardsHref,
    SortMode sortMode,
    List<EntryRowVm> rows,
    List<EntryCardVm> cards)
```

- `rows` und `cards` werden defensiv kopiert.
- `totalLabel()`, `isList()`, `isCards()` und `isEmpty()` bleiben einfache abgeleitete Methoden.
- Das JTE iteriert direkt über `SortMode.values()`.
- Keine separate Controls-Klasse und keine Liste vorberechneter Sortieroptionen beibehalten.

#### Benennungen

- `ResultItemVm` wird in `EntryRowVm` umbenannt.
- `CardResultVm` wird in `EntryCardVm` umbenannt.
- Produktionscode, Tests, JTE-Imports und Dokumentation werden atomar angepasst.
- Die Benennungen stimmen danach mit `docs/component-map.md` überein.

#### Filter

- `FilterGroupVm.type` wird durch `boolean singleSelect` ersetzt.
- Der Zwei-Werte-Enum `FilterGroupType` entfällt.
- Das Template entscheidet direkt anhand von `singleSelect`.
- Keine neue Strategie oder Unterklassenhierarchie für Filtergruppen ergänzen.

### 9.5 `DetailPageVmFactory`

Die Factory wird auf die tatsächlich gerenderten Modelle reduziert.

- `dataset()` liefert `EntryDetailPageVm`.
- `issue()` liefert `EntryDetailPageVm`.
- `seriesIssues()` liefert direkt `List<SeriesIssueVm>`.
- `relatedIssues()` liefert direkt `List<RelatedIssueVm>`.
- `responsibilitiesContactSection()` liefert `MetadataSectionVm`.
- `item()` erzeugt eine einzeilige `MetadataLineVm`.
- Kontaktmethoden liefern `List<MetadataLineVm>`.
- Gemeinsame fachliche Logik von Dataset und Issue darf in einer kleinen privaten Methode verbleiben, wenn dadurch echte Duplikation vermieden wird; keine neue Factory einführen.

Folgende tote Methoden werden entfernt:

- `metadataSections()`
- `addSection()`
- `topicItems()`
- `responsibilityItems()`
- `usageItems()`
- `timeItems()`
- `resourceItems()`
- `addContactItems()`
- `officeLabel()`
- `starterRecipes()`
- `downloadLead()`

Wenn eine dieser Methoden entgegen der Analyse noch produktiv verwendet wird, ist die Verwendung zuerst auf die beschriebenen direkten Methoden umzustellen. Sie darf nicht vorsorglich bestehen bleiben.

### 9.6 Templates und Controller

#### Detailseiten

- `datasetDetail.jte` und `issueDetail.jte` durch `entryDetail.jte` ersetzen.
- Die drei Detailrouten in `CatalogDetailController` liefern `pages/entryDetail` für Dataset- und Issue-Darstellung.
- Serienseiten verwenden weiterhin ihr reduziertes eigenes Modell und Template.
- Keine bedingte Template-Hierarchie oder Vererbung einführen.

#### Komponenten

- `relatedIssuesCard.jte` nimmt direkt `List<RelatedIssueVm>` entgegen.
- `seriesIssues.jte` nimmt direkt `List<SeriesIssueVm>` entgegen.
- Der Titel `Ausgaben` bleibt statisch im Template.
- `detailFeatureDownloadStrip.jte` erhält direkt `AccessStateVm` und `List<DownloadLinkVm>`.
- `detailHero.jte` behält nur tatsächlich gerenderte Parameter:
  - `title`
  - `description`
  - `titleBadgeLabel`

#### Tote Templates

Folgende Templates werden entfernt:

- `detailBadges.jte`
- `detailDownloadPanel.jte`
- `detailTeaserRow.jte`
- `contactMetadataSection.jte`
- `usageStarterRecipes.jte`

#### Tote Assets

Die vier ausschließlich für Scheinlinks verwendeten Recipe-PNGs werden entfernt.

Vor dem Löschen ist mit `rg` sicherzustellen, dass sie nicht anderweitig produktiv verwendet werden. Es werden keine Ersatzbilder ergänzt.

#### Dokumentation

`docs/ui-implementation-contract.md` und `docs/component-map.md` werden an die konsolidierten ViewModels und Templates angepasst.

Der Vertrag hält ausdrücklich fest:

- Starter-Rezepte dürfen erst mit echten Ziel-URLs wieder eingeführt werden.
- Ein vorbereiteter Link auf `#` ist nicht zulässig.
- Dataset und Issue verwenden dasselbe Detailseiten-ViewModel und dasselbe Template.

### 9.7 Tests

Mindestens folgende Tests und Prüfungen durchführen:

- Bestehende Factory-Tests auf `EntryDetailPageVm` umstellen.
- Bestehende MVC-Tests auf `pages/entryDetail` umstellen.
- Dataset-Seite rendert weiterhin dieselben fachlichen Inhalte.
- Issue-Seite rendert weiterhin dieselben fachlichen Inhalte.
- Kontaktzeilen mit normalem Text testen.
- Kontaktzeilen mit HTTP-Link testen.
- Kontaktzeilen mit Mail-Link testen.
- Gemischte Kontaktzeilen in einem Metadatenpunkt testen.
- Serienausgaben behalten ihre Reihenfolge.
- Related Issues behalten ihre Reihenfolge.
- Usage-Seite enthält weiterhin Direktzugriff und Codebeispiele.
- Usage-Seite enthält keine Starter-Rezepte.
- Usage-Seite enthält kein `href="#"`.
- Listen- und Kartenansicht rendern weiterhin mit `EntryRowVm` und `EntryCardVm`.
- Single- und Multi-Select-Filter funktionieren nach Entfernung von `FilterGroupType` unverändert.

Zusätzliche statische Prüfungen:

- Höchstens 34 Dateien im Package `web/view`.
- Keine Referenzen auf gelöschte Typen.
- Keine Referenzen auf gelöschte Templates.
- Keine Referenzen auf gelöschte Recipe-PNGs.
- Keine unbenutzten JTE-Komponenten.

### 9.8 Definition of Done

- Die Zahl der ViewModel-Dateien ist von 46 auf höchstens 34 reduziert.
- Alle aufgelisteten unnötigen Wrapper-Typen sind entfernt.
- Es existieren nur die zwei ausdrücklich zugelassenen neuen ViewModels.
- Keine zusätzliche Factory, Mapper-Hierarchie oder Basisklasse.
- Dataset- und Issue-Seiten verwenden `EntryDetailPageVm` und `entryDetail.jte`.
- Keine unbenutzten JTE-Komponenten.
- Keine `#`-Links für nicht existierende Anleitungen.
- Fachliche Darstellung und Reihenfolgen bleiben erhalten.
- `git diff --check` ist erfolgreich.
- `./gradlew clean check` ist erfolgreich.
- Erst danach darf Phase 6 beginnen.

## 10. Phase 6 – Fertige `catalog.duckdb` einmal laden und konsistent veröffentlichen

### 10.1 Ziel

Die Anwendung lädt XTF und die extern erzeugte DuckDB-Datei als gemeinsamen Runtime-Stand. HTTP-Requests dürfen die DuckDB-Quelle nicht bei jedem Abruf erneut lesen. Ein Reload veröffentlicht XTF, Lucene-Index und DuckDB-Datei nur gemeinsam.

Diese Phase betrifft ausschließlich das Laden, minimale technische Prüfen, Speichern und Ausliefern einer bereits fertigen `catalog.duckdb`.

### 10.2 Änderungen auf Klassen- und Methodenebene

#### `CatalogSnapshot`

Der aktive Snapshot enthält zusätzlich beide geladenen Artefakte:

```java
CatalogBytes publishedCatalog
CatalogBytes duckDbCatalog
```

- `sourceDescription()` wird aus `publishedCatalog` abgeleitet.
- `contentHash()` wird aus `publishedCatalog` abgeleitet.
- Die vorhandenen fachlichen Snapshot-Daten und der Suchindex bleiben Teil desselben Snapshots.
- Alte Factory-Overloads ohne Artefakte werden entfernt.
- Keine neue `CatalogPublication`-Wrapperklasse einführen.
- Der Snapshot bleibt nach außen unveränderlich.

#### `CatalogSnapshotBuilder`

Neue Signatur:

```java
CatalogBuildResult build(
    CatalogBytes publishedCatalog,
    CatalogBytes duckDbCatalog)
```

Vor dem Indexaufbau wird die DuckDB-Datei minimal technisch geprüft:

- mindestens zwölf Bytes;
- ASCII-Marker `DUCK` an Byteposition 8 bis 11.

Ein ungültiges Artefakt wirft `CatalogSourceException` mit einer verständlichen, nicht geheimen Fehlermeldung.

Ausdrücklich nicht ergänzen:

- DuckDB-JDBC-Abhängigkeit;
- Schemaanalyse;
- Tabellen- oder Spaltenprüfung;
- Vergleich der fachlichen Inhalte mit XTF;
- Erzeugung oder Reparatur der Datei.

#### `CatalogSnapshotLoader`

- Die primäre XTF-Quelle injizieren.
- Die qualifizierte `catalogDuckDbSource` injizieren.
- `load()` lädt beide Artefakte genau einmal.
- `load()` übergibt beide Artefakte gemeinsam an den Builder.
- Bei einem Fehler wird kein partieller Snapshot zurückgegeben.

#### `CatalogReloadService`

- Beide Quellen vor dem Build laden.
- Erst nach erfolgreichem Download beider Artefakte fortfahren.
- XTF parsen und validieren.
- DuckDB-Minimalprüfung durchführen.
- Lucene-Index vollständig aufbauen.
- Erst danach den neuen Snapshot atomar publizieren.
- Scheitert einer dieser Schritte, bleiben alter Snapshot, alter Index und alte DuckDB-Datei gemeinsam aktiv.
- Neu erzeugte, aber nicht publizierte Ressourcen kontrolliert schließen.
- Keine verteilte Transaktions- oder Publication-Manager-Abstraktion ergänzen.

#### `CatalogArtifactController`

Der Controller injiziert nur noch `CatalogService`.

`publishedCatalog()` und `duckDbCatalog()` müssen:

- innerhalb von `CatalogService.withSnapshot()` auf den aktiven Snapshot zugreifen;
- das jeweilige Artefakt aus dem Snapshot lesen;
- keine Source pro GET neu laden;
- `InputStreamResource` auf `CatalogBytes.inputStream()` verwenden;
- keine weitere große Bytekopie anlegen;
- den SHA-256-Hash als ETag verwenden;
- eine korrekte `Content-Length` liefern.

Caching-Verhalten für DuckDB:

- Unversionierter Aufruf bleibt `no-cache`.
- Explore verwendet `/catalog/catalog.duckdb?v=<sha256>`.
- Ein passender Versionsparameter erhält langes `public, immutable` Cache-Control.
- Ein veralteter oder falscher Versionsparameter ergibt HTTP 409.
- Die Anwendung liefert bei falscher Version nicht still die aktuelle Datenbank aus.
- Passendes `If-None-Match` ergibt HTTP 304.

Dasselbe ETag-Grundprinzip ist für das veröffentlichte XTF anzuwenden, soweit die bestehende Route dies bereits vorsieht. Keine generische Artifact-Controller-Hierarchie ergänzen.

#### `ExploreContextService`

- `buildContext()`-Varianten erhalten den zugehörigen `CatalogSnapshot`.
- Die DuckDB-URL wird aus `snapshot.duckDbCatalog().contentHash()` gebildet.
- Kontext und Artefakt stammen damit garantiert aus demselben Snapshot.
- Keine ungeschützte Snapshot-Abfrage außerhalb von `withSnapshot()` einführen.

#### `ExploreContextDto`

- Keinen hartcodierten Fallback auf die unversionierte DuckDB-URL mehr erzeugen.
- Die vom Backend gelieferte versionierte URL ist verpflichtend.
- Keine Clientlogik zum Erraten eines Hashes ergänzen.

#### Health

Interne Snapshot-Details werden ergänzt um:

- XTF-Hash;
- DuckDB-Hash;
- DuckDB-Größe;
- Ladezeit.

Quellpfade werden weiterhin nicht öffentlich angezeigt. Die öffentliche Health-Antwort bleibt gemäß Phase 3 reduziert.

### 10.3 Konsistenzgrenze

Die Anwendung kann ohne externes Release-Manifest nicht beweisen, dass XTF und `catalog.duckdb` fachlich denselben Datenstand enthalten.

Deshalb gilt:

- Die externe Pipeline ist verantwortlich, zueinander passende Artefakte bereitzustellen.
- Die Anwendung garantiert nur, dass die gemeinsam geladenen Artefakte mit dem daraus gebauten Index atomar aktiviert werden.
- Diese Grenze wird in der Betriebs- und Konfigurationsdokumentation ausdrücklich festgehalten.
- Es wird in dieser Phase kein Release-Manifest-Format erfunden.

### 10.4 Tests

Mindestens folgende Tests ergänzen:

- Startup lädt jede Source genau einmal.
- Mehrere Artifact-GETs erhöhen den Source-Load-Counter nicht.
- XTF-Antwort liefert korrektes ETag.
- DuckDB-Antwort liefert korrektes ETag.
- Passendes `If-None-Match` ergibt 304.
- Unversionierter DuckDB-Aufruf ist `no-cache`.
- Passender Versionsparameter ergibt langes `public, immutable` Cache-Control.
- Falscher Versionsparameter ergibt 409.
- `Content-Length` entspricht der gespeicherten Artefaktgröße.
- DuckDB-Fehler beim Reload hält beide alten Artefakte aktiv.
- XTF-Fehler beim Reload hält beide alten Artefakte aktiv.
- Indexfehler beim Reload hält beide alten Artefakte aktiv.
- Eine Datei mit weniger als zwölf Bytes verhindert Startup beziehungsweise Reload.
- Fehlender `DUCK`-Marker verhindert Startup beziehungsweise Reload.
- Explore-Kontext enthält den aktiven DuckDB-Hash.
- Reload wechselt Kontext-URL und ausgeliefertes Artefakt gemeinsam.
- Ein Request während des Reloads sieht vollständig den alten oder vollständig den neuen Snapshot, nie eine Mischung.

### 10.5 Definition of Done

- Keine Erzeugung oder Transformation von `catalog.duckdb`.
- Kein Source-Zugriff pro HTTP-Request.
- XTF, Lucene und DuckDB werden als ein Runtime-Stand aktiviert.
- Fehlgeschlagene Reloads lassen den gesamten alten Stand aktiv.
- Explore verwendet eine inhaltshash-versionierte DuckDB-URL.
- ETag, 304, 409 und Cache-Control entsprechen der Spezifikation.
- Die semantische Gleichheit von XTF und DuckDB ist als Verantwortung der externen Pipeline dokumentiert.
- `git diff --check` ist erfolgreich.
- `./gradlew clean check` ist erfolgreich.
- Erst danach darf Phase 7 beginnen.

## 11. Phase 7 – Explore-Artefakte drastisch verkleinern

### 11.1 Ziel

Nur tatsächlich verwendete DuckDB-Wasm-Varianten und Produktionsartefakte werden ausgeliefert. R-Code wird erst geladen, wenn das R-Labor geöffnet wird. Produktions-Source-Maps werden nicht in das Boot-JAR aufgenommen.

### 11.2 Änderungen auf Modul- und Komponentenebene

#### `duckdbBundles.ts`

Alle EH- und COI-Imports entfernen:

- EH-Wasm;
- EH-Worker;
- COI-Wasm;
- COI-Worker;
- COI-Pthread-Worker.

Zusätzlich:

- `bundledDuckDbAssetUrls` entfernen.
- `createLocalDuckDbBundles()` liefert ausschließlich MVP.
- Keine automatische Browsererkennung zur vorsorglichen Auswahl entfernter Bundles ergänzen.
- Keine Feature-Flag zur Reaktivierung der Varianten ergänzen.

#### `vite.config.ts`

- `build.sourcemap` auf `false` setzen.
- Keine neue Umgebungsvariable nur zur Wiedereinführung produktiver Source Maps ergänzen.
- Vorhandene Development-Source-Maps außerhalb des Produktionsbuilds dürfen unverändert bleiben, sofern sie nicht ins JAR gelangen.

#### `ExploreApp.tsx`

- `RPanel` mit `React.lazy()` dynamisch importieren.
- `RDataFramePanel` mit `React.lazy()` dynamisch importieren.
- Beide Komponenten erst rendern, nachdem `rLabMounted` wahr ist.
- Eine kleine lokale `Suspense`-Fallbackanzeige verwenden.
- SQL-Labor, Schema und DuckDB bleiben im initialen Chunk.
- Keine allgemeine Route- oder Module-Loader-Abstraktion ergänzen.

#### Build

- Bestehende Precompression bleibt erhalten.
- Keine neue Asset-Budget-Task einführen.
- Keine zusätzliche Build-Frameworkklasse einführen.
- Die Abnahme erfolgt direkt am erzeugten Boot-JAR.

### 11.3 Tests und Artefaktprüfung

#### Unit- und Komponententests

- `duckdbBundles.test.ts` prüft exakt einen Key `mvp`.
- Der Test stellt sicher, dass keine EH- oder COI-URL enthalten ist.
- Vitest bestätigt, dass das R-Labor vor der Tabaktivierung nicht geladen beziehungsweise nicht gemountet wird.
- Vitest bestätigt, dass die R-Komponenten nach Tabaktivierung geladen und gerendert werden.
- SQL-Smoke bleibt erfolgreich.
- R-Playwright-Smoke bleibt erfolgreich.

#### JAR-Inhaltsprüfung

Nach dem Produktionsbuild wird das erzeugte JAR direkt mit `jar tf` geprüft.

Es darf nichts enthalten, das auf Folgendes passt:

- `duckdb-eh`
- `duckdb-coi`
- `coi.pthread`
- `.map`

Zusätzlich muss:

- ein eigener R-Chunk vorhanden sein;
- der initiale Explore-Chunk die R-Komponenten nicht enthalten;
- ausschließlich das MVP-DuckDB-Wasm-Bundle ausgeliefert werden.

#### Größenprüfung

- Ausgangswert: rund 224 MiB.
- Das Boot-JAR muss mindestens 40 MiB kleiner sein.
- Das Boot-JAR darf höchstens 180 MiB groß sein.
- Die genaue Vorher-/Nachher-Größe wird im Phasenprotokoll dokumentiert.
- Die Prüfung erfolgt auf demselben Build-Artefakttyp und mit demselben Gradle-Task.

### 11.4 Definition of Done

- Nur MVP-Wasm wird ausgeliefert.
- Keine produktiven Source Maps im Boot-JAR.
- Die R-Oberfläche liegt nicht im initialen Explore-Chunk.
- Ein eigener R-Chunk ist vorhanden.
- JAR-Inhaltsprüfung ist erfolgreich.
- Das Boot-JAR ist mindestens 40 MiB kleiner als der dokumentierte Ausgangswert und höchstens 180 MiB groß.
- SQL- und R-Smokes sind erfolgreich.
- `git diff --check` ist erfolgreich.
- `./gradlew clean check` ist erfolgreich.
- Der reale WebR-Smoke ist erfolgreich.
- Erst danach darf Phase 8 beginnen.

## 12. Phase 8 – Vorbereiteten Explore-Zukunftscode entfernen

### 12.1 Ziel

Nicht eingebundene UI, nicht aktive Feature Flags und manuell gepflegter Zukunftscode werden gelöscht. Der Explore-Kontext beschreibt danach nur produktive Fähigkeiten. Aktive SQL-Rezepte und das produktive R-Labor bleiben erhalten.

### 12.2 Zu löschender Java-Code

Folgende Typen werden gelöscht:

- `ExploreCodeSnippetDto`
- `ExploreCodeSnippetService`
- `ExploreSnippetLanguage`
- `ExploreContextSource`
- `ExploreFeatureFlagsDto`
- `ExploreContextJsonWriter`
- die ausschließlich zu diesen Typen gehörenden Tests.

Vor dem Löschen ist mit `rg` zu prüfen, dass alle produktiven Aufrufer im Rahmen dieser Phase umgestellt werden. Keine Deprecated-Klassen und keine V3-Kompatibilitätswrapper beibehalten.

### 12.3 Zu löschender TypeScript-Code

Folgende Module und zugehörige Tests werden gelöscht:

- `FutureExtensionSlots.tsx`
- `CodeSnippetsPanel.tsx`
- `RecipeList.tsx`
- `QueryHistory.ts`

Produktive SQL-Rezepte, die tatsächlich in der Anwendung verwendet werden, sind nicht mit `RecipeList.tsx` gleichzusetzen und bleiben erhalten.

### 12.4 Build und Dokumentation

Folgendes wird entfernt:

- `scripts/checkFutureDeps.mjs`
- `check:future-deps` aus `package.json`
- Dokumentation dieses Zukunfts-Guards
- Aussagen, dass AI, Vega, Mosaic, Geospatial oder lokale History bereits vorbereitet seien.

Es wird kein Ersatz-Guard für nicht vorhandene Zukunftsfunktionen eingeführt.

### 12.5 Explore-Kontext V4

#### `ExploreContextDto`

Folgende Felder werden entfernt:

- `codeSnippets`
- `featureFlags`

Stattdessen enthält der Kontext nur die aktiven Flags:

```java
boolean chartsEnabled
boolean webREnabled
```

- Kontextversion von 3 auf 4 erhöhen.
- Keine V3-Kompatibilitätsschicht ergänzen, weil Backend und Island gemeinsam ausgeliefert werden.
- Die übrigen produktiv genutzten Kontextfelder bleiben erhalten.

#### `ExploreProperties`

Folgende Felder und Methoden entfernen:

- `localHistoryEnabled`
- `aiEnabled`
- `vegaEnabled`
- `mosaicEnabled`
- `geospatialEnabled`
- `featureFlags()`

Behalten werden:

- `chartsEnabled`
- `webrEnabled`

Der Frontend-Kontext verwendet anschließend direkt:

- `context.chartsEnabled`
- `context.webREnabled`

Keine Map generischer Feature Flags einführen.

### 12.6 JSON-Serialisierung vereinfachen

#### `ExploreContextService`

- Den vorhandenen Spring-`ObjectMapper` injizieren.
- `toEmbeddableJson()` ruft `objectMapper.writeValueAsString(context)` auf.
- Danach ausschließlich Schutz für folgende Sequenzen anwenden:
  - `</script>`
  - `<!--`
  - `-->`
- `JacksonException` als `IllegalStateException` weitergeben.
- Keine neue Serializerklasse erstellen.
- Keine manuelle Feld-für-Feld-JSON-Erzeugung beibehalten.

#### DTO-Annotationen

- Optionale DTO-Felder erhalten `@JsonInclude(NON_ABSENT)`.
- `ExploreColumnRole` erhält `@JsonValue` auf der vorhandenen Wertrepräsentation.
- `ExploreChartType` erhält `@JsonValue` auf der vorhandenen Wertrepräsentation.
- `ExploreRecipeCategory.value()` erhält `@JsonValue`.
- Enumwerte bleiben dadurch in der bestehenden kleingeschriebenen Vertragsform.
- Keine Custom-Serializer ergänzen.

#### TypeScript-Zod-Schema

- Nur Kontextversion 4 akzeptieren.
- `chartsEnabled` und `webREnabled` direkt abbilden.
- Entfernte Snippet- und Feature-Flag-Felder nicht mehr akzeptieren.
- V3 mit verständlichem Schemafehler ablehnen.
- Keine Transformationsschicht von V3 auf V4 ergänzen.

### 12.7 Tests

Mindestens folgende Tests und statischen Prüfungen durchführen:

- Java-Kontexttest erwartet Version 4.
- Java-Kontexttest erwartet `chartsEnabled`.
- Java-Kontexttest erwartet `webREnabled`.
- TypeScript-Zod-Schema akzeptiert einen vollständigen V4-Kontext.
- V3 wird kontrolliert abgelehnt.
- JSON serialisiert Quotes korrekt.
- JSON serialisiert Backslashes korrekt.
- JSON serialisiert Zeilenumbrüche korrekt.
- JSON serialisiert Unicode korrekt.
- Leere Optionals werden ausgelassen.
- Enumwerte bleiben kleingeschrieben.
- `</script>` kann das eingebettete Script nicht schließen.
- `<!--` und `-->` können den eingebetteten Block nicht manipulieren.
- `rg` findet keine Referenzen auf gelöschte Flags.
- `rg` findet keine Referenzen auf gelöschte Snippet-Typen oder Komponenten.
- `rg` findet keine Referenzen auf `QueryHistory` oder `FutureExtensionSlots`.
- Produktive SQL-Rezepte bleiben vorhanden und getestet.
- Das aktive R-Labor bleibt vorhanden und getestet.

### 12.8 Definition of Done

- Kein ungenutzter Zukunfts-Slot.
- Keine Zukunfts-Feature-Flag.
- Kein manueller JSON-Writer.
- Keine toten Tests oder Dokumentationsbehauptungen.
- Kontext V4 wird von Java und TypeScript identisch verstanden.
- Keine V3-Kompatibilitätsschicht.
- Produktive SQL-Rezepte und das R-Labor funktionieren weiterhin.
- `git diff --check` ist erfolgreich.
- `./gradlew clean check` ist erfolgreich.
- `./gradlew clean check -Ddatenportal.playwright.webr=true` ist erfolgreich.

## 13. Gesamt-Definition-of-Done

Die gesamte Bereinigung ist erst abgeschlossen, wenn alle folgenden Bedingungen erfüllt sind:

- Alle acht Phasen wurden in der vorgegebenen Reihenfolge abgeschlossen.
- Für jede Phase liegt ein vollständiges Abschluss- und Testprotokoll vor.
- Jede Phase hat ihr eigenes Quality Gate bestanden, bevor die nächste begonnen wurde.
- `git diff --check` ist erfolgreich.
- `./gradlew clean check` läuft zweimal hintereinander erfolgreich.
- `./gradlew clean check -Ddatenportal.playwright.webr=true` ist erfolgreich.
- Der reale WebR-Smoke ist erfolgreich.
- Keine entfernten Symbole werden noch referenziert.
- Keine neuen ungenutzten Klassen, Templates oder Komponenten sind entstanden.
- Die ViewModel-Anzahl beträgt höchstens 34 Dateien.
- Das Boot-JAR erfüllt die Inhalts- und Größengrenzen aus Phase 7.
- Konfigurations-, Betriebs-, Such-, UI- und Explore-Dokumentation beschreibt das neue Verhalten korrekt.
- Kein Suchfehler wird als leere Treffermenge kaschiert.
- Kein produktiver Start verwendet unbemerkt Demo- oder localhost-Defaults.
- XTF, Lucene-Index und fertige DuckDB-Datei werden gemeinsam aktiviert.
- Runtime-Cleanup und Query-Abbruch sind deterministisch.
- Keine Scheinlinks auf `#` verbleiben in den von den Phasen betroffenen Bereichen.
- Die Herstellung von `catalog.duckdb` ist nirgends Teil der Implementierung geworden.
- Es wurde keine unnötige neue Architektur zur Umsetzung der Bereinigung eingeführt.

## 14. Die jeweils drei zwingenden Verbesserungen

### 14.1 Java inklusive serverseitigem Frontend

1. **Suchfehler und die 500-Treffer-Vorfilterung beseitigen.** Lucene-Fehler müssen als 503 sichtbar werden, und Java-Filter müssen auf der vollständigen Treffermenge arbeiten.
2. **Produktionsunsichere Demo-Defaults und öffentlich sichtbare Betriebsdetails entfernen.** Ein Produktionsstart benötigt explizite Quellen; öffentliche Health-Antworten dürfen keine internen Details oder Pfade offenlegen.
3. **UI-Vertrag und tatsächliche Datenabbildung korrigieren.** Dazu gehören der blaue Typ-Badge, das korrekte Struktur-Badge, die dokumentierte Serienzeilen-Interaktion und echte Qualitätsreport-Links.

Die ViewModel-Bereinigung ist als Phase 5 verbindlich, besitzt aber eine geringere Betriebspriorität als diese drei Korrektheits- und Transparenzprobleme.

### 14.2 Explore-Insel

1. **Die fertige `catalog.duckdb` einmal laden und atomar mit XTF und Index veröffentlichen.** Requests dürfen die Source nicht erneut lesen, und Reloads dürfen keinen gemischten Datenstand sichtbar machen.
2. **DuckDB-, Query- und WebR-Lebenszyklen sowie die Testdeterministik korrigieren.** Cleanup, Cancel, Timeout und Unmount müssen kontrolliert und reproduzierbar funktionieren.
3. **Unbenutzte Wasm-Varianten, Source Maps und eager geladenen R-Code aus dem Produktionsartefakt entfernen.** Das JAR muss nur produktiv benötigte Varianten enthalten und die festgelegte Größengrenze einhalten.

Das Entfernen des vorbereiteten Zukunftscodes ist als Phase 8 verbindlich, aber nachrangig gegenüber Datenkonsistenz, Runtime-Stabilität und Payload.

## 15. Annahmen und explizite Nicht-Ziele

### 15.1 Annahmen

- Der gewählte Umfang sind vier Bereinigungen je Anwendungsteil.
- Der Explore-Kontext ist ein gemeinsam ausgelieferter interner Vertrag; V3-Kompatibilität ist nicht erforderlich.
- Die externe Pipeline liefert zueinander passende XTF- und DuckDB-Artefakte beziehungsweise release-spezifische URLs.
- Java-Backend und Explore-Frontend werden gemeinsam gebaut und ausgeliefert.
- Die bestehenden fachlichen Suchfilter und Sortierregeln bleiben gültig, sofern diese Spezifikation sie nicht ausdrücklich ändert.

### 15.2 Nicht-Ziele

- Keine Erstellung von `catalog.duckdb`.
- Keine Aktualisierung oder Reparatur von `catalog.duckdb`.
- Keine fachliche Schema- oder Inhaltsprüfung von `catalog.duckdb`.
- Keine Einführung eines Release-Manifests.
- Kein vollständiges Refactoring der Anwendung.
- Keine neue allgemeine Architektur für Lifecycle, Fehler, ViewModels, Artefakte oder Feature Flags.
- Keine Wiedereinführung entfernter Zukunftsfunktionen ohne einen konkreten produktiven Anwendungsfall.
- Keine Commits ohne gesonderten Auftrag.
