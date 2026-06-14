---
name: datenportal-webapp
description: Use for general implementation of the Datenportal Spring Boot/JTE/HTMX web application, including architecture, project structure, domain boundaries, tests, and documentation.
compatibility: codex, opencode
metadata:
  project: datenportal
  package_base: ch.so.agi.datenportal
---

# Skill: Datenportal Webapp

Use this skill for general implementation work in the Datenportal web application.

## Required reading

Before coding, read:

1. `AGENTS.md`
2. `datenportal_webapp_agent_spec_detailed_v5.md`
3. relevant docs in `docs/`
4. more specific skills when applicable

## Core architecture

Build a server-side rendered Spring Boot application with clear boundaries:

- Catalog/domain model
- PublishedCatalog XTF import
- Immutable runtime snapshot
- Lucene search index
- JTE templates and HTMX fragments
- Admin/reload operations

Prefer boring, explicit Java over clever abstractions.

## Technology defaults

- Java 25
- Spring Boot 4.1.0, with documented fallback to 4.0.x if needed
- Gradle Groovy DSL
- JTE templates
- HTMX for dynamic filtering and partial updates
- Apache Lucene for search
- JUnit/Jupiter and Spring Boot test support

## Package guidance

Use the base package:

```text
ch.so.agi.datenportal
```

Recommended structure:

```text
ch.so.agi.datenportal.catalog.domain
ch.so.agi.datenportal.catalog.importxtf
ch.so.agi.datenportal.catalog.store
ch.so.agi.datenportal.catalog.service
ch.so.agi.datenportal.search
ch.so.agi.datenportal.ui.controller
ch.so.agi.datenportal.ui.viewmodel
ch.so.agi.datenportal.ui.component
ch.so.agi.datenportal.admin.reload
ch.so.agi.datenportal.config
```

Keep domain classes independent from Spring where practical.

## Implementation rules

- Keep changes small and reviewable.
- Do not mix parser, search, UI and security changes in one large step unless a phase explicitly requires it.
- Add tests for every behavior change.
- Update documentation when behavior, configuration or architecture changes.
- Make invalid configuration fail clearly.
- Avoid global mutable state except for a controlled atomic snapshot/index holder.
- Use constructor injection for Spring components.
- Prefer records/value objects for immutable view/domain data.

## Runtime snapshot

The published catalog should be represented as a single snapshot object:

- created from a complete XTF/XML import
- validated before activation
- immutable after creation
- atomically replaceable on reload
- used as the source for UI and search indexing

Do not update individual datasets in place during reload.

## Done checklist

Before reporting completion:

- run relevant tests
- run `./gradlew clean check` if available
- update docs
- summarize changed files
- mention limitations and follow-up work
- use `commit-after-dod` if the task/phase should be committed
