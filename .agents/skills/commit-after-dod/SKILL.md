---
name: commit-after-dod
description: Use before creating a Git commit after a completed task or phase; enforces Definition of Done, tests, diff review, explicit staging, and informative commit messages.
compatibility: codex, opencode
metadata:
  project: datenportal
---

# Skill: Commit after Definition of Done

Use this skill whenever an implementation phase, bug fix, refactoring, documentation update, fixture update or UI change is complete and should be committed.

## Preconditions

Before committing, verify that:

1. The requested task or phase is complete.
2. The implementation matches `AGENTS.md`, relevant skills and project documentation.
3. The change is intentionally scoped.
4. No unrelated files are included.
5. No secrets, credentials, local paths, generated caches, IDE files or temporary files are staged.
6. Documentation and tests have been updated where behavior changed.

## Protect user work

Before staging, inspect:

```bash
git status --short
git diff
```

If there are pre-existing unrelated modifications, do not stage or overwrite them. Ask before touching ambiguous files.

## Required verification

Run the strongest available verification command.

Default:

```bash
./gradlew clean check
```

When the change affects startup, parsing, Lucene indexing, reload endpoints or templates, also run a smoke check if available:

```bash
./gradlew bootRun
```

If a command cannot be run, document why. Do not pretend it passed.

## Review the diff

Inspect unstaged and staged diffs:

```bash
git diff
git diff --cached
```

Check especially:

- no unrelated formatting-only changes
- no generated files unless intentional
- no absolute local paths such as `/Users/...`
- no credentials or tokens
- no weakened reload/admin security
- no unnecessary JavaScript
- no broken documentation links

## Stage only relevant files

Use explicit staging:

```bash
git add <file1> <file2>
```

Avoid `git add .` unless the diff is very small and already reviewed.

Then verify:

```bash
git diff --cached
git status --short
```

## Commit message format

Use conventional-style messages:

- `feat:` new user-visible functionality
- `fix:` bug fix
- `test:` tests or fixtures
- `docs:` documentation only
- `refactor:` internal restructuring without behavior change
- `chore:` build/tooling/housekeeping
- `style:` visual-only UI styling

Examples:

```text
feat: add published catalog parser and snapshot store
```

```text
feat: implement catalog list view with multi filters
```

```text
test: add published catalog fixture with dataset series issues
```

For larger phase commits, include a body:

```text
feat: implement Lucene-backed dataset search

- build index from published catalog entries
- support search by title, identifier, theme, office and keywords
- add integration tests for normal datasets and dataset series
- document search fields and ranking assumptions

Verification:
- ./gradlew clean check
```

## Commit

After the staged diff has been reviewed:

```bash
git commit -m "<message>"
```

For multi-line messages, use:

```bash
git commit
```

## Final report

After committing, report:

- commit hash
- commit message
- verification commands run
- skipped checks or known limitations

## Never do this

- Do not commit failing tests.
- Do not commit unrelated files.
- Do not hide skipped verification.
- Do not rewrite history unless explicitly requested.
- Do not use vague messages such as `update`, `changes`, `fix stuff`, `wip`.
- Do not commit secrets or machine-specific configuration.
