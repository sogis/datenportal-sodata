# Verwendung mit Codex und OpenCode

## Ziel

Dieses Paket soll verhindern, dass der Agent das GUI aus älteren Mockups oder aus freier Interpretation baut. Der Agent muss vor UI-Arbeit den UI-Contract lesen und gegen ihn testen.

## Einbau ins Zielrepo

1. ZIP entpacken.
2. Dateien ins Zielrepo kopieren.
3. `docs/ui-implementation-contract.md`, `docs/spec-ui-addendum.md`, `docs/component-map.md` und `spec/mockups/current/*.png` committen.
4. Skill unter `.agents/skills/datenportal-ui-contract/SKILL.md` committen.
5. Optional OpenCode-Command unter `.opencode/commands/implement-ui-contract.md` übernehmen.
6. `AGENTS.md` ergänzen:

```md
## UI contract

Before changing UI, JTE templates, HTMX fragments, CSS, header/breadcrumb integration, filters, catalog result views or detail pages, read `docs/ui-implementation-contract.md` and apply `.agents/skills/datenportal-ui-contract/SKILL.md`.

The current authoritative mockups are:
- `spec/mockups/current/startseite_liste.png`
- `spec/mockups/current/cards.png`
- `spec/mockups/current/web-components.png`

Older UI mockups are secondary and must not override the UI contract.
```

## Beispielprompt für Codex/OpenCode

```text
Lies zuerst AGENTS.md, docs/ui-implementation-contract.md, docs/component-map.md und spec/mockups/current/startseite_liste.png. Implementiere Phase UI-2: Katalogseite in Listenansicht. Header und Breadcrumb sind bereits via Web Components vorgesehen. Baue Mehrfachfilter mit sichtbaren aktiven Chips, die Tabellenansicht ohne Themenicons, graue Typ-Badges für Datensatz/Datenreihe, Downloads der aktuellen Ausgabe bei Datenreihen und MVC-Tests. Danach nutze den commit-after-dod Skill.
```

## Reihenfolge der UI-Umsetzung

1. Web Components / Page Chrome
2. Listenansicht mit Filtern
3. Datenreihen-Expansion
4. Kartenansicht
5. Detailseiten

Nicht alles in einem Commit umsetzen. Jede Phase muss lauffähig, getestet und dokumentiert sein.

## Was der Agent bei jedem UI-Commit melden soll

- verwendete Referenzdateien
- geänderte JTE-Komponenten
- geänderte ViewModels/Factories
- Tests, die die HTML-Struktur absichern
- noch offene UI-Abweichungen vom Contract

