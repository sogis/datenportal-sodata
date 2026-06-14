---
name: xtf-publishedcatalog
description: Use for SO_AGI_DataCatalog_PublishedCatalog_20260602 XTF/XML parsing, fixtures, validation behavior, dataset series handling, and runtime snapshot reloads.
compatibility: codex, opencode
metadata:
  project: datenportal
  model: SO_AGI_DataCatalog_PublishedCatalog_20260602
---

# Skill: XTF PublishedCatalog

Use this skill for everything related to the PublishedCatalog runtime configuration.

## Model contract

Runtime input is a XTF/XML file based on:

```text
SO_AGI_DataCatalog_PublishedCatalog_20260602.ili
```

Do not treat Datasheet-XTF examples as the runtime contract. They can inspire test content, but runtime fixtures must match PublishedCatalog.

## Domain concepts

Represent at least:

- `Dataset`: normal dataset with direct distributions/resources
- `DatasetSeries`: data series with multiple issues
- `DatasetIssue`: one concrete issue/edition of a data series
- `Distribution` or resource/download representation
- format information for CSV, XLSX, Parquet and optionally API
- theme/topic
- responsible office / Fachstelle / Amt
- publication/update dates
- license and contact metadata
- identifiers and stable URLs/slugs

## Parser rules

- Parse into intermediate DTOs only if it improves validation.
- Convert DTOs into immutable domain objects.
- Validate required fields before activating a snapshot.
- Preserve identifiers exactly.
- Keep raw parser details out of UI controllers.
- Log useful errors without exposing secrets.
- Use deterministic ordering for datasets, series and issues.

## Series handling

For `DatasetSeries`:

- determine the current issue explicitly, preferably by metadata, otherwise by latest publication/reference year with documented rule
- keep historical issues ordered descending by year/date
- expose downloads for current issue and each historical issue
- make it impossible for UI code to confuse a series with a normal dataset
- ensure root series downloads always refer to the current issue
- expose detail links for the series/current issue and for historical issues

## Reload behavior

Reload must be atomic:

1. download or read candidate XTF/XML
2. parse candidate into a new snapshot
3. validate candidate snapshot
4. build candidate Lucene index
5. swap snapshot and index together
6. keep old snapshot active if any step fails

## Tests

Cover:

- normal dataset fixture
- data series with at least three issues
- missing required field
- invalid XML
- duplicate identifiers
- missing CSV/XLSX/Parquet resource if required by project rules
- current issue selection
- deterministic issue ordering
- atomic reload failure

## Documentation

Document:

- expected XTF location/config property
- local mock endpoint or fixture path
- error behavior
- reload behavior
- assumptions that are not enforced by the INTERLIS model
