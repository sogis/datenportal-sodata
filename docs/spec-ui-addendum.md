# Spec-UI-Addendum v5

Dieses Addendum ist dafür gedacht, in eine bestehende `datenportal_webapp_agent_spec_detailed.md` übernommen zu werden. Es ersetzt alle älteren UI-Aussagen zu Startseite, Kartenansicht, Listenansicht, Detailseiten, Header und Breadcrumb.

## Zu ersetzende/zu ergänzende Spezifikationsbereiche

In der bestehenden Spezifikation müssen die Abschnitte zu UI, Mockups, Page Chrome, Header/Breadcrumb, Startseite, Kartenansicht, Listenansicht und Detailseiten mit dem Inhalt aus `docs/ui-implementation-contract.md` ersetzt oder ergänzt werden.

## Neuer verbindlicher UI-Grundsatz

Die Startseite ist im MVP die funktionale Katalogseite. Sie rendert initial alle Top-Level-Katalogeinträge in Listenansicht. Die verbindliche visuelle Referenz ist `spec/mockups/current/startseite_liste.png`.

Der Header und das Breadcrumb werden über die Web Components aus `so-web-components` integriert. Die visuelle Referenz ist `spec/mockups/current/web-components.png`. Es darf kein eigener Header als primäre Lösung nachgebaut werden.

Die Kartenansicht ist eine alternative Ansicht der Start-/Katalogseite. Die verbindliche visuelle Referenz ist `spec/mockups/current/cards.png`.

## Startseite Listenansicht

- Initiale Ansicht: `view=list`.
- Spalten: `Thema / Datensatz`, `Typ`, `Publiziert`, `Details`, `Daten herunterladen`.
- Keine Icons in der ersten Spalte, ausser Plus/Minus bei Datenreihen.
- `Datensatz` und `Datenreihe` verwenden denselben grauen Badge-Stil.
- Datenreihen sind aufklappbar.
- Root-Zeile einer Datenreihe zeigt Downloads der aktuellen Ausgabe als `CSV`, `XLSX`, `Parquet`.
- Aufgeklappte Ausgabezeilen zeigen die Formate ebenfalls ohne Zusatz `aktuelle Ausgabe`.
- Info-Link der Root-Zeile führt auf die Detailseite der aktuellen Ausgabe.
- Klick auf Info-Link oder Downloadlink darf das Aufklappen nicht auslösen.

## Filter

- Mehrfachauswahl pro Filtergruppe.
- Werte werden als wiederholte Query-Parameter codiert.
- Aktive Filter werden als Chips sichtbar gemacht.
- Jeder Chip ist einzeln entfernbar.
- Es gibt `Filter zurücksetzen`.
- HTMX ist progressive Enhancement; normale GET-Requests bleiben vollständig funktionsfähig.

## Kartenansicht

- `Datensatz` und `Datenreihe` verwenden denselben grauen Typ-Badge.
- Badge `Open Data` wird im MVP hardcodiert angezeigt.
- Badge `Struktur beschrieben` wird angezeigt, wenn Attribute beschrieben sind oder ein Datenmodell vorhanden ist.
- Titel, Beschreibung, Keyword-Chips, Datum und Pfeil/Link zur Detailseite folgen `cards.png`.

## Detailseiten

- Keine Datenvorschau im MVP.
- Normale Datensatz-Detailseite zeigt Titel, Beschreibung, Badges, Downloads und strukturierte Metadaten.
- Detailseite einer Datenreihen-Ausgabe zeigt den Serienkontext im Kicker und danach die Dataset-Detail-Cards.
- Die Root-Datenreihe aus der Liste verlinkt per Info-Link zur aktuellen Ausgabe.
