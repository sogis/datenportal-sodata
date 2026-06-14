#!/usr/bin/env python3
from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
required = [
    "README.md",
    "datenportal_webapp_agent_spec_detailed_v5.md",
    "docs/ui-implementation-contract.md",
    "docs/component-map.md",
    "docs/spec-ui-addendum.md",
    "docs/llm-coding-agent-usage.md",
    "docs/v5-changelog.md",
    "spec/mockups/current/startseite_liste.png",
    "spec/mockups/current/cards.png",
    "spec/mockups/current/web-components.png",
    ".agents/skills/datenportal-ui-contract/SKILL.md",
    ".opencode/commands/implement-ui-contract.md",
]
missing = [p for p in required if not (root / p).exists()]
if missing:
    print("Missing files:")
    for p in missing:
        print(" -", p)
    sys.exit(1)

spec = (root / "datenportal_webapp_agent_spec_detailed_v5.md").read_text(encoding="utf-8")
checks = [
    "## 1. Architekturentscheid",
    "## 3. Java-Paketstruktur",
    "## 7. XTF-/XML-Parser",
    "## 9. Suche, Filter, Sortierung und Facetten",
    "## 12. Integration der Web Components",
    "## 14. UI-Implementation-Contract und UI-Verhalten",
    "## 18. Phasenplan mit Klassenfokus",
    "### Phase 0 — Bootstrap",
    "### Phase 8 — Hardening und Doku",
    "### UI-Phasen — verbindliche Ergänzung zum Phasenplan",
]
failed = [c for c in checks if c not in spec]
if failed:
    print("Spec checks failed:")
    for c in failed:
        print(" -", c)
    sys.exit(1)
print("v5 package OK")
