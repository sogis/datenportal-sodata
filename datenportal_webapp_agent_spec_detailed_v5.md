# Datenportal-Webanwendung Kanton Solothurn — detaillierte Agenten-Spezifikation v5

Status: vollständige Arbeitsfassung v5, basierend auf v3 plus verbindlichem UI-Contract v5  
Ziel: Umsetzung durch einen LLM-Coding-Agenten in kleinen, lauffähigen, getesteten Phasen; UI-Anforderungen sind verbindlich in Abschnitt 14 und `docs/ui-implementation-contract.md` konkretisiert  
Package-Basis: `ch.so.agi.datenportal`  
Zielplattform: Java 25, Spring Boot 4.x, Gradle Groovy, JTE, HTMX, Apache Lucene  
Laufzeitdatenquelle: XTF/XML gemäss `SO_AGI_DataCatalog_PublishedCatalog_20260602.ili`


**Revision v5:** Diese Datei ist wieder die vollständige Spezifikation mit Architektur, Klassen-/Methodenvorschlägen, Parser, Suche, Tests und Phasenplan. Gegenüber v3 wurde der UI-Teil durch den verbindlichen UI-Implementation-Contract ersetzt. Die frühere verkürzte v4-Spezifikation ist nicht als Ersatz für diese Vollspezifikation zu verwenden.

---

## 1. Architekturentscheid

Die Anwendung soll als **klassische serverseitig gerenderte Spring-Boot-Webanwendung** gebaut werden. Die Kernidee ist ein immutable `CatalogSnapshot`: Beim Start oder Reload wird die PublishedCatalog-XTF-Datei vollständig gelesen, fachlich normalisiert, validiert, in ein internes Read-Model überführt und zusammen mit einem Lucene-Index atomar veröffentlicht.

Es gibt im MVP **keine Datenbank** und keine fachliche Schreibfunktion. Dadurch bleibt die Anwendung betrieblich einfach: Pipeline erzeugt XTF, Anwendung liest XTF, Weboberfläche zeigt Daten und Links.

### 1.1 Schichten

```text
Browser
  |
  |  HTML, CSS, HTMX, Web Components
  v
web layer
  - Controller
  - View Models
  - JTE Templates
  - HTMX fragments
  |
  v
application layer
  - CatalogService
  - CatalogReloader
  - SearchService
  - FacetService
  |
  v
domain/read model
  - CatalogSnapshot
  - CatalogEntry
  - DatasetEntry
  - DatasetSeriesEntry
  - DatasetIssueEntry
  - DistributionLink
  |
  v
infrastructure
  - XtfPublishedCatalogParser
  - CatalogSource implementations
  - LuceneSearchIndex
  - Web component static assets
```

### 1.2 Leitlinien

- Der Controller kennt keine XTF-Details.
- JTE-Templates erhalten ausschliesslich View Models, keine Parser- oder Lucene-Objekte.
- Die Domain-Records sind immutable und möglichst frei von Spring-Abhängigkeiten.
- Parser und Validator liefern verständliche Fehler und Warnungen.
- Ein Reload darf den aktuell aktiven Katalog nie beschädigen.
- Ohne JavaScript muss Suche/Filterung per normalem GET-Request funktionieren.
- HTMX verbessert nur das Nutzererlebnis; es ist keine fachliche Voraussetzung.
- Web Components werden für Header und Breadcrumb eingebunden, aber die Anwendung bleibt serverseitig kontrolliert.

---

## 2. Zielstruktur des Repositories

```text
.
├─ AGENTS.md
├─ README.md
├─ build.gradle
├─ settings.gradle
├─ gradle/
├─ docs/
│  ├─ architecture.md
│  ├─ configuration.md
│  ├─ catalog-source.md
│  ├─ xtf-parser.md
│  ├─ search.md
│  ├─ web-components.md
│  ├─ ui-design.md
│  ├─ operations.md
│  └─ testing.md
├─ src/
│  ├─ main/
│  │  ├─ java/ch/so/agi/datenportal/
│  │  ├─ jte/
│  │  └─ resources/
│  │     ├─ application.yml
│  │     ├─ static/
│  │     └─ catalog/
│  └─ test/
│     ├─ java/ch/so/agi/datenportal/
│     └─ resources/catalog/
└─ .agents/
   └─ skills/
```

---

## 3. Java-Paketstruktur

```text
src/main/java/ch/so/agi/datenportal/
  DatenportalApplication.java

  config/
    CatalogProperties.java
    ReloadProperties.java
    SearchProperties.java
    WebComponentsProperties.java
    WebConfig.java
    CatalogSourceConfig.java
    SearchConfig.java
    TemplateSupportConfig.java

  catalog/
    CatalogService.java
    CatalogReloader.java
    StartupCatalogLoader.java
    CatalogLoadReport.java
    CatalogWarning.java
    CatalogValidationException.java
    CatalogValidator.java
    FacetService.java

  catalog/domain/
    Catalog.java
    CatalogSnapshot.java
    CatalogEntry.java
    CatalogEntryType.java
    DatasetEntry.java
    DatasetSeriesEntry.java
    DatasetIssueEntry.java
    DistributionLink.java
    DistributionFormat.java
    Office.java
    ContactPoint.java
    ThemeAssignment.java
    AccessRights.java
    AccrualPeriodicity.java
    TemporalCoverage.java
    PublicationStatus.java
    Origin.java

  catalog/source/
    CatalogSource.java
    CatalogBytes.java
    CatalogSourceException.java
    ClasspathCatalogSource.java
    FileCatalogSource.java
    HttpCatalogSource.java
    MockHttpCatalogSource.java

  catalog/parse/
    PublishedCatalogParser.java
    XtfPublishedCatalogParser.java
    XtfParseException.java
    XtfParserContext.java
    XtfElementPath.java
    XmlSecurity.java
    MutableCatalogBuilder.java
    MutableDataResourceBuilder.java

  search/
    SearchService.java
    SearchIndex.java
    SearchIndexBuilder.java
    LuceneSearchIndex.java
    LuceneSearchService.java
    SearchQuery.java
    SearchFilters.java
    SearchResultPage.java
    SearchResultItem.java
    SortMode.java
    ModifiedRange.java
    PageRequest.java
    PageSlice.java

  web/
    HomeController.java
    DatasetController.java
    ErrorPageController.java
    HtmxRequest.java
    CatalogQueryParams.java
    DownloadUrlResolver.java
    HeaderViewModelFactory.java
    BreadcrumbFactory.java
    ViewMode.java

  web/view/
    PageChromeVm.java
    HeaderVm.java
    NavItemVm.java
    BreadcrumbVm.java
    BreadcrumbItemVm.java
    HomePageVm.java
    ResultsVm.java
    EntryCardVm.java
    EntryRowVm.java
    DownloadButtonVm.java
    FilterPanelVm.java
    FilterOptionVm.java
    DatasetDetailPageVm.java
    SeriesDetailPageVm.java
    MetadataGroupVm.java
    MetadataRowVm.java
    IssueRowVm.java
    StatusBadgeVm.java

  admin/
    AdminCatalogController.java
    ReloadTokenVerifier.java
    ReloadResponse.java
    CatalogStatusResponse.java

  support/
    ContentHash.java
    DateFormatters.java
    IdentifierPath.java
    JsonAttributeEncoder.java
    MimeTypes.java
    SafeHtml.java
    TextNormalizer.java
    Urls.java
```

---

## 4. Konfiguration

### 4.1 `application.yml`

```yaml
datenportal:
  catalog:
    source: classpath # classpath | file | http | mock-http
    classpath-location: catalog/published_catalog_full_54_entries.xtf
    file-location: ./config/catalog.xtf
    http-url: http://localhost:18080/catalog.xtf
    connect-timeout: 5s
    read-timeout: 30s
    show-only-published: true
    show-only-open-data: true
    fail-on-warnings: false
  reload:
    enabled: true
    token: ${DATENPORTAL_RELOAD_TOKEN:dev-token-change-me}
  search:
    max-results: 500
    default-page-size: 10
    max-page-size: 100
    default-sort: modified-desc
  web-components:
    enabled: true
    asset-base-path: /vendor/so-web-components/0.1.10
    use-cdn: false
    version: 0.1.10
```

### 4.2 `CatalogProperties`

```java
package ch.so.agi.datenportal.config;

@ConfigurationProperties(prefix = "datenportal.catalog")
public record CatalogProperties(
    SourceType source,
    String classpathLocation,
    Path fileLocation,
    URI httpUrl,
    Duration connectTimeout,
    Duration readTimeout,
    boolean showOnlyPublished,
    boolean showOnlyOpenData,
    boolean failOnWarnings
) {
    public enum SourceType { CLASSPATH, FILE, HTTP, MOCK_HTTP }
}
```

Methoden/Validierung:

```java
public CatalogProperties {
    // Defaults und Plausibilisierung in Konstruktor oder via @Validated.
}

public boolean isClasspathSource();
public boolean isFileSource();
public boolean isHttpSource();
```

### 4.3 `ReloadProperties`

```java
@ConfigurationProperties(prefix = "datenportal.reload")
public record ReloadProperties(
    boolean enabled,
    String token
) {
    public boolean hasUsableToken();
}
```

Akzeptanz:

- In Produktion darf `dev-token-change-me` nicht akzeptiert werden, wenn ein Profil `prod` aktiv ist.
- Bei leerem Token ist `/admin/catalog/reload` deaktiviert oder gibt `503 Service Unavailable` zurück.

### 4.4 `SearchProperties`

```java
@ConfigurationProperties(prefix = "datenportal.search")
public record SearchProperties(
    int maxResults,
    int defaultPageSize,
    int maxPageSize,
    SortMode defaultSort
) {
    public PageRequest normalize(PageRequest pageRequest);
}
```

### 4.5 `WebComponentsProperties`

```java
@ConfigurationProperties(prefix = "datenportal.web-components")
public record WebComponentsProperties(
    boolean enabled,
    String assetBasePath,
    boolean useCdn,
    String version
) {
    public String indexJsPath();
    public String resetCssPath();
    public String fontsCssPath();
    public String tokensCssPath();
}
```

Regel: Für produktionsnahe Deployments `useCdn=false`. Assets werden versioniert unter `src/main/resources/static/vendor/so-web-components/<version>/` abgelegt. Keine externen CDN-Abhängigkeiten im Betrieb.

---

## 5. Domain-/Read-Model

### 5.1 `CatalogSnapshot`

```java
package ch.so.agi.datenportal.catalog.domain;

public record CatalogSnapshot(
    Catalog catalog,
    List<CatalogEntry> visibleEntries,
    Map<String, CatalogEntry> visibleEntriesByIdentifier,
    Map<String, CatalogEntry> allEntriesByIdentifier,
    Facets facets,
    Instant loadedAt,
    String sourceDescription,
    String contentHash,
    CatalogLoadReport loadReport,
    SearchIndex searchIndex
) implements AutoCloseable {

    public static CatalogSnapshot empty(Clock clock);

    public Optional<CatalogEntry> findVisibleEntry(String identifier);

    public Optional<CatalogEntry> findAnyEntry(String identifier);

    public List<CatalogEntry> visibleEntriesSorted(SortMode sortMode);

    public int visibleDatasetCount();

    public int visibleSeriesCount();

    public int visibleIssueCount();

    public boolean isEmpty();

    @Override
    public void close();
}
```

Regeln:

- `visibleEntries` enthält normale `DatasetEntry` und `DatasetSeriesEntry`, aber keine einzelnen `DatasetIssueEntry` als Top-Level-Resultate.
- `allEntriesByIdentifier` enthält zusätzlich Issues, damit spätere Issue-Detailseiten möglich sind.
- `close()` schliesst den alten Lucene-Index nach erfolgreichem Swap.

### 5.2 `Catalog`

```java
public record Catalog(
    URI catalogUri,
    String title,
    String description,
    Office publisher,
    Optional<URI> homepage,
    Optional<LocalDate> modified,
    List<Office> agents,
    List<DatasetEntry> datasets,
    List<DatasetSeriesEntry> datasetSeries
) {
    public List<CatalogEntry> topLevelEntries();
    public int datasetCount();
    public int seriesCount();
    public int issueCount();
}
```

### 5.3 `CatalogEntry` sealed interface

```java
public sealed interface CatalogEntry permits DatasetEntry, DatasetSeriesEntry {
    URI resourceUri();
    String identifier();
    String title();
    String description();
    Office publisher();
    Office creator();
    ContactPoint contactPoint();
    List<ThemeAssignment> themes();
    List<String> keywords();
    Optional<URI> landingPage();
    Optional<LocalDate> issued();
    LocalDate modified();
    Optional<URI> licenseUri();
    AccessRights accessRights();
    PublicationStatus publicationStatus();
    Optional<Origin> origin();
    Optional<AccrualPeriodicity> accrualPeriodicity();
    Optional<TemporalCoverage> temporalCoverage();

    CatalogEntryType type();

    boolean isPublished();

    boolean isOpenData();

    List<DistributionLink> distributionsForListing();

    Optional<DistributionLink> distribution(DistributionFormat format);

    boolean hasDistribution(DistributionFormat format);

    String detailPath();

    String shortDescription(int maxCharacters);

    String searchText();

    List<String> themeLabels();
}
```

Implementationsregeln:

- `detailPath()` erzeugt `/datasets/{urlEncodedIdentifier}`.
- `shortDescription()` schneidet nicht mitten in HTML, weil Beschreibungen als Plain Text behandelt werden.
- `searchText()` ist eine Hilfsmethode für Tests und Fallback, nicht die Lucene-Quelle allein.

### 5.4 `DatasetEntry`

```java
public record DatasetEntry(
    URI resourceUri,
    String identifier,
    String title,
    String description,
    Office publisher,
    Office creator,
    ContactPoint contactPoint,
    List<ThemeAssignment> themes,
    List<String> keywords,
    Optional<URI> landingPage,
    Optional<LocalDate> issued,
    LocalDate modified,
    Optional<URI> licenseUri,
    AccessRights accessRights,
    PublicationStatus publicationStatus,
    Optional<Origin> origin,
    Optional<AccrualPeriodicity> accrualPeriodicity,
    Optional<TemporalCoverage> temporalCoverage,
    List<DistributionLink> distributions
) implements CatalogEntry {

    @Override
    public CatalogEntryType type();

    @Override
    public List<DistributionLink> distributionsForListing();

    @Override
    public Optional<DistributionLink> distribution(DistributionFormat format);

    public List<DistributionLink> primaryDistributions();
}
```

Regel: `primaryDistributions()` gibt in der Reihenfolge `CSV`, `XLSX`, `PARQUET` zurück, falls vorhanden. `OTHER` folgt nur auf Detailseiten.

### 5.5 `DatasetSeriesEntry`

```java
public record DatasetSeriesEntry(
    URI resourceUri,
    String identifier,
    String title,
    String description,
    Office publisher,
    Office creator,
    ContactPoint contactPoint,
    List<ThemeAssignment> themes,
    List<String> keywords,
    Optional<URI> landingPage,
    Optional<LocalDate> issued,
    LocalDate modified,
    Optional<URI> licenseUri,
    AccessRights accessRights,
    PublicationStatus publicationStatus,
    Optional<Origin> origin,
    Optional<AccrualPeriodicity> accrualPeriodicity,
    Optional<TemporalCoverage> temporalCoverage,
    List<DatasetIssueEntry> issues,
    Optional<String> currentIssueIdentifier
) implements CatalogEntry {

    @Override
    public CatalogEntryType type();

    public Optional<DatasetIssueEntry> currentIssue();

    public DatasetIssueEntry currentIssueOrThrow();

    public List<DatasetIssueEntry> issuesNewestFirst();

    @Override
    public List<DistributionLink> distributionsForListing();

    @Override
    public Optional<DistributionLink> distribution(DistributionFormat format);

    public String currentIssueLabelForDisplay();

    public int issueCount();
}
```

Regeln für `currentIssue()`:

1. Wenn genau ein Issue `isCurrentIssue=true` hat: dieses Issue.
2. Wenn mehrere Issues current sind: Issue mit neuestem `modified`, Warnung erzeugen.
3. Wenn kein Issue current ist: Issue mit neuestem `modified`, Warnung erzeugen.
4. Wenn keine Issues vorhanden sind: Validierungsfehler, Serie wird nicht übernommen.

### 5.6 `DatasetIssueEntry`

```java
public record DatasetIssueEntry(
    URI resourceUri,
    String identifier,
    String title,
    String description,
    Office publisher,
    Office creator,
    ContactPoint contactPoint,
    List<ThemeAssignment> themes,
    List<String> keywords,
    Optional<URI> landingPage,
    Optional<LocalDate> issued,
    LocalDate modified,
    Optional<URI> licenseUri,
    AccessRights accessRights,
    PublicationStatus publicationStatus,
    Optional<Origin> origin,
    Optional<AccrualPeriodicity> accrualPeriodicity,
    Optional<TemporalCoverage> temporalCoverage,
    List<DistributionLink> distributions,
    String issueLabel,
    boolean currentIssue
) {
    public Optional<DistributionLink> distribution(DistributionFormat format);

    public List<DistributionLink> primaryDistributions();

    public boolean isVisibleAsHistoricalIssue();
}
```

### 5.7 `DistributionLink`

```java
public record DistributionLink(
    URI distributionUri,
    URI accessUrl,
    Optional<URI> downloadUrl,
    DistributionFormat format
) {
    public URI preferredHref();

    public String displayLabel();

    public boolean isPrimaryFormat();

    public String cssModifier();
}
```

Regeln:

- `preferredHref()` gibt `downloadUrl` zurück, wenn vorhanden, sonst `accessUrl`.
- `displayLabel()` ist `CSV`, `XLSX`, `Parquet`, `Weitere`.
- Download-Links werden nie synthetisch geraten.

### 5.8 Weitere Records und Enums

```java
public enum CatalogEntryType {
    DATASET("Datensatz"),
    DATASET_SERIES("Datenreihe");

    public String label();
}

public enum DistributionFormat {
    CSV("CSV"), XLSX("XLSX"), PARQUET("Parquet"), OTHER("Weitere");

    public static DistributionFormat fromModelValue(String value);
    public String label();
    public int displayOrder();
}

public record Office(
    URI officeUri,
    String name,
    String abbreviation,
    URI email,
    URI officeAtWeb
) {
    public String displayName();
    public Optional<String> mailAddress();
}

public record ContactPoint(
    String name,
    Optional<String> organizationUnit,
    URI email,
    Optional<String> phone,
    Optional<URI> url
) {
    public Optional<String> mailAddress();
}

public record ThemeAssignment(
    String localThemeCode,
    URI themeUri
) {
    public String displayLabel();
}

public record AccessRights(
    AccessLevel localAccessLevel,
    URI accessRightsUri
) {
    public boolean isOpen();
    public String displayLabel();
}

public record AccrualPeriodicity(
    MaintenanceFrequency localFrequency,
    URI frequencyUri
) {
    public String displayLabel();
}

public record TemporalCoverage(
    Optional<LocalDate> startDate,
    Optional<LocalDate> endDate,
    Optional<LocalDate> referenceDate
) {
    public String displayText(DateFormatters formatters);
    public boolean isPointInTime();
}
```

---

## 6. Katalogquelle und Laden

### 6.1 `CatalogSource`

```java
package ch.so.agi.datenportal.catalog.source;

public interface CatalogSource {
    CatalogBytes load() throws CatalogSourceException;

    String description();
}
```

### 6.2 `CatalogBytes`

```java
public record CatalogBytes(
    byte[] bytes,
    String sourceDescription,
    String contentHash,
    Instant fetchedAt
) {
    public InputStream inputStream();
    public int sizeInBytes();
}
```

`contentHash` ist SHA-256 über die Bytes, hex-kodiert.

### 6.3 `ClasspathCatalogSource`

```java
public final class ClasspathCatalogSource implements CatalogSource {
    public ClasspathCatalogSource(ResourceLoader resourceLoader, String classpathLocation);

    @Override
    public CatalogBytes load();

    @Override
    public String description();
}
```

Regeln:

- `classpathLocation` darf mit oder ohne `classpath:` angegeben werden.
- Nicht vorhandene Resource führt zu `CatalogSourceException` mit verständlicher Meldung.

### 6.4 `FileCatalogSource`

```java
public final class FileCatalogSource implements CatalogSource {
    public FileCatalogSource(Path fileLocation, Clock clock);

    @Override
    public CatalogBytes load();

    @Override
    public String description();
}
```

Regeln:

- File muss existieren, regulär und lesbar sein.
- Symbolic Links sind erlaubt, aber Pfad wird in Logs normalisiert.

### 6.5 `HttpCatalogSource`

```java
public final class HttpCatalogSource implements CatalogSource {
    public HttpCatalogSource(
        HttpClient httpClient,
        URI uri,
        Duration connectTimeout,
        Duration readTimeout,
        Clock clock
    );

    @Override
    public CatalogBytes load();

    @Override
    public String description();
}
```

Regeln:

- HTTP GET.
- `2xx` ist erfolgreich.
- Andere Statuscodes führen zu `CatalogSourceException`.
- Maximalgrösse optional vorbereiten, z.B. `max-size` später.
- `Content-Type` wird geloggt, aber im MVP nicht hart validiert.

### 6.6 `CatalogService`

```java
@Service
public final class CatalogService {
    public CatalogService(CatalogSnapshot initialSnapshot);

    public CatalogSnapshot currentSnapshot();

    public void replaceSnapshot(CatalogSnapshot newSnapshot);

    public Optional<CatalogEntry> findVisibleEntry(String identifier);

    public List<CatalogEntry> visibleEntries();

    public CatalogLoadReport currentLoadReport();
}
```

Implementation:

- Intern `AtomicReference<CatalogSnapshot>`.
- Bei `replaceSnapshot()` alten Snapshot erst nach erfolgreichem Set schliessen.
- `currentSnapshot()` ist lock-free.

### 6.7 `CatalogReloader`

```java
@Service
public final class CatalogReloader {
    public CatalogReloader(
        CatalogSource catalogSource,
        PublishedCatalogParser parser,
        CatalogValidator validator,
        SearchIndexBuilder searchIndexBuilder,
        CatalogService catalogService,
        CatalogProperties properties,
        Clock clock
    );

    public CatalogLoadReport reload();

    CatalogSnapshot buildSnapshot(CatalogBytes bytes);
}
```

Reload-Ablauf:

1. `CatalogSource.load()`.
2. `PublishedCatalogParser.parse(...)`.
3. `CatalogValidator.validate(...)`.
4. Sichtbarkeit anwenden (`published`, `open`).
5. Facets berechnen.
6. Lucene-Index bauen.
7. `CatalogSnapshot` erstellen.
8. Atomar in `CatalogService.replaceSnapshot(...)` publizieren.
9. `CatalogLoadReport` zurückgeben.

Fehlerfall:

- Keine teilweise Veröffentlichung.
- Alter Snapshot bleibt aktiv.
- Exception enthält technische Ursache, Admin-Antwort enthält kontrollierte Fehlermeldung.

### 6.8 `StartupCatalogLoader`

```java
@Component
public final class StartupCatalogLoader implements ApplicationRunner {
    public StartupCatalogLoader(CatalogReloader reloader);

    @Override
    public void run(ApplicationArguments args);
}
```

Regel: Wenn initiales Laden fehlschlägt, soll die Anwendung nicht still mit leerem Katalog starten. Für lokale Entwicklung darf ein Profil `dev-empty-catalog` später eingeführt werden, aber nicht als Default.

---

## 7. XTF-/XML-Parser

### 7.1 Ziel

Der Parser ist **kein generischer INTERLIS-Validator**. Er liest genau die benötigte Struktur des PublishedCatalog-XTF. Er ist namespace-aware, aber nicht prefix-abhängig. Das heisst: `base:ContactPoint` und `ContactPoint` werden über `localName` und Kontext erkannt.

### 7.2 `PublishedCatalogParser`

```java
public interface PublishedCatalogParser {
    Catalog parse(InputStream inputStream, XtfParserContext context) throws XtfParseException;
}
```

### 7.3 `XtfParserContext`

```java
public record XtfParserContext(
    String sourceDescription,
    Clock clock,
    boolean collectWarnings
) {
    public static XtfParserContext forSource(String sourceDescription, Clock clock);
}
```

### 7.4 `XtfPublishedCatalogParser`

```java
public final class XtfPublishedCatalogParser implements PublishedCatalogParser {
    public XtfPublishedCatalogParser();

    @Override
    public Catalog parse(InputStream inputStream, XtfParserContext context);

    private XMLStreamReader newReader(InputStream inputStream);

    private Catalog parseTransfer(XMLStreamReader reader, XtfParserContext context);

    private Catalog parseCatalog(XMLStreamReader reader, XtfElementPath path);

    private Office parseOfficeElement(XMLStreamReader reader, XtfElementPath path);

    private DatasetEntry parseDatasetWrapper(XMLStreamReader reader, XtfElementPath path);

    private DatasetEntry parseDataset(XMLStreamReader reader, XtfElementPath path);

    private DatasetSeriesEntry parseDatasetSeriesWrapper(XMLStreamReader reader, XtfElementPath path);

    private DatasetSeriesEntry parseDatasetSeries(XMLStreamReader reader, XtfElementPath path);

    private DatasetIssueEntry parseDatasetIssueWrapper(XMLStreamReader reader, XtfElementPath path);

    private DatasetIssueEntry parseDatasetIssue(XMLStreamReader reader, XtfElementPath path);

    private DistributionLink parseDistributionWrapper(XMLStreamReader reader, XtfElementPath path);

    private DistributionLink parseDistribution(XMLStreamReader reader, XtfElementPath path);

    private ThemeAssignment parseThemeAssignmentWrapper(XMLStreamReader reader, XtfElementPath path);

    private ThemeAssignment parseThemeAssignment(XMLStreamReader reader, XtfElementPath path);

    private AccessRights parseAccessRightsWrapper(XMLStreamReader reader, XtfElementPath path);

    private AccessRights parseAccessRights(XMLStreamReader reader, XtfElementPath path);

    private AccrualPeriodicity parseAccrualPeriodicityWrapper(XMLStreamReader reader, XtfElementPath path);

    private AccrualPeriodicity parseAccrualPeriodicity(XMLStreamReader reader, XtfElementPath path);

    private ContactPoint parseContactPointWrapper(XMLStreamReader reader, XtfElementPath path);

    private ContactPoint parseContactPoint(XMLStreamReader reader, XtfElementPath path);

    private TemporalCoverage parseTemporalCoverageWrapper(XMLStreamReader reader, XtfElementPath path);

    private TemporalCoverage parseTemporalCoverage(XMLStreamReader reader, XtfElementPath path);

    private String readRequiredText(XMLStreamReader reader, XtfElementPath path, String fieldName);

    private Optional<String> readOptionalText(XMLStreamReader reader, XtfElementPath path, String fieldName);

    private URI parseRequiredUri(String value, XtfElementPath path, String fieldName);

    private Optional<URI> parseOptionalUri(String value, XtfElementPath path, String fieldName);

    private LocalDate parseRequiredDate(String value, XtfElementPath path, String fieldName);

    private Optional<LocalDate> parseOptionalDate(String value, XtfElementPath path, String fieldName);

    private boolean isStart(XMLStreamReader reader, String localName);

    private boolean isEnd(XMLStreamReader reader, String localName);

    private void skipElement(XMLStreamReader reader);
}
```

### 7.5 XML-Sicherheit

```java
public final class XmlSecurity {
    private XmlSecurity();

    public static XMLInputFactory secureXmlInputFactory();
}
```

Pflicht:

- DTD deaktivieren.
- External Entities deaktivieren.
- Keine XInclude-/Entity-Auflösung.
- XMLStreamException in `XtfParseException` mit Source und Elementpfad verpacken.

### 7.6 Parser-Kontext und Pfade

```java
public record XtfElementPath(List<String> elements) {
    public XtfElementPath push(String localName);
    public XtfElementPath pop();
    public String display();
}
```

Beispiel einer Fehlermeldung:

```text
Fehler in catalog-with-series.xtf bei /Publication/Catalog/datasetSeries/DatasetSeries/issues/DatasetIssue/identifier: Pflichtfeld fehlt.
```

### 7.7 Builder-Ansatz

Beim StAX-Parsen sind temporäre mutable Builder erlaubt. Nach Abschluss wird in immutable Domain-Records konvertiert.

```java
final class MutableDataResourceBuilder {
    URI resourceUri;
    String identifier;
    String title;
    String description;
    Office publisher;
    Office creator;
    ContactPoint contactPoint;
    List<ThemeAssignment> themes = new ArrayList<>();
    List<String> keywords = new ArrayList<>();
    URI landingPage;
    LocalDate issued;
    LocalDate modified;
    URI licenseUri;
    AccessRights accessRights;
    PublicationStatus publicationStatus;
    Origin origin;
    AccrualPeriodicity accrualPeriodicity;
    TemporalCoverage temporalCoverage;

    DatasetEntry toDataset(List<DistributionLink> distributions);
    DatasetIssueEntry toIssue(List<DistributionLink> distributions, String issueLabel, boolean currentIssue);
    DatasetSeriesEntry toSeries(List<DatasetIssueEntry> issues, Optional<String> currentIssueIdentifier);
}
```

### 7.8 XTF-Struktur, die unterstützt werden muss

Der Parser muss mindestens diese wiederkehrende Struktur lesen:

```xml
<Catalog>
  <datasets>
    <Dataset>...</Dataset>
  </datasets>
  <datasetSeries>
    <DatasetSeries>
      <issues>
        <DatasetIssue>...</DatasetIssue>
      </issues>
    </DatasetSeries>
  </datasetSeries>
</Catalog>
```

Und verschachtelte Strukturen:

```xml
<publisher><Office>...</Office></publisher>
<creator><Office>...</Office></creator>
<contactPoint><base:ContactPoint>...</base:ContactPoint></contactPoint>
<themes><ThemeAssignment>...</ThemeAssignment></themes>
<accessRights><AccessRights>...</AccessRights></accessRights>
<accrualPeriodicity><AccrualPeriodicity>...</AccrualPeriodicity></accrualPeriodicity>
<temporalCoverage><base:TemporalCoverage>...</base:TemporalCoverage></temporalCoverage>
<distributions><Distribution>...</Distribution></distributions>
```

### 7.9 Parser-Fehlertoleranz

- Unbekannte Elemente innerhalb bekannter Strukturen: `skipElement`, Warnung auf Debug-Level.
- Fehlende Pflichtfelder: `XtfParseException`.
- Ungültige URI: `XtfParseException`.
- Ungültiges Datum: `XtfParseException`.
- Ungültiger Enum-Wert: `XtfParseException`.
- Leere Strings für Pflichtfelder gelten als fehlend.

---

## 8. Validierung und Sichtbarkeit

### 8.1 `CatalogValidator`

```java
@Component
public final class CatalogValidator {
    public ValidationResult validate(Catalog catalog);

    private void validateCatalog(Catalog catalog, ValidationResultBuilder result);

    private void validateDataset(DatasetEntry dataset, ValidationResultBuilder result);

    private void validateSeries(DatasetSeriesEntry series, ValidationResultBuilder result);

    private void validateIssue(DatasetSeriesEntry series, DatasetIssueEntry issue, ValidationResultBuilder result);

    private void validateDistributions(String ownerIdentifier, List<DistributionLink> distributions, ValidationResultBuilder result);

    private void validateIdentifiers(Catalog catalog, ValidationResultBuilder result);
}
```

### 8.2 `ValidationResult`

```java
public record ValidationResult(
    List<CatalogWarning> warnings,
    List<String> errors
) {
    public boolean hasErrors();
    public boolean hasWarnings();
    public void throwIfInvalid();
}
```

### 8.3 Pflichtregeln

- Katalog enthält mindestens einen normalen Datensatz oder eine Serie.
- Identifier sind global eindeutig über Datasets, Series und Issues.
- Normaler Datensatz hat mindestens eine Distribution.
- Issue hat mindestens eine Distribution.
- Serie hat mindestens ein Issue.
- Distribution hat `accessUrl` und `format`.
- Nur `csv`, `xlsx`, `parquet`, `other` sind erlaubte Formate.
- Für Listing-Downloadbuttons werden nur `csv`, `xlsx`, `parquet` berücksichtigt.

### 8.4 Sichtbarkeitsfilter

```java
public final class VisibilityPolicy {
    public VisibilityPolicy(CatalogProperties properties);

    public boolean isVisible(CatalogEntry entry);

    public boolean isVisibleIssue(DatasetIssueEntry issue);
}
```

MVP-Regel:

- `publicationStatus == PUBLISHED`
- `accessRights.localAccessLevel == OPEN`, falls `showOnlyOpenData=true`

Bei Serien:

- Serie sichtbar, wenn Serie selbst sichtbar ist und mindestens ein sichtbares Issue existiert.
- `currentIssue()` wird aus sichtbaren Issues bestimmt.

---

## 9. Suche, Filter, Sortierung und Facetten

### 9.1 Query-Objekte

```java
public record SearchQuery(
    String q,
    SearchFilters filters,
    SortMode sortMode,
    PageRequest pageRequest
) {
    public boolean hasTextQuery();
    public SearchQuery normalized(SearchProperties properties);
}

public record SearchFilters(
    Optional<String> themeCode,
    Optional<String> officeIdentifierOrName,
    ModifiedRange modifiedRange,
    Optional<CatalogEntryType> entryType
) {
    public boolean isEmpty();
}

public enum ModifiedRange {
    ALL,
    LAST_30_DAYS,
    LAST_365_DAYS;

    public boolean matches(LocalDate date, Clock clock);
}

public enum SortMode {
    MODIFIED_DESC,
    TITLE_ASC;

    public static SortMode defaultMode();
}

public record PageRequest(int page, int size) {
    public int offset();
    public PageRequest normalize(SearchProperties properties);
}
```

### 9.2 `SearchService`

```java
public interface SearchService {
    SearchResultPage search(CatalogSnapshot snapshot, SearchQuery query);
}
```

### 9.3 `LuceneSearchService`

```java
@Service
public final class LuceneSearchService implements SearchService {
    public LuceneSearchService(SearchProperties properties, Clock clock);

    @Override
    public SearchResultPage search(CatalogSnapshot snapshot, SearchQuery query);

    private List<CatalogEntry> allEntries(CatalogSnapshot snapshot, SearchQuery query);

    private List<CatalogEntry> luceneEntries(CatalogSnapshot snapshot, SearchQuery query);

    private List<CatalogEntry> applyFilters(List<CatalogEntry> entries, SearchFilters filters);

    private List<CatalogEntry> applySort(List<CatalogEntry> entries, SortMode sortMode, boolean hasTextQuery);

    private SearchResultPage paginate(List<CatalogEntry> entries, SearchQuery query);
}
```

Regel:

- Bei leerer Query keine Lucene-Suche, sondern Snapshot-Liste + Filter + Sortierung.
- Bei Query zunächst Lucene, danach Filter. Das ist für den MVP akzeptabel und einfacher.

### 9.4 `SearchIndexBuilder`

```java
@Component
public final class SearchIndexBuilder {
    public SearchIndex build(List<CatalogEntry> visibleEntries);

    private Document documentFor(CatalogEntry entry);

    private void addCommonFields(Document doc, CatalogEntry entry);

    private void addSeriesFields(Document doc, DatasetSeriesEntry series);
}
```

Lucene-Felder:

```text
id                 StringField stored
entryType          StringField stored
identifier_exact   StringField stored, boosted beim Query-Building
identifier_text    TextField
name               TextField boosted
name_exact         StringField
description        TextField
keywords           TextField boosted
themes_text        TextField
themes_exact       StringField mehrfach
office_text        TextField
office_exact       StringField
modified_epoch_day LongPoint + StoredField
issue_labels       TextField, nur Serie
issue_titles       TextField, nur Serie
all_text           TextField
```

### 9.5 `LuceneSearchIndex`

```java
public final class LuceneSearchIndex implements SearchIndex {
    public LuceneSearchIndex(Directory directory, IndexSearcher searcher, Map<String, CatalogEntry> entriesByIdentifier);

    @Override
    public List<SearchResultItem> search(String userQuery, int maxResults);

    @Override
    public void close();

    private Query buildQuery(String userQuery);
}
```

Query-Regeln:

- User-Input escapen.
- Identifier-Exact-Matches boosten.
- Titel stärker als Beschreibung.
- Keywords stärker als Fliesstext.
- Keine Exception bei Sonderzeichen wie `+`, `:`, `/`, `(`.

### 9.6 `SearchResultPage`

```java
public record SearchResultPage(
    List<CatalogEntry> entries,
    int totalElements,
    int page,
    int size,
    int totalPages,
    SearchQuery query
) {
    public boolean hasPrevious();
    public boolean hasNext();
    public int previousPage();
    public int nextPage();
}
```

### 9.7 Facetten

```java
public record Facets(
    List<FacetValue> themes,
    List<FacetValue> offices,
    List<FacetValue> entryTypes
) {}

public record FacetValue(
    String value,
    String label,
    int count
) {}

@Service
public final class FacetService {
    public Facets compute(List<CatalogEntry> visibleEntries);
}
```

Facetten werden zunächst aus allen sichtbaren Einträgen berechnet, nicht dynamisch aus der aktuellen Suchmenge. Dynamische Facetten können später ergänzt werden.

---

## 10. Web Layer und Routen

### 10.1 Routen

```text
GET  /
GET  /datasets
GET  /datasets/{identifier}
GET  /datasets/{identifier}/issues/{issueLabel}       # optional später
POST /admin/catalog/reload
GET  /admin/catalog/status
```

### 10.2 Query-Parameter

```text
q=wasser
theme=Raum_und_Umwelt
office=Amt%20für%20Umwelt
modified=all|last30|last365
type=dataset|series
view=cards|list
sort=modified-desc|title-asc
```

Die öffentliche Katalog-UI verwendet im MVP keine sichtbare Pagination. `page` und `size` dürfen im Request-Modell für eine spätere Reaktivierung vorhanden sein, werden aktuell aber ignoriert und nicht kanonisch weitergetragen.

### 10.3 `CatalogQueryParams`

```java
public record CatalogQueryParams(
    Optional<String> q,
    Optional<String> theme,
    Optional<String> office,
    Optional<String> modified,
    Optional<String> type,
    Optional<String> view,
    Optional<String> sort
) {
    public SearchQuery toSearchQuery(SearchProperties searchProperties);

    public ViewMode viewMode();

    public String toQueryString();
}
```

### 10.4 `HtmxRequest`

```java
public final class HtmxRequest {
    private HtmxRequest();

    public static boolean isHtmx(HttpServletRequest request);

    public static boolean isBoosted(HttpServletRequest request);

    public static Optional<String> target(HttpServletRequest request);
}
```

Header:

- `HX-Request: true`
- `HX-Boosted: true`
- `HX-Target`

### 10.5 `HomeController`

```java
@Controller
public final class HomeController {
    public HomeController(
        CatalogService catalogService,
        SearchService searchService,
        HeaderViewModelFactory headerFactory,
        BreadcrumbFactory breadcrumbFactory,
        HomePageVmFactory homePageVmFactory,
        SearchProperties searchProperties
    );

    @GetMapping({"/", "/datasets"})
    public String index(
        @ModelAttribute CatalogQueryParams params,
        HttpServletRequest request,
        Model model
    );

    private String renderFullPage(HomePageVm vm, Model model);

    private String renderResultsFragment(ResultsVm vm, Model model);
}
```

Regeln:

- Normaler Request: `pages/home.jte`.
- HTMX-Request mit Target `dataset-results`: `fragments/results.jte`.
- Query-Parameter bleiben kanonisch in der URL.
- Formulare verwenden `method="get"`.

### 10.6 `DatasetController`

```java
@Controller
public final class DatasetController {
    public DatasetController(
        CatalogService catalogService,
        HeaderViewModelFactory headerFactory,
        BreadcrumbFactory breadcrumbFactory,
        DatasetDetailVmFactory detailVmFactory
    );

    @GetMapping("/datasets/{identifier}")
    public String detail(@PathVariable String identifier, Model model);

    private String renderDatasetDetail(DatasetEntry dataset, Model model);

    private String renderSeriesDetail(DatasetSeriesEntry series, Model model);
}
```

Regeln:

- Identifier wird URL-dekodiert.
- Nicht gefundener Identifier: `404` mit `pages/not-found.jte`.
- Detailseite zeigt keine komplexen Tabs, die JavaScript benötigen. Tabs können als Anker oder einfache serverseitige Abschnitte umgesetzt werden.

### 10.7 `DownloadUrlResolver`

```java
@Component
public final class DownloadUrlResolver {
    public List<DownloadButtonVm> buttonsForListing(CatalogEntry entry);

    public List<DownloadButtonVm> buttonsForDataset(DatasetEntry dataset);

    public List<DownloadButtonVm> buttonsForIssue(DatasetIssueEntry issue);

    public Optional<DownloadButtonVm> buttonFor(CatalogEntry entry, DistributionFormat format);
}
```

Regeln:

- Reihenfolge: CSV, XLSX, Parquet, danach Other nur Detailseite.
- `href` = `DistributionLink.preferredHref()`.
- Externe Links erhalten `rel="noopener noreferrer"`, falls `target="_blank"` verwendet wird.

---

## 11. View Models

View Models sind bewusst klein, UI-orientiert und frei von Domainlogik. Sie werden in Factories aus Domain-Objekten erzeugt.

### 11.1 `PageChromeVm`

```java
public record PageChromeVm(
    String pageTitle,
    HeaderVm header,
    BreadcrumbVm breadcrumb,
    WebAssetsVm assets
) {}
```

### 11.2 `HeaderVm`

```java
public record HeaderVm(
    String siteName,
    String logoHref,
    String activeSection,
    List<NavItemVm> topNav,
    List<NavItemVm> sectionNav
) {
    public String topNavJson();
    public String sectionNavJson();
}
```

`topNavJson()` und `sectionNavJson()` müssen JSON für HTML-Attribute sicher escapen. Keine manuelle String-Konkatenation im Template.

### 11.3 `BreadcrumbVm`

```java
public record BreadcrumbVm(List<BreadcrumbItemVm> items) {
    public boolean isEmpty();
}

public record BreadcrumbItemVm(
    String label,
    Optional<String> href,
    boolean currentPage
) {}
```

### 11.4 `HomePageVm`

```java
public record HomePageVm(
    PageChromeVm chrome,
    String heroTitle,
    String heroLead,
    CatalogQueryParams queryParams,
    FilterPanelVm filterPanel,
    ResultsVm results,
    ViewMode viewMode
) {}
```

### 11.5 `ResultsVm`

```java
public record ResultsVm(
    List<EntryCardVm> cards,
    List<EntryRowVm> rows,
    int totalElements,
    ViewMode viewMode,
    CatalogQueryParams queryParams
) {
    public boolean isCards();
    public boolean isList();
    public boolean isEmpty();
}
```

### 11.6 `EntryCardVm`

```java
public record EntryCardVm(
    String typeLabel,
    List<StatusBadgeVm> badges,
    String title,
    String description,
    String creatorLabel,
    String modifiedLabel,
    List<String> themeLabels,
    List<DownloadButtonVm> downloads,
    String detailHref,
    Optional<String> currentIssueLabel
) {}
```

### 11.7 `EntryRowVm`

```java
public record EntryRowVm(
    String iconName,
    String title,
    String description,
    String typeLabel,
    boolean series,
    String publicationDateLabel,
    String creatorLabel,
    String metadataHref,
    List<DownloadButtonVm> downloads,
    Optional<String> currentIssueActionHref,
    Optional<String> allIssuesHref
) {}
```

### 11.8 Detail-View-Models

```java
public record DatasetDetailPageVm(
    PageChromeVm chrome,
    String identifier,
    String title,
    String description,
    List<StatusBadgeVm> badges,
    List<DownloadButtonVm> downloads,
    List<MetadataGroupVm> metadataGroups,
    List<MetadataRowVm> technicalAccessRows
) {}

public record SeriesDetailPageVm(
    PageChromeVm chrome,
    String identifier,
    String title,
    String description,
    List<StatusBadgeVm> badges,
    List<DownloadButtonVm> currentIssueDownloads,
    String currentIssueLabel,
    List<IssueRowVm> issues,
    List<MetadataGroupVm> metadataGroups
) {}

public record IssueRowVm(
    String issueLabel,
    boolean current,
    String modifiedLabel,
    String title,
    List<DownloadButtonVm> downloads,
    String detailHref
) {}
```

---

## 12. Integration der Web Components

### 12.1 Ausgangslage

`so-web-components` stellt Custom Elements mit Shadow DOM bereit. Relevant für diese Anwendung sind:

- `<so-header>`
- `<so-breadcrumb>`
- `<so-breadcrumb-item>`
- optional `<so-lead-text>`

Die Komponenten werden über Attribute konfiguriert. `top-nav` und `section-nav` sind JSON-Arrays. Das Styling nutzt CSS Custom Properties wie `--so-bg`, `--so-fg`, `--so-container-padding`.

### 12.2 Asset-Strategie

MVP-Entscheid: **vendored static assets** statt npm-Build in der Spring-Boot-App.

Zielstruktur:

```text
src/main/resources/static/vendor/so-web-components/0.1.10/
  index.js
  styles/
    reset.css
    tokens.css
    fonts.css
    FrutigerLTW05-55Roman.woff2
    FrutigerLTW05-75Black.woff2
```

Wichtig:

- `so-web-components@0.1.10` liefert die benoetigten `woff2`-Dateien direkt im `styles/`-Verzeichnis mit.
- Die Fontdateien werden nicht aus dem Internet geladen, sondern zusammen mit den Web-Component-Assets lokal ausgeliefert.
- `fonts.css` muss relative URLs auf diese Dateien verwenden und darf keine absoluten lokalen Pfade wie `/Users/...` enthalten.
- Es gibt keine separate globale Font-Ablage unter `src/main/resources/static/assets/fonts/`.
- Für lokale Entwicklung darf testweise CDN für Web Components verwendet werden; produktionsnah nicht. Fonts werden auch lokal nie über externe CDN geladen.

### 12.2.1 Schriftintegration

Die Webanwendung soll die im Datenportal-Designsystem vorgesehene Schrift konsistent in App und Web Components verwenden. Fuer `so-web-components@0.1.10` ist die mitgelieferte `fonts.css` mit ihren `woff2`-Dateien die Source of Truth.

Empfohlene Zielstruktur:

```text
src/main/resources/static/css/
  app.css

src/main/resources/static/vendor/so-web-components/<version>/styles/
  reset.css
  tokens.css
  fonts.css
  FrutigerLTW05-55Roman.woff2
  FrutigerLTW05-75Black.woff2
```

Beispielprinzip für `fonts.css`:

```css
@font-face {
  font-family: Frutiger;
  src: url('./FrutigerLTW05-55Roman.woff2') format('woff2');
  font-weight: 400;
  font-style: normal;
}
```

Akzeptanzkriterien:

- Die Schrift wird auf Startseite, Übersicht, Detailseite und in Fallback-Komponenten einheitlich verwendet.
- Header- und Breadcrumb-Web-Components übernehmen die Designsystem-Tokens oder fügen sich optisch in die Schrift-/Token-Definition ein.
- Keine Fontdatei wird von einer externen URL geladen.
- Keine absoluten lokalen Pfade werden committet.
- Tests oder Smoke Checks prüfen mindestens, dass `/vendor/so-web-components/<version>/styles/fonts.css` und die referenzierten vendor-lokalen Font-URLs ausgeliefert werden.

### 12.3 `WebAssetsVm`

```java
public record WebAssetsVm(
    boolean webComponentsEnabled,
    String webComponentsIndexJs,
    String resetCss,
    String tokensCss,
    Optional<String> fontsCss,
    String appCss,
    String htmxJs
) {}
```

Factory:

```java
@Component
public final class WebAssetsVmFactory {
    public WebAssetsVmFactory(WebComponentsProperties properties);

    public WebAssetsVm create();
}
```

### 12.4 Layout-Template

`src/main/jte/layout/page.jte` soll die Assets zentral laden.

Konzept:

```html
<!doctype html>
<html lang="de">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${page.chrome().pageTitle()}</title>

  <link rel="stylesheet" href="${page.chrome().assets().resetCss()}">
  <link rel="stylesheet" href="${page.chrome().assets().tokensCss()}">
  @if(page.chrome().assets().fontsCss().isPresent())
    <link rel="stylesheet" href="${page.chrome().assets().fontsCss().get()}">
  @endif
  <link rel="stylesheet" href="${page.chrome().assets().appCss()}">

  @if(page.chrome().assets().webComponentsEnabled())
    <script type="module" src="${page.chrome().assets().webComponentsIndexJs()}"></script>
  @endif
  <script src="${page.chrome().assets().htmxJs()}" defer></script>
</head>
<body>
  @template.components.soHeader(page.chrome().header())
  @template.components.soBreadcrumb(page.chrome().breadcrumb())

  <main id="main-content" class="dp-main">
    @content
  </main>

  @template.components.footer()
</body>
</html>
```

Die konkrete JTE-Syntax kann je nach Projektsetup leicht angepasst werden. Wichtig ist die fachliche Struktur: Header und Breadcrumb sind zentrale Layout-Bestandteile, nicht pro Seite handkopiert.

### 12.5 Header-Component

`src/main/jte/components/soHeader.jte`:

```html
@param ch.so.agi.datenportal.web.view.HeaderVm header

<so-header
  top-nav='@raw(header.topNavJson())'
  section-nav='@raw(header.sectionNavJson())'
  active-section="${header.activeSection()}"
  logo-href="${header.logoHref()}"
  site-name="${header.siteName()}">
</so-header>
```

`HeaderViewModelFactory`:

```java
@Component
public final class HeaderViewModelFactory {
    private final JsonAttributeEncoder json;

    public HeaderViewModelFactory(JsonAttributeEncoder json);

    public HeaderVm create(String activeSection);

    private List<NavItemVm> topNav();

    private List<NavItemVm> sectionNav();
}
```

Default-Navigation für das Datenportal:

```java
sectionNav = [
  new NavItemVm("Daten", "/datasets"),
  new NavItemVm("Themen", "/themes"),
  new NavItemVm("Statistiken", "/statistics"),
  new NavItemVm("Karten", "/maps"),
  new NavItemVm("APIs", "/apis"),
  new NavItemVm("Über uns", "/about")
]

topNav = [
  new NavItemVm("Services", "https://so.ch/services/"),
  new NavItemVm("Verwaltung", "https://so.ch/verwaltung/"),
  new NavItemVm("my.so.ch", "https://my.so.ch/")
]
```

Anmerkung: Das tatsächliche `so-header`-Web-Component-Layout unterscheidet zwischen `top-nav` und `section-nav`. Für das Datenportal kann die Portalnavigation in `section-nav` liegen, die kantonalen Utility-Links in `top-nav`.

### 12.6 Breadcrumb-Component

`src/main/jte/components/soBreadcrumb.jte`:

```html
@param ch.so.agi.datenportal.web.view.BreadcrumbVm breadcrumb

@if(!breadcrumb.isEmpty())
  <so-breadcrumb>
    @for(var item : breadcrumb.items())
      @if(item.currentPage())
        <so-breadcrumb-item iscurrentpage>${item.label()}</so-breadcrumb-item>
      @elseif(item.href().isPresent())
        <so-breadcrumb-item href="${item.href().get()}">${item.label()}</so-breadcrumb-item>
      @else
        <so-breadcrumb-item>${item.label()}</so-breadcrumb-item>
      @endif
    @endfor
  </so-breadcrumb>
@endif
```

Regel: Für das aktuelle Element wird `iscurrentpage` lowercase verwendet. Das ist robust in HTML und wird von der Web Component erkannt.

### 12.7 `BreadcrumbFactory`

```java
@Component
public final class BreadcrumbFactory {
    public BreadcrumbVm home();

    public BreadcrumbVm catalog();

    public BreadcrumbVm dataset(CatalogEntry entry);

    public BreadcrumbVm series(DatasetSeriesEntry series);

    public BreadcrumbVm issue(DatasetSeriesEntry series, DatasetIssueEntry issue);
}
```

Beispiele:

```text
so.ch > Datenportal > Daten & Statistiken
so.ch > Datenportal > Daten & Statistiken > Bevölkerung nach Alter und Geschlecht
so.ch > Datenportal > Daten & Statistiken > Gemeindegrenzen
```

### 12.8 Fallback, falls Web Components nicht geladen werden

Wenn `datenportal.web-components.enabled=false`, sollen JTE-Fallbacks gerendert werden:

- `components/headerFallback.jte`
- `components/breadcrumbFallback.jte`

Die Seiten müssen weiterhin bedienbar sein. Der Fallback muss nicht pixelgenau sein, aber semantisch korrekt:

```html
<header role="banner">...</header>
<nav aria-label="Breadcrumb">...</nav>
```

### 12.9 Tests für Web Components Integration

```java
@WebMvcTest(HomeController.class)
class WebComponentIntegrationMvcTest {
    @Test
    void homePageContainsSoHeaderWhenWebComponentsEnabled();

    @Test
    void detailPageContainsBreadcrumbItems();

    @Test
    void breadcrumbMarksLastItemAsCurrentPage();

    @Test
    void fallbackHeaderIsRenderedWhenWebComponentsDisabled();
}
```

HTML-Asserts:

- `<so-header`
- `section-nav=`
- `<so-breadcrumb>`
- `<so-breadcrumb-item href="/datasets"`
- `iscurrentpage`

---

## 13. JTE-Template-Struktur

```text
src/main/jte/
  layout/
    page.jte
  pages/
    home.jte
    detail-dataset.jte
    detail-series.jte
    not-found.jte
    error.jte
  fragments/
    results.jte
    result-count.jte
  components/
    soHeader.jte
    soBreadcrumb.jte
    headerFallback.jte
    breadcrumbFallback.jte
    footer.jte
    hero.jte
    searchForm.jte
    filterPanel.jte
    viewToggle.jte
    sortSelect.jte
    datasetCard.jte
    datasetRow.jte
    downloadButtons.jte
    statusBadges.jte
    metadataGroups.jte
    metadataRow.jte
    issueTable.jte
    emptyState.jte
```

### 13.1 `pages/home.jte`

Parameter:

```java
@param ch.so.agi.datenportal.web.view.HomePageVm page
```

Inhalt:

- Hero mit Suchfeld.
- Filterleiste/Filterpanel.
- Resultatbereich mit `id="dataset-results"`.
- Formulare mit `hx-get="/datasets"`, `hx-target="#dataset-results"`, `hx-push-url="true"`.

### 13.2 `fragments/results.jte`

Parameter:

```java
@param ch.so.agi.datenportal.web.view.ResultsVm results
```

Regeln:

- Rendert **nur** Ergebniscontrols und Cards/List.
- Kein `<html>`, kein `<body>`, kein Header.
- Muss auch ohne umgebende Seite semantisch verständlich bleiben.

### 13.3 Cards und Liste

`datasetCard.jte`:

- Badge `Datensatz` oder `Datenreihe`.
- Statusbadges.
- Titel als Link zur Detailseite.
- Kurzbeschreibung.
- Themenchips.
- Amt/Fachstelle.
- Datum.
- Downloadbuttons.

`datasetRow.jte`:

- Tabellenartige Zeile.
- Bei Serien zusätzlich `Aktuelle Ausgabe` und `Alle Ausgaben`.
- Downloadbuttons bleiben klein.

---

## 14. UI-Implementation-Contract und UI-Verhalten

Dieser Abschnitt ist verbindlich und ersetzt ältere UI-Aussagen aus v2/v3 sowie ältere generierte Mockups, soweit sie widersprechen. Die vollständige, stärker ausführbare UI-Spezifikation liegt zusätzlich in `docs/ui-implementation-contract.md`. Coding-Agenten müssen vor jeder Änderung an JTE, CSS, HTMX, Header/Breadcrumb, Filter, Startseite, Listenansicht, Kartenansicht oder Detailseite den Skill `.agents/skills/datenportal-ui-contract/SKILL.md` anwenden.

### 14.1 Verbindliche UI-Referenzen

Die aktuellen Referenzen liegen im Repository unter:

```text
spec/mockups/current/startseite_liste.png
spec/mockups/current/cards.png
spec/mockups/current/web-components.png
```

Verbindlich ist die Kombination aus Textspezifikation und Referenzbildern:

1. `docs/ui-implementation-contract.md` ist die fachliche und technische Quelle für das UI.
2. `startseite_liste.png` ist die verbindliche visuelle Ausgangslage für die Startseite in Listenansicht.
3. `cards.png` ist die visuelle Referenz für die Kartenansicht.
4. `web-components.png` ist die visuelle Referenz für Header und Breadcrumb aus `so-web-components`.
5. Ältere Mockups sind nur historische Inspiration und dürfen die aktuellen Vorgaben nicht überschreiben.

Wenn PNG und Text voneinander abweichen, gilt der Text. Wenn Text und Datenmodell kollidieren, gilt das Datenmodell; der Agent muss die Abweichung dokumentieren.

### 14.2 Page Chrome: Header und Breadcrumb über Web Components

Header und Breadcrumb werden nicht als eigene JTE-HTML-Nachbauten implementiert. Sie werden über die Web Components aus `so-web-components` integriert. Die Anwendung stellt nur Konfiguration, Assets, Slot-Inhalte und Fallback-Markup bereit.

Verantwortliche Klassen/Records:

```java
package ch.so.agi.datenportal.web.chrome;

public record PageChromeVm(
    String pageTitle,
    HeaderVm header,
    BreadcrumbVm breadcrumb,
    WebAssetsVm webAssets,
    List<String> bodyClasses
) {}

public record HeaderVm(
    String brandLabel,
    String activeArea,
    List<HeaderNavItemVm> mainItems,
    List<HeaderNavItemVm> utilityItems,
    boolean searchEnabled
) {}

public record BreadcrumbVm(List<BreadcrumbItemVm> items) {}

public record BreadcrumbItemVm(String label, String href, boolean current) {}
```

Factories:

```java
@Component
public final class HeaderViewModelFactory {
    public HeaderVm create(String activeSection);
}

@Component
public final class BreadcrumbFactory {
    public BreadcrumbVm catalog();
    public BreadcrumbVm detail(CatalogEntry entry);
    public BreadcrumbVm seriesIssue(DatasetSeriesEntry series, DatasetIssueEntry issue);
}
```

JTE-Komponenten:

```text
src/main/jte/layouts/main.jte
src/main/jte/components/chrome/header.jte
src/main/jte/components/chrome/breadcrumb.jte
```

`header.jte` und `breadcrumb.jte` binden die Web Components ein. Ein semantischer Fallback ist erlaubt, aber nur als Backup für fehlendes JavaScript oder Ladefehler der Web Components. Der Fallback darf optisch einfacher sein, muss aber Navigation und Breadcrumb-Inhalt zugänglich machen.

### 14.3 Startseite/Katalogseite: Grundprinzip

Die Startseite ist im MVP keine Marketing-Landingpage, sondern die zentrale Katalogseite.

Route:

```text
GET /
GET /daten
```

Beide können auf dieselbe Controller-Methode führen oder `/` nach `/daten` weiterleiten. Die Default-Ansicht ist die Listenansicht gemäss `startseite_liste.png`.

Pflichtbereiche oberhalb der Resultate:

- Web-Component-Header
- Web-Component-Breadcrumb
- Seitentitel `Daten & Statistiken`
- Leadtext: `Finden und nutzen Sie offene Daten, Geodaten und Statistiken des Kantons Solothurn.`
- ergänzender Hinweis: `Alle Datensätze sind – sofern verfügbar – als Open Data mit freien Lizenzen nutzbar.`
- Suchfeld
- Filterleiste
- Ergebnisanzahl
- Ansicht-Umschaltung `Kartenansicht` / `Listenansicht`
- Sortierung

Controller-Signatur:

```java
@Controller
@RequestMapping({"/", "/daten"})
public final class CatalogController {
    @GetMapping
    public String index(@Valid CatalogQueryParams params, HtmxRequest htmx, Model model);
}
```

### 14.4 Mehrfachfilter und aktive Filteranzeige

Filter müssen Mehrfachauswahl unterstützen. Betroffen sind mindestens:

- Thema
- Fachstelle / Amt
- Publikationsdatum / Zeitraum

URL-Konvention:

```text
?theme=bevoelkerung&theme=umwelt&office=agi&office=afu&format=CSV&format=PARQUET
```

Die Query-Parameter werden bewusst wiederholt statt kommasepariert. Das ist HTML-Form-kompatibel und robust für HTMX und normale GET-Requests.

ViewModel:

```java
public record FilterGroupVm(
    String id,
    String label,
    List<FilterOptionVm> options,
    List<ActiveFilterChipVm> activeChips,
    boolean expanded
) {}

public record FilterOptionVm(
    String value,
    String label,
    long count,
    boolean selected,
    String removeUrl,
    String addUrl
) {}

public record ActiveFilterChipVm(
    String groupLabel,
    String label,
    String removeUrl
) {}
```

Darstellung:

- geschlossene Filter zeigen Label und Zusammenfassung, z.B. `2 ausgewählt`
- geöffnete Filter zeigen Checkboxen mit Counts
- ausgewählte Werte werden zusätzlich als aktive Chips unterhalb oder neben der Filterleiste angezeigt
- jeder Chip hat einen Entfernen-Link
- `Filter zurücksetzen` entfernt alle Filter, aber nicht zwingend `view` und `sort`
- ohne JavaScript funktioniert alles per GET
- mit HTMX wird nur Resultatliste plus Filterzusammenfassung ersetzt

### 14.5 Ansicht-Umschaltung

Query-Parameter:

```text
view=list
view=cards
```

Default:

```text
view=list
```

ViewModel:

```java
public enum CatalogViewMode { LIST, CARDS }

public record ViewToggleVm(
    CatalogViewMode active,
    String listUrl,
    String cardsUrl
) {}
```

Die Umschaltung muss Links verwenden, keine rein clientseitigen Buttons. Der aktive Zustand ist mit `aria-current="page"` oder gleichwertig zugänglich zu markieren.

### 14.6 Listenansicht

Die Listenansicht folgt `startseite_liste.png`, mit den fachlichen Korrekturen aus diesem Abschnitt.

Tabellenspalten:

```text
[Expand] | Thema / Datensatz | Typ | Publikationsdatum | Metadaten | Daten herunterladen
```

Abweichung gegenüber Screenshot: In der Spalte `Thema / Datensatz` werden keine Icons angezeigt. Das Plus-/Minus-Zeichen für Datenreihen steht in einer eigenen schmalen Expand-Spalte ganz links. Normale Datensätze haben dort entweder eine leere Zelle oder einen unsichtbaren Platzhalter für Spaltenausrichtung.

Typ-Badges:

- `Datensatz` und `Datenreihe` verwenden exakt denselben grauen Badge-Stil.
- `Datenreihe` darf nicht rot hervorgehoben werden.
- Der Typ-Badge ist informativ, nicht primäre Aktion.

ViewModel:

```java
public record EntryRowVm(
    String id,
    String title,
    String description,
    String typeLabel,
    boolean expandable,
    boolean expanded,
    String expandUrl,
    String detailUrl,
    LocalDate publicationDate,
    String publicationDateLabel,
    String metadataUrl,
    List<DownloadActionVm> downloadActions,
    List<IssueRowVm> issueRows
) {}

public record DownloadActionVm(
    String label,
    String format,
    String href,
    boolean currentIssue,
    String ariaLabel
) {}

public record IssueRowVm(
    String issueId,
    String title,
    String description,
    String typeLabel,
    LocalDate publicationDate,
    String publicationDateLabel,
    String metadataUrl,
    List<DownloadActionVm> downloadActions,
    String detailUrl
) {}
```

Normale Datensatz-Zeile:

- kein Icon beim Titel
- Titel und Beschreibung links
- Typ-Badge `Datensatz`, grau
- Metadaten-Spalte enthält einen `i`-Link auf die Detailseite
- Download-Spalte enthält direkte Formatlinks, z.B. `CSV`, `XLSX`, `Parquet`, optional `API`

Datenreihen-Root-Zeile:

- Plus-Zeichen in der Expand-Spalte
- Zeile ist per Plus und optional per Row-Klick aufklappbar
- Row-Klick darf nicht auslösen, wenn auf Metadaten-Link oder Download-Link geklickt wird
- Typ-Badge `Datenreihe`, gleicher grauer Stil wie `Datensatz`
- Download-Spalte zeigt die Formate der aktuellen Ausgabe, aber mit kurzer Kennzeichnung, z.B.:
  - `CSV (aktuelle Ausgabe)`
  - `XLSX (aktuelle Ausgabe)`
  - `Parquet (aktuelle Ausgabe)`
- Es gibt keine roten Extra-Badges `Aktuelle Ausgabe` / `Alle Ausgaben` in der Download-Spalte
- Der `i`-Link der Root-Zeile führt auf die Detailseite der aktuellen Ausgabe oder auf eine Detailseite, die die aktuelle Ausgabe prominent zeigt. Im MVP gilt: Root-`i` zeigt auf aktuelle Ausgabe.

Aufgeklappte Ausgaben:

- Jede Ausgabe wird als eigene zusätzliche Tabellenzeile unterhalb der Root-Zeile dargestellt.
- Ausgabezeilen sehen grundsätzlich wie normale Zeilen aus.
- Ausgabezeilen zeigen nicht den Klammertext `(aktuelle Ausgabe)`, ausser die Ausgabe selbst ist als aktuell markiert und dies in einer separaten dezenten Statusangabe nötig ist.
- Ausgabezeilen verlinken ihre eigenen Metadaten-/Detailseiten.
- Ausgabezeilen haben direkte Downloadlinks für ihre konkreten Dateien.

HTMX-Verhalten:

```text
GET /daten/series/{seriesId}/issues-row-fragment
```

liefert optional nur die Ausgabezeilen. Ohne JavaScript/HTMX muss ein normaler Request mit `expanded=<seriesId>` denselben Zustand serverseitig rendern.

### 14.7 Kartenansicht

Die Kartenansicht orientiert sich an `cards.png`.

Card-Pflichtbestandteile:

- grauer Typ-Badge `Datensatz` oder `Datenreihe`, gleicher Stil wie Listenansicht
- Badge `Open Data`, im MVP hardcodiert, weil aktuell nur Open-Data-Einträge angezeigt werden
- zusätzlicher Qualitäts-/Struktur-Badge, wenn Attribute beschrieben sind oder ein Datenmodell vorhanden ist
- Titel
- Beschreibung
- graue Keyword-Badges
- Formatlinks
- Aktualisierungs- oder Publikationsdatum
- roter Pfeil oder dezenter Link zur Detailseite

Der zusätzliche Badge heisst im MVP:

```text
Struktur beschrieben
```

Er wird angezeigt, wenn mindestens eine der folgenden Bedingungen erfüllt ist:

- Datenmodell vorhanden
- Attributbeschreibung vorhanden
- schema-/structure-Metadaten im PublishedCatalog vorhanden

ViewModel:

```java
public record EntryCardVm(
    String id,
    String title,
    String description,
    String typeLabel,
    boolean openData,
    boolean structureDescribed,
    List<String> keywords,
    List<DownloadActionVm> downloadActions,
    String dateLabel,
    String detailUrl
) {}
```

Bei Datenreihen zeigen die Formatlinks auch in der Card die aktuelle Ausgabe. Falls Platz knapp ist, darf die Kennzeichnung als `aktuelle Ausgabe` im `aria-label` und in einem dezenten Hilfstext stehen.

### 14.8 Detailseiten

Die bisherigen Detailseiten-Mockups sind für das MVP nicht verbindlich. Der Agent soll aus den Informationen des PublishedCatalog-Modells eine klare, ruhige Detailseite ableiten.

Nicht im MVP:

- keine Datenvorschau
- keine Diagramme
- keine Preview-Tabelle
- keine clientseitigen Visualisierungen

Normale Datensatz-Detailseite:

- Web-Component-Header und Breadcrumb
- Titel
- Typ-Badge `Datensatz`
- Open-Data-/Lizenzhinweis, sofern vorhanden
- Beschreibung
- Downloadbereich mit `CSV`, `XLSX`, `Parquet`, optional `API`
- strukturierte Metadaten aus dem Datenmodell
- Fachstelle/Amt und Kontakt
- Themen, Keywords
- räumlicher/zeitlicher Bezug, sofern vorhanden
- Publikations-/Aktualisierungsdatum
- Identifier
- Lizenz
- technische Ressourcen/Links

Datenreihen-/Ausgabe-Detailseite:

- Datenreihe und aktuelle/ausgewählte Ausgabe müssen verständlich unterschieden werden
- Detailseite der aktuellen Ausgabe ist über Root-`i` erreichbar
- andere Ausgaben werden verlinkt
- jede Ausgabe hat eigene Downloadlinks und Metadaten, soweit vorhanden
- eine kompakte Liste `Weitere Ausgaben` oder `Historische Ausgaben` ist Pflicht, wenn mehr als eine Ausgabe vorhanden ist

Vorgeschlagene Routen:

```text
GET /daten/{entryId}
GET /datenreihen/{seriesId}
GET /datenreihen/{seriesId}/ausgaben/{issueId}
```

Wenn nur eine Detailroute umgesetzt wird, muss sie intern trotzdem zwischen `DatasetEntry`, `DatasetSeriesEntry` und `DatasetIssueEntry` unterscheiden.

### 14.9 CSS- und Accessibility-Regeln

- zurückhaltendes Behörden-/Fachportal-Design
- keine starken Schatten
- keine grossen Rundungen
- Grautöne und dunkles Blau/Grau als Basis
- Rot nur für Links, aktive Zustände und dezente Aktionen
- Fokuszustände sind sichtbar
- Downloadlinks sind echte Links
- Expand-Controls sind Buttons mit `aria-expanded`
- Tabellenkopf ist semantisch korrekt
- aktive Filter sind auch für Screenreader verständlich

### 14.10 UI-Tests

Mindesttests:

- Startseite rendert Header-Web-Component und Breadcrumb-Web-Component
- Default-Startseite ist Listenansicht
- Filter mit mehreren Werten werden korrekt aus Query-Parametern gelesen
- aktive Filterchips werden angezeigt
- `Datensatz` und `Datenreihe` nutzen denselben Badge-Klassenpräfix
- Listenansicht enthält keine Icons in der Titelspalte
- Datenreihen-Root-Zeile zeigt Formatlinks mit Kennzeichnung `aktuelle Ausgabe`
- Expand-Zustand zeigt Ausgabezeilen
- Kartenansicht zeigt `Open Data` und `Struktur beschrieben`
- Detailseite enthält keine Datenvorschau
- Datenreihen-/Ausgabe-Detailseite verlinkt weitere Ausgaben

Pixelgenaue Screenshot-Tests sind im MVP nicht erforderlich.

## 15. Admin und Reload

### 15.1 `AdminCatalogController`

```java
@RestController
@RequestMapping("/admin/catalog")
public final class AdminCatalogController {
    public AdminCatalogController(
        CatalogReloader reloader,
        CatalogService catalogService,
        ReloadTokenVerifier tokenVerifier,
        ReloadProperties reloadProperties
    );

    @PostMapping("/reload")
    public ResponseEntity<ReloadResponse> reload(HttpServletRequest request);

    @GetMapping("/status")
    public CatalogStatusResponse status();

    private void requireReloadEnabled();

    private void requireValidToken(HttpServletRequest request);
}
```

### 15.2 `ReloadTokenVerifier`

```java
@Component
public final class ReloadTokenVerifier {
    public ReloadTokenVerifier(ReloadProperties properties);

    public boolean isValid(Optional<String> providedToken);

    private boolean constantTimeEquals(String expected, String actual);
}
```

Header:

```text
X-Datenportal-Reload-Token: <secret>
```

Regeln:

- Fehlender/falscher Token: `401 Unauthorized`.
- Keine Detailangabe, ob Token fehlt oder falsch ist.
- Constant-time Vergleich.

### 15.3 Responses

```java
public record ReloadResponse(
    boolean success,
    String message,
    Instant loadedAt,
    String sourceDescription,
    String contentHash,
    int visibleDatasets,
    int visibleSeries,
    int visibleIssues,
    List<String> warnings
) {}

public record CatalogStatusResponse(
    Instant loadedAt,
    String sourceDescription,
    String contentHash,
    int visibleEntries,
    int visibleDatasets,
    int visibleSeries,
    int visibleIssues,
    List<String> warnings
) {}
```

HTTP-Status:

- `200 OK`: Reload erfolgreich.
- `401 Unauthorized`: Token fehlt/falsch.
- `503 Service Unavailable`: Reload deaktiviert.
- `422 Unprocessable Entity`: XTF konnte gelesen, aber nicht validiert werden.
- `502 Bad Gateway`: HTTP-Quelle nicht erreichbar oder liefert Fehler.
- `500 Internal Server Error`: unerwarteter Fehler.

---

## 16. Build und Dependencies

### 16.1 Gradle Groovy DSL

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.1.0'
    id 'io.spring.dependency-management' version '1.1.7'
    id 'gg.jte.gradle' version '3.2.4'
}

group = 'ch.so.agi'
description = 'Datenportal Webanwendung Kanton Solothurn'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

ext {
    luceneVersion = '10.3.2'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'gg.jte:jte-spring-boot-starter-4:3.2.4'

    implementation "org.apache.lucene:lucene-core:${luceneVersion}"
    implementation "org.apache.lucene:lucene-analysis-common:${luceneVersion}"
    implementation "org.apache.lucene:lucene-queryparser:${luceneVersion}"

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
    useJUnitPlatform()
}

jte {
    generate()
    binaryStaticContent = true
}
```

Fallback:

- Wenn Boot 4.1.0 oder JTE-Starter im konkreten Zeitpunkt Probleme macht: Spring Boot 4.0.x verwenden und im README dokumentieren.
- Kein Wechsel zu Thymeleaf ohne explizite Entscheidung.

### 16.2 Statische Assets

```text
src/main/resources/static/css/app.css
src/main/resources/static/js/htmx.min.js
src/main/resources/static/vendor/so-web-components/0.1.10/index.js
src/main/resources/static/vendor/so-web-components/0.1.10/styles/tokens.css
src/main/resources/static/vendor/so-web-components/0.1.10/styles/fonts.css
```

HTMX kann vendored werden. Kein npm-Build im MVP.

---

## 17. Testspezifikation

### 17.1 Testpakete

```text
src/test/java/ch/so/agi/datenportal/
  catalog/domain/
  catalog/parse/
  catalog/source/
  catalog/
  search/
  web/
  admin/
```

### 17.2 Domain-Tests

```java
class DatasetSeriesEntryTest {
    @Test void currentIssueReturnsExplicitCurrentIssue();
    @Test void currentIssueFallsBackToNewestModifiedIssueWhenNoneMarked();
    @Test void distributionsForListingUseCurrentIssue();
    @Test void issuesNewestFirstSortsByModifiedDescending();
}

class DistributionLinkTest {
    @Test void preferredHrefUsesDownloadUrlBeforeAccessUrl();
    @Test void displayLabelMapsPrimaryFormats();
}
```

### 17.3 Parser-Tests

```java
class XtfPublishedCatalogParserTest {
    @Test void parsesCatalogWithOneDataset();
    @Test void parsesCatalogWithDatasetSeriesAndIssues();
    @Test void parsesDistributionFormats();
    @Test void rejectsDatasetWithoutDistribution();
    @Test void rejectsSeriesWithoutIssues();
    @Test void rejectsInvalidDateWithUsefulPath();
    @Test void ignoresUnknownElements();
    @Test void disablesExternalEntities();
}
```

### 17.4 Validator-Tests

```java
class CatalogValidatorTest {
    @Test void rejectsDuplicateIdentifiersAcrossDatasetsSeriesAndIssues();
    @Test void warnsWhenMultipleCurrentIssuesExist();
    @Test void warnsWhenNoCurrentIssueExists();
    @Test void rejectsEmptyCatalog();
}
```

### 17.5 Search-Tests

```java
class LuceneSearchServiceTest {
    @Test void emptyQueryReturnsAllEntriesSortedByModifiedDescending();
    @Test void findsDatasetByTitle();
    @Test void findsDatasetByKeyword();
    @Test void findsDatasetByOfficeName();
    @Test void boostsExactIdentifier();
    @Test void findsSeriesByIssueLabel();
    @Test void doesNotReturnSeriesMoreThanOnce();
    @Test void handlesSpecialCharactersInQuery();
}
```

### 17.6 MVC-Tests

```java
@WebMvcTest(HomeController.class)
class HomeControllerTest {
    @Test void homePageShowsDatasetCards();
    @Test void listViewShowsRows();
    @Test void filterByThemeWorks();
    @Test void htmxRequestReturnsResultsFragmentOnly();
    @Test void catalogIgnoresPageAndSizeParameters();
}

@WebMvcTest(DatasetController.class)
class DatasetControllerTest {
    @Test void datasetDetailPageRendersMetadata();
    @Test void seriesDetailPageRendersIssueTable();
    @Test void unknownIdentifierReturns404();
}
```

### 17.7 Admin-Tests

```java
@WebMvcTest(AdminCatalogController.class)
class AdminCatalogControllerTest {
    @Test void reloadWithoutTokenReturns401();
    @Test void reloadWithWrongTokenReturns401();
    @Test void reloadWithCorrectTokenReturns200();
    @Test void statusReturnsCurrentSnapshotMetadata();
}

@SpringBootTest
class CatalogReloadIntegrationTest {
    @Test void successfulReloadReplacesSnapshot();
    @Test void failedReloadKeepsOldSnapshot();
}
```

---

## 18. Phasenplan mit Klassenfokus

### Phase 0 — Bootstrap

Implementieren:

- `DatenportalApplication`
- `HomeController` mit statischer Seite
- `PageChromeVm`, `HeaderVm`, `BreadcrumbVm`
- JTE Layout mit Header/Breadcrumb-Fallback
- Basis-CSS

Tests:

- `DatenportalApplicationTests.contextLoads()`
- `HomeControllerTest.homePageReturns200()`

DoD:

- `./gradlew test`
- `./gradlew bootRun`

### Phase 1 — Domain-Read-Model ohne XTF

Implementieren:

- Alle Records in `catalog/domain`
- `CatalogSnapshot`
- `StaticCatalogFactory` test/lokal
- `CatalogService`
- einfache Cards/List aus statischem Katalog

Tests:

- Domain-Tests für Datenreihe/current issue
- MVC-Test für Darstellung

### Phase 2 — XTF-Parser

Implementieren:

- `CatalogSource`, `CatalogBytes`, `ClasspathCatalogSource`
- `PublishedCatalogParser`, `XtfPublishedCatalogParser`
- `CatalogValidator`
- Startup Loading

Tests:

- Parser- und Validator-Tests gegen Fixtures

### Phase 3 — Suche/Filter ohne Lucene, View Models stabilisieren

Implementieren:

- `CatalogQueryParams`
- `SearchQuery`, `SearchFilters`, `ModifiedRange`, `SortMode`
- `FacetService`
- `HomePageVmFactory`, `ResultsVmFactory`
- GET-Filter, Cards/List ohne sichtbare Pagination
- HTMX-Fragmente

Tests:

- MVC Filter/Sort/View/HTMX

### Phase 4 — Lucene

Implementieren:

- `SearchIndexBuilder`
- `LuceneSearchIndex`
- `LuceneSearchService`
- Snapshot enthält Index

Tests:

- Search-Tests
- Reload baut Index neu

### Phase 5 — Detailseiten

Implementieren:

- `DatasetController`
- `DatasetDetailPageVm`, `SeriesDetailPageVm`
- `detail-dataset.jte`, `detail-series.jte`
- Issue-Tabelle

Tests:

- Detailseiten und 404

### Phase 6 — Web Components final integrieren

Implementieren:

- vendored `so-web-components`
- `WebComponentsProperties`
- `WebAssetsVmFactory`
- `soHeader.jte`, `soBreadcrumb.jte`
- Fallbacks

Tests:

- HTML enthält Web Component Tags
- Fallback wenn deaktiviert

### Phase 7 — HTTP-Quelle und Reload

Implementieren:

- `HttpCatalogSource`
- `CatalogReloader`
- `AdminCatalogController`
- `ReloadTokenVerifier`
- Status/Reload JSON

Tests:

- Reload-Integration
- Security Tests

### Phase 8 — Hardening und Doku

Implementieren/Dokumentieren:

- `docs/architecture.md`
- `docs/configuration.md`
- `docs/operations.md`
- `docs/web-components.md`
- Fehlerseiten
- Actuator Health/Info
- Caching statische Assets

Tests:

- `./gradlew check`
- Smoke-Test lokal

---


### UI-Phasen — verbindliche Ergänzung zum Phasenplan

Die folgenden UI-Phasen ergänzen die fachlichen Phasen 0–8. Sie dürfen in dieselben technischen Phasen integriert werden, müssen aber jeweils separat getestet und dokumentiert sein.

#### UI-1 — Page Chrome mit Web Components

Ziel: Header und Breadcrumb gemäss `web-components.png` integrieren.

Umfang:

- vendored Web-Component-Assets einbinden
- `HeaderViewModelFactory` und `BreadcrumbFactory` implementieren
- `layouts/main.jte`, `components/chrome/header.jte`, `components/chrome/breadcrumb.jte`
- semantische Fallbacks
- Tests für Vorhandensein der Component-Tags und Breadcrumb-Items

DoD:

- `./gradlew clean check`
- Startseite und Detailseite verwenden denselben Page Chrome
- keine handgebaute Hauptnavigation als primäre Lösung

#### UI-2 — Startseite Listenansicht

Ziel: `startseite_liste.png` als initiale Startseite umsetzen.

Umfang:

- Suchfeld
- Mehrfachfilter mit aktiven Chips
- Listenansicht als Default
- graue Typ-Badges für `Datensatz` und `Datenreihe`
- keine Icons in der Titelspalte
- direkte Downloadlinks
- Sortierung ohne sichtbare Pagination

DoD:

- HTML-Strukturtests für Tabelle, Filter, aktive Chips und Downloadlinks
- normale GET-Requests funktionieren ohne HTMX

#### UI-3 — Datenreihen-Expansion

Ziel: Datenreihen in der Liste aufklappbar machen.

Umfang:

- Plus-/Minus-Button mit `aria-expanded`
- Row-Klick als progressive Enhancement
- HTMX-Fragment für Ausgabezeilen
- Fallback über `expanded=<seriesId>`
- Root-Downloads mit Kennzeichnung `(aktuelle Ausgabe)`
- Ausgabezeilen mit eigenen Downloadlinks

DoD:

- Tests für Root-Zeile, Expanded-Zeilen und Nicht-Auslösen bei Download-/Info-Klicks soweit serverseitig prüfbar

#### UI-4 — Kartenansicht

Ziel: Kartenansicht gemäss `cards.png` umsetzen.

Umfang:

- View Toggle `view=cards`
- Cards mit Titel, Beschreibung, grauen Keyword-Badges
- Typ-Badge gleich wie Listenansicht
- `Open Data` Badge
- `Struktur beschrieben` Badge
- Datum und Detailpfeil

DoD:

- Tests für Cards-HTML und URL-Erhalt der Filter bei View-Wechsel

#### UI-5 — Detailseiten ohne Datenvorschau

Ziel: klare Detailseiten aus PublishedCatalog-Metadaten.

Umfang:

- normaler Datensatz
- Datenreihe / aktuelle Ausgabe
- historische Ausgaben / weitere Ausgaben
- Downloadbereich
- strukturierte Metadaten
- keine Preview-Tabellen oder Diagramme

DoD:

- Tests für Detailseiten, Downloadlinks und Verweise zwischen Ausgaben


## 19. Akzeptanzkriterien MVP

Der MVP ist erfüllt, wenn:

- Die Anwendung mit Java 25 startet.
- Package-Basis ist `ch.so.agi.datenportal`.
- Die PublishedCatalog-XTF-Datei wird beim Start geladen.
- Normale Datensätze und Datenreihen werden korrekt normalisiert.
- Startseite zeigt alle sichtbaren Top-Level-Einträge.
- Suche funktioniert mit Lucene.
- Filter nach Thema, Fachstelle/Amt und Aktualisierungszeitraum funktionieren.
- Cards-/Listenansicht funktionieren ohne JavaScript.
- HTMX aktualisiert Resultate progressiv.
- Detailseiten für Datensatz und Datenreihe existieren.
- CSV/XLSX/Parquet-Links werden korrekt aus `downloadURL` oder `accessURL` gebildet.
- Datenreihen zeigen aktuelle und historische Ausgaben verständlich.
- Header und Breadcrumb werden über `so-web-components` oder dokumentierten Fallback gerendert.
- Reload-Endpunkt ist geschützt und atomar.
- Fehlerhafter Reload lässt alten Snapshot aktiv.
- Tests und Dokumentation sind vorhanden.

---

## 20. Coding-Agent-Regeln

Der Agent muss:

- Pro Phase klein und reviewbar arbeiten.
- Keine Schichten vermischen.
- Records und Methoden so implementieren, wie in dieser Spezifikation beschrieben, ausser ein klar dokumentierter Grund spricht dagegen.
- Keine DB einführen.
- Keine SPA bauen.
- Keine zusätzliche JavaScript-Logik einführen, wenn HTMX oder serverseitiges Rendering genügt.
- Lizenzierte Fontdateien dürfen für dieses Projekt eingebunden werden; dabei keine externen Font-CDNs, keine absoluten lokalen Pfade und keine unklaren Dritt-Assets verwenden.
- Keine Secrets committen.
- Nach jeder Phase testen und dokumentieren.
- Nach erfüllter Definition of Done den `commit-after-dod` Skill verwenden.

Pflichtchecks vor Abschluss einer Phase:

```bash
./gradlew test
```

Wenn vorhanden:

```bash
./gradlew check
```

---

## 21. Konkrete erste Prompts für Codex/OpenCode

### Phase 0

```text
Arbeite gemäss datenportal_webapp_agent_spec_detailed.md. Implementiere Phase 0. Nutze Package ch.so.agi.datenportal. Erstelle ein Java-25/Spring-Boot-4/JTE-Projekt mit minimaler Startseite, Header/Breadcrumb-Fallback und Tests. Keine XTF-Logik in dieser Phase. Nach erfolgreichem ./gradlew test verwende den commit-after-dod Skill.
```

### Phase 1

```text
Implementiere Phase 1 aus datenportal_webapp_agent_spec_detailed.md. Erstelle das immutable Domain-Read-Model für CatalogSnapshot, CatalogEntry, DatasetEntry, DatasetSeriesEntry, DatasetIssueEntry und DistributionLink. Baue eine StaticCatalogFactory für Entwicklung/Tests und rendere echte Daten auf der Startseite. Tests für Current-Issue-Logik und Downloadbuttons sind Pflicht.
```

### Phase 2

```text
Implementiere Phase 2 aus datenportal_webapp_agent_spec_detailed.md. Erstelle CatalogSource, ClasspathCatalogSource, XtfPublishedCatalogParser und CatalogValidator. Die App soll mit einer PublishedCatalog-XTF-Fixture starten. Parser muss namespace-aware und XXE-sicher sein. Tests für normale Datensätze, Datenreihen, Distributionen und Fehlerfälle sind Pflicht.
```
