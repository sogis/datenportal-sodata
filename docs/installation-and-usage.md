# Installation in the target repository

Assume this ZIP was downloaded to `/tmp/datenportal_webapp_spec_v6_full_agent_context.zip` and the target repository is your Datenportal repo.

## Variant A: copy into an empty or new repository

```bash
cd /path/to/datenportal-repo
unzip /tmp/datenportal_webapp_spec_v6_full_agent_context.zip -d /tmp/datenportal-agent-package
cp -R /tmp/datenportal-agent-package/datenportal_webapp_spec_v6_full_agent_context/. .
python3 tools/validate-agent-package.py
```

This copies the full specification package into the repository root.

## Variant B: merge into an existing repository carefully

```bash
cd /path/to/datenportal-repo
unzip /tmp/datenportal_webapp_spec_v6_full_agent_context.zip -d /tmp/datenportal-agent-package

# Inspect first
find /tmp/datenportal-agent-package/datenportal_webapp_spec_v6_full_agent_context -maxdepth 3 -type f | sort

# Copy agent instructions and skills
cp /tmp/datenportal-agent-package/datenportal_webapp_spec_v6_full_agent_context/AGENTS.md ./AGENTS.md
mkdir -p .agents .opencode docs spec/mockups/current tools
cp -R /tmp/datenportal-agent-package/datenportal_webapp_spec_v6_full_agent_context/.agents/skills .agents/
cp -R /tmp/datenportal-agent-package/datenportal_webapp_spec_v6_full_agent_context/.opencode/commands .opencode/

# Copy specification and docs
cp /tmp/datenportal-agent-package/datenportal_webapp_spec_v6_full_agent_context/datenportal_webapp_agent_spec_detailed_v5.md ./
cp -R /tmp/datenportal-agent-package/datenportal_webapp_spec_v6_full_agent_context/docs/. docs/

# Copy current mockup references
cp -R /tmp/datenportal-agent-package/datenportal_webapp_spec_v6_full_agent_context/spec/mockups/current/. spec/mockups/current/

# Copy validation helper
cp /tmp/datenportal-agent-package/datenportal_webapp_spec_v6_full_agent_context/tools/validate-agent-package.py tools/
python3 tools/validate-agent-package.py
```

## Variant C: compare before overwriting AGENTS.md

If your repository already has an `AGENTS.md`, compare first:

```bash
cd /path/to/datenportal-repo
unzip /tmp/datenportal_webapp_spec_v6_full_agent_context.zip -d /tmp/datenportal-agent-package
diff -u AGENTS.md /tmp/datenportal-agent-package/datenportal_webapp_spec_v6_full_agent_context/AGENTS.md || true
```

Then merge manually or replace the file deliberately.

## Codex usage

Codex should read `AGENTS.md` automatically when it is in the repository root. The repo-local skills are under `.agents/skills/`.

## Lokale Laufzeit ab Phase 2

Der Katalog wird beim Start aus einer konfigurierten PublishedCatalog-XTF-Datei geladen.

Standardkonfiguration in `src/main/resources/application.yml`:

```yaml
datenportal:
  catalog:
    source: classpath:published_catalog_full_54_entries.xtf
```

Die Datei `spec/fixtures/published_catalog_full_54_entries.xtf` ist dafür als zusätzliche Main-Resource auf dem Classpath eingebunden. Damit startet die Anwendung und auch `@SpringBootTest` standardmässig mit der Full Fixture.

Unterstützte Quellen in Phase 2:

- `classpath:published_catalog_full_54_entries.xtf`
- `file:./pfad/zum/catalog.xtf`

Beispiel für einen lokalen Dateipfad:

```bash
./gradlew bootRun --args='--datenportal.catalog.source=file:./tmp/catalog.xtf'
```

Wichtige Eigenschaften des Startup-Loadings:

- fail-fast bei fehlender Quelle, ungültigem XML oder Validierungsfehlern
- namespace-aware PublishedCatalog-Parser
- XXE/DTD deaktiviert
- Read-Model bleibt das bestehende immutable Domain-Modell aus Phase 1

Für die Verifikation der Importphase sind mindestens vorgesehen:

```bash
./gradlew test
./gradlew check
```

Useful first prompt:

```text
Read AGENTS.md, datenportal_webapp_agent_spec_detailed_v5.md, docs/ui-implementation-contract.md and the relevant skills. Then plan Phase 1 without editing files.
```

## OpenCode usage

OpenCode can also use the repo files directly. This package additionally contains project-local commands under `.opencode/commands/`.

Useful prompts:

```text
/plan-phase Implement the catalog startup skeleton and package structure.
```

```text
/implement-ui-contract Implement the list-view start page according to the UI contract.
```

```text
/dod-commit Commit the completed phase after running the DoD checks.
```
