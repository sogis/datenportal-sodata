---
name: lucene-search
description: Use for Apache Lucene indexing, search queries, ranking, filtering integration, reindexing, and tests for the Datenportal catalog.
compatibility: codex, opencode
metadata:
  project: datenportal
---

# Skill: Lucene Search

Use this skill for search and index work.

## Search goals

Users should find datasets and data series by:

- title
- identifier
- description
- theme/topic
- Fachstelle/Amt
- keywords/tags
- formats such as CSV, XLSX, Parquet
- year/date for data series issues where useful

## Indexing rules

- Build the index from a complete catalog snapshot.
- Do not mutate the active index during a failed reload.
- Prefer a new in-memory or temporary index per candidate snapshot, then atomically swap.
- Keep index field names documented and centralized.
- Treat normal datasets and data series uniformly for search results, but preserve their type.
- Index the series root as the catalog result, not every historical issue as a separate top-level search result unless the specification changes.

## Suggested fields

- `id` stored, exact
- `type` stored/filterable: `dataset` or `series`
- `title` indexed/stored, boosted
- `description` indexed/stored
- `identifier` indexed/stored, boosted exact-ish
- `theme` indexed/stored/filterable
- `office` indexed/stored/filterable
- `keywords` indexed
- `formats` indexed/filterable
- `publicationDate` stored/sortable
- `currentIssueYear` stored/sortable for data series
- `issueYears` indexed for data series
- `openData` stored/filterable if represented in the model
- `structureDescribed` stored/filterable if represented in the model/view model

## Query behavior

- Empty query returns all entries subject to filters.
- Multi-word queries should behave reasonably without exact phrase requirement.
- Identifier matches should rank high.
- Title matches should rank above description-only matches.
- Filters should combine with query using AND semantics.
- Multiple values inside one filter category should behave as OR unless documented otherwise.
- Invalid user query syntax must not crash the application.

## Tests

Cover:

- empty query
- title search
- identifier search
- theme search/filter
- office filter
- format filter
- multi-select filters
- no results
- dataset series found by current and historical issue metadata if indexed
- index swap after reload

## Documentation

Document:

- indexed fields
- boost/ranking assumptions
- how filters interact with search
- reindexing behavior during reload
