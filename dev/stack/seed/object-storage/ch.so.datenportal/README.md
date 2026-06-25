# Datenportal PublishedCatalog Fixtures – erweitert

Erzeugt am 2026-06-24.

## Inhalt

- `models/SO_AGI_DataCatalog_PublishedCatalog_20260602.ili` – korrigiertes Modell mit `DataResource.attributes` und `DataResource.model`.
- `testdata/published_catalog_examples_only.xtf` – kleine Fixture mit den vier aus echten Beispielen abgeleiteten Ressourcen.
- `testdata/published_catalog_full_62_entries.xtf` – grosse Fixture mit Examples, fiktiven älteren Ressourcen, zusätzlichen nicht-öffentlichen Ressourcen und zusätzlichen Bundes-/Gemeinde-Ressourcen.
- `downloads/*.csv`, `downloads/*.xlsx`, `downloads/*.parquet` – Dummy-Beispieldaten für alle materialisierten Datensätze und DatasetIssues.
- `metadata/data_file_manifest.csv` – Übersicht aller erzeugten Download-Dateien.

## Datumslogik

- Die vier `examples_only`-Ressourcen und ihre Issues haben `issued`/`modified = 2026-06-24`.
- Alle übrigen fiktiven Ressourcen haben `issued`/`modified <= 2026-06-10`.

## CSV-Format

Die CSV-Dateien sind Excel-freundlich erzeugt: UTF-8 mit BOM, Semikolon als Trennzeichen, CRLF-Zeilenenden und Punkt als Dezimaltrennzeichen.

## Hinweis

Die Parquet-Dateien sind kleine, unkomprimierte Dummy-Dateien mit einfachem UTF8/INT32/DOUBLE-Schema pro Attributliste. Sie dienen als Download-/Preview-Fixtures, nicht als fachliche Referenzdaten.
