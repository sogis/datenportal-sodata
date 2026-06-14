#!/usr/bin/env python3
from pathlib import Path
import sys

root = Path.cwd()
required = [
    'AGENTS.md',
    'README.md',
    'datenportal_webapp_agent_spec_detailed_v5.md',
    'docs/ui-implementation-contract.md',
    'docs/component-map.md',
    'docs/llm-coding-agent-usage.md',
    'docs/skills-overview.md',
    'docs/installation-and-usage.md',
    'spec/mockups/current/startseite_liste.png',
    'spec/mockups/current/cards.png',
    'spec/mockups/current/web-components.png',
    '.opencode/commands/plan-phase.md',
    '.opencode/commands/implement-phase.md',
    '.opencode/commands/implement-ui-contract.md',
    '.opencode/commands/dod-commit.md',
    '.opencode/commands/review-diff.md',
]
skills = [
    'datenportal-webapp',
    'phase-delivery',
    'xtf-publishedcatalog',
    'jte-htmx-ui',
    'datenportal-ui-contract',
    'lucene-search',
    'spring-boot-reload-security',
    'commit-after-dod',
]
required.extend([f'.agents/skills/{skill}/SKILL.md' for skill in skills])
missing = [p for p in required if not (root / p).exists()]
if missing:
    print('Missing required files:')
    for p in missing:
        print(f'  - {p}')
    sys.exit(1)

agents = (root / 'AGENTS.md').read_text(encoding='utf-8')
for skill in skills:
    if skill not in agents:
        print(f'AGENTS.md does not mention skill: {skill}')
        sys.exit(1)

spec = (root / 'datenportal_webapp_agent_spec_detailed_v5.md').read_text(encoding='utf-8')
for token in ['ch.so.agi.datenportal', 'UI', 'Phase', 'Lucene', 'PublishedCatalog']:
    if token not in spec:
        print(f'Spec seems incomplete; missing token: {token}')
        sys.exit(1)

ui = (root / 'docs/ui-implementation-contract.md').read_text(encoding='utf-8')
for token in ['startseite_liste.png', 'cards.png', 'web-components.png', 'Datenreihe']:
    if token not in ui:
        print(f'UI contract seems incomplete; missing token: {token}')
        sys.exit(1)

print('OK: Datenportal agent/spec package looks complete.')
