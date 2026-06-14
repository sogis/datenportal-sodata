---
name: jte-htmx-ui
description: Use for JTE templates, HTMX interactions, server-rendered UI, cards/list view, filters, detail pages, and minimal JavaScript islands.
compatibility: codex, opencode
metadata:
  project: datenportal
---

# Skill: JTE + HTMX UI

Use this skill for all frontend and template work. Also read `datenportal-ui-contract` before changing any UI.

## UI principles

- Server-rendered HTML first.
- HTMX for partial updates.
- No JavaScript unless HTMX and semantic HTML are insufficient.
- If JavaScript is necessary, implement a small isolated island and document why.
- Keep templates simple; complex decisions belong in view models.
- Preserve useful non-HTMX fallbacks with normal forms and links.

## Visual direction

Follow the Datenportal UI contract:

- official, calm, serious
- white background
- dark blue-gray text
- subtle gray borders
- Solothurn red as restrained accent
- minimal shadows
- modest border radius
- no playful animations

## Required pages

### Start / overview

Must support:

- all datasets and data series visible by default
- default list view based on `spec/mockups/current/startseite_liste.png`
- search input
- multi-select filter by theme
- multi-select filter by Fachstelle/Amt
- multi-select filter by publication date
- multi-select filter by resource type
- visible active filter chips
- cards view
- list view
- sort by newest first

### Normal dataset detail

Must show:

- title
- description
- badges such as Datensatz/Open Data/Aktualisiert where available
- downloads: CSV, XLSX, Parquet
- metadata panel
- technical access if API exists
- no data preview in the MVP

### Data series / issue detail

Must show:

- title
- badge Datenreihe or issue context
- current issue clearly identified
- current issue downloads
- historical issue links/list
- series/issue metadata
- no data preview in the MVP

## HTMX rules

- Use normal links and forms as fallback where possible.
- HTMX endpoints should return fragments, not full pages, unless requested.
- Keep fragment names explicit.
- Preserve browser history for meaningful state changes if useful.
- Avoid hidden client-side state; selected filters should be represented in query parameters.
- For expandable series rows, prevent row expansion when clicking info or download links.

## Accessibility rules

- Use semantic headings in order.
- Every form control needs a label.
- Icon-only buttons need accessible names.
- Expand/collapse controls need `aria-expanded`.
- Links must describe their target; avoid many identical “Mehr erfahren” links without context.
- Ensure keyboard navigation works.

## Tests

Prefer controller/template tests for:

- overview renders with empty query
- search renders filtered result
- multi-filter selections render active chips
- cards/list toggle
- series row expansion fragment
- normal dataset detail
- data series / issue detail
- no-result state

## Anti-patterns

- business logic in JTE templates
- JS-only filtering
- duplicate rendering logic in multiple templates
- fragile CSS tied to generated IDs
- visual-only badges without semantic text
