# Architektur

Die Anwendung ist eine serverseitig gerenderte Spring-Boot-Webanwendung mit JTE, HTMX als Progressive Enhancement, `so-web-components` für Header/Breadcrumb und einem immutable In-Memory-Katalog.

## Systemübersicht

Die zentrale Architekturidee ist ein vollständiger, atomar austauschbarer `CatalogSnapshot`. Parser, Validierung und Lucene-Indexing bauen immer erst einen neuen Kandidaten auf; veröffentlicht wird erst der komplette, konsistente Stand.

```mermaid
flowchart LR
    source["CatalogSource<br/>Classpath / File / HTTP"]
    parser["Parser / Validator<br/>XTF nach Domain"]
    builder["CatalogSnapshotBuilder"]
    snapshot["CatalogSnapshot<br/>Read-Model + Lookup-Maps"]
    lucene["Lucene Index<br/>Top-Level-Suchdokumente"]
    service["CatalogService<br/>aktiver Snapshot"]
    web["Web Controller"]
    vm["ViewModel Factories"]
    jte["JTE Templates"]
    browser["Browser"]
    admin["Admin Reload"]

    source --> parser
    parser --> builder
    builder --> lucene
    builder --> snapshot
    lucene --> snapshot
    snapshot --> service
    admin --> service
    service --> web
    web --> vm
    vm --> jte
    jte --> browser
    web --> lucene
```

## Laufzeitmodell

Beim Start oder Reload wird die PublishedCatalog-XTF/XML-Quelle vollständig geladen, sicher geparst, validiert, in ein Domain-Read-Model überführt und mit einem vollständig neu gebauten Lucene-Index als `CatalogSnapshot` veröffentlicht.

Der aktive Snapshot enthält:

- normale Datensätze
- Datenreihen
- Ausgaben von Datenreihen
- sichtbare Top-Level-Einträge
- Lookup-Maps für Detailseiten
- Ladezeitpunkt, Source-Beschreibung und Content-Hash
- aktiven `CatalogSearchIndex`

`CatalogService` hält den Snapshot in-memory und tauscht ihn atomar unter einem Read/Write-Lock. Öffentliche Controller verwenden `withSnapshot(...)`, damit Suche, Facetten und ViewModel-Aufbau konsistent denselben Katalogstand sehen.

## Materialisierte Pakete

```text
ch.so.agi.datenportal
  admin.actuator
  admin.reload
  catalog.domain
  catalog.importxtf
  catalog.service
  config
  search
  support
  web
  web.view
```

Wichtige Verantwortlichkeiten:

- `catalog.importxtf`: Katalogquellen, Byte-Lesen, XTF/XML-Parser, XML-Sicherheit und Validierung.
- `catalog.service`: Snapshot-Building, initialer Load, atomarer Reload und aktiver Snapshot.
- `search`: Lucene-Dokumente, Indexaufbau, Suche, Filter, Sortierung und Facetten.
- `web`: Controller, Query-Parameter, ViewModel-Factories, Page Chrome, Fehlerseiten.
- `admin.reload`: geschützte JSON-Endpunkte für Reload und Status.
- `admin.actuator`: Health- und Info-Beiträge für Betrieb.
- `config`: Properties, statisches Asset-Caching und Security-Header.

## Startup

1. Spring bindet `datenportal.catalog.*`.
2. `CatalogSource` lädt die vollständigen Katalogbytes.
3. `XtfPublishedCatalogParser` parst namespace-aware und XXE-sicher, inklusive exakter `accessRights` sowie strukturbezogener Metadaten (`attributes`, `model`).
4. `CatalogValidator` prüft Pflichtregeln.
5. `CatalogSearchIndexBuilder` baut einen neuen In-Memory-Lucene-Index.
6. `CatalogSnapshotBuilder` erzeugt den immutable `CatalogSnapshot`.
7. `CatalogService` stellt den Snapshot für Web, Suche und Actuator bereit.

Fehlschläge beim initialen Laden sind fail-fast. Die Anwendung startet nicht still mit leerem Katalog.

## Reload

`POST /admin/catalog/reload` ist über `X-Reload-Token` geschützt. Der Token stammt aus `datenportal.admin.reload-token`, typischerweise `DATENPORTAL_ADMIN_RELOAD_TOKEN`.

Reload-Ablauf:

1. Quelle laden.
2. Kandidat parsen.
3. Kandidat validieren.
4. Kandidaten-Index bauen.
5. Kandidaten-Snapshot erzeugen.
6. Snapshot und Index atomar veröffentlichen.

Bei Fehlern bleibt der alte Snapshot samt altem Lucene-Index aktiv. Parallel laufende Reloads werden mit `409 Conflict` abgelehnt.

## Detaillierter Datenfluss vom XTF bis ins GUI

Das folgende Sequenzdiagramm beschreibt den heutigen Ist-Zustand der Laufzeitkette. Wichtig dabei: Lucene dient der Katalogsuche, nicht als primäre Quelle für Detailseiten. Detailseiten lesen ihre Einträge direkt aus dem aktiven Snapshot. Trefferlisten werden nach der Lucene-Suche wieder über Treffer-IDs auf `CatalogEntry`-Objekte im Snapshot aufgelöst.

```mermaid
sequenceDiagram
    autonumber
    participant XTF as "PublishedCatalog XTF/XML"
    participant Source as "CatalogSource"
    participant Loader as "CatalogSnapshotLoader"
    participant Builder as "CatalogSnapshotBuilder"
    participant Parser as "XtfPublishedCatalogParser"
    participant Validator as "CatalogValidator"
    participant Mapper as "CatalogDocumentMapper"
    participant Lucene as "Lucene Index"
    participant Snapshot as "CatalogSnapshot"
    participant Service as "CatalogService"
    participant Browser as "Browser"
    participant CatalogCtl as "CatalogController"
    participant DetailCtl as "CatalogDetailController"
    participant Search as "CatalogSearchService"
    participant Facets as "FacetService"
    participant HomeVm as "HomePageVmFactory / ResultsVmFactory"
    participant DetailVm as "DetailPageVmFactory"
    participant JTE as "JTE Templates"

    Note over XTF,Service: Start oder Reload
    Source->>XTF: XTF-Quelle lesen
    XTF-->>Source: Bytes
    Source-->>Loader: CatalogBytes
    Loader->>Builder: build(bytes)
    Builder->>Parser: parse(inputStream, sourceDescription)
    Parser-->>Builder: Catalog mit Datasets, Series, Issues, exakten Access-Levels und Resource-Metadaten
    Builder->>Validator: validate(catalog)
    Validator-->>Builder: OK oder Fehler
    loop je Top-Level-Eintrag
        Builder->>Mapper: toDocument(entry)
        Mapper->>Lucene: addDocument(...)
    end
    Lucene-->>Builder: CatalogSearchIndex
    Builder->>Snapshot: CatalogSnapshot.of(catalog, ..., searchIndex)
    Note right of Snapshot: sichtbare Top-Level-Einträge,<br/>visibleEntriesByIdentifier,<br/>allEntriesByIdentifier
    Snapshot-->>Builder: fertiger Snapshot
    Builder-->>Loader: CatalogBuildResult(snapshot, warnings)
    Loader-->>Service: initialer Snapshot
    opt administrativer Reload
        Builder-->>Service: neuer Snapshot
        Service->>Snapshot: atomarer Austausch
        Note right of Service: alter Snapshot inklusive altem Index<br/>wird erst nach dem Tausch geschlossen
    end

    Note over Browser,JTE: Katalogseite und Suche
    Browser->>CatalogCtl: GET / oder /datasets?q=...
    CatalogCtl->>Service: withSnapshot(...)
    Service-->>CatalogCtl: aktiver Snapshot
    CatalogCtl->>Search: search(snapshot, query)
    alt Textsuche vorhanden
        Search->>Lucene: snapshot.searchIndex().search(q)
        Lucene-->>Search: Treffer-IDs und Scores
        loop je Treffer
            Search->>Snapshot: findVisibleEntry(entryId)
            Snapshot-->>Search: CatalogEntry
        end
    else keine Textsuche
        Search->>Snapshot: visibleEntries()
        Snapshot-->>Search: sichtbare Top-Level-Einträge
    end
    Search->>Search: Filter, Sortierung und Pagination
    CatalogCtl->>Facets: compute(snapshot)
    Facets->>Snapshot: visibleEntries()
    Snapshot-->>Facets: Einträge für Themen, Ämter und Datumsbereiche
    CatalogCtl->>HomeVm: create(snapshot, searchResult, params, facets)
    HomeVm-->>CatalogCtl: CatalogPageVm / ResultsVm
    CatalogCtl->>JTE: render pages/catalog oder fragments/catalogResults
    JTE-->>Browser: HTML

    Note over Browser,JTE: Detailseite eines Datensatzes
    Browser->>DetailCtl: GET /datasets/{identifier}
    DetailCtl->>Service: withSnapshot(...)
    Service-->>DetailCtl: aktiver Snapshot
    DetailCtl->>Snapshot: findAnyEntry(identifier)
    Snapshot-->>DetailCtl: DatasetEntry
    DetailCtl->>DetailVm: dataset(datasetEntry)
    DetailVm-->>DetailCtl: DatasetDetailPageVm
    DetailCtl->>JTE: render pages/datasetDetail
    JTE-->>Browser: HTML
```

## Web Layer

Die Katalogseite auf `/` und `/datasets` rendert Suche, Mehrfachfilter, Listenansicht, Kartenansicht und HTMX-Fragmente. Detailseiten liegen unter:

```text
/datasets/{identifier}
/series/{seriesIdentifier}
/series/{seriesIdentifier}/issues/current
/series/{seriesIdentifier}/issues/{issueIdentifier}
```

Die fachliche Soll-Semantik der Katalogsuche ist in [search.md](search.md) beschrieben.

JTE-Templates erhalten nur vorbereitete ViewModels. Fachlogik bleibt in Domain, Services und Factories.

## Fehlerseiten

Fachliche 404s über `CatalogNotFoundException`, statische 404s und der generische Boot-Fehlerpfad werden kontrolliert gerendert:

- `pages/notFound.jte` für 404
- `pages/error.jte` für 5xx und andere Fehler

Beide nutzen den normalen Page Chrome mit Header/Breadcrumb. Stacktraces, Exception-Klassen und interne Pfade werden nicht an Nutzer ausgeliefert.

## Actuator

Exponiert sind nur:

```text
/actuator/health
/actuator/info
```

Eigene Health-Komponenten:

- `catalogSnapshot`: aktiver Snapshot und Counts.
- `catalogReload`: Reload-Laufstatus und letzter erfolgreicher/fehlgeschlagener Reload.
- `catalogSearchIndex`: Verfügbarkeit des aktiven Lucene-Index.

Der Info-Endpunkt enthält App-Name, Package-Basis, Java-Version und optional Gradle-Build-Informationen.

## Static Assets und Header

Statische CSS-, HTMX-, Web-Component-, Font- und optionale Bildpfade werden über `StaticAssetCachingConfiguration` mit Cache-Headern ausgeliefert. Versionierte Web-Component-Assets erhalten eine lange TTL, nicht fingerprinted CSS eine kurze TTL.

`SecurityHeadersConfiguration` setzt grundlegende sichere Response-Header:

- `X-Content-Type-Options`
- `Referrer-Policy`
- `X-Frame-Options`
- `Permissions-Policy`
- `Content-Security-Policy`

## Bewusste Grenzen

- Keine Datenbank.
- Kein Login-System.
- Kein Admin-UI.
- Keine CI/CD-Pipeline.
- Keine Datenvorschau.
- Keine fachlichen Such- oder UI-Erweiterungen in Phase 8.
