# Suche

Diese Datei beschreibt die fachliche Soll-Semantik der Katalogsuche.
Sie ist bewusst produkt- und nutzungsorientiert formuliert und beschreibt nicht die technische Umsetzung im Detail.

## Zweck

Die Suche soll Nutzerinnen und Nutzern helfen, Datensätze und Datenreihen auch dann zuverlässig zu finden, wenn nur unvollständige oder ungefähre Begriffe bekannt sind.

Die Suche ist damit kein reines Identifier-Lookup und auch keine exakte Phrasensuche, sondern eine tolerante fachliche Einstiegssuche für den Katalog.

## Geltungsbereich

Diese Suchdefinition ist ein fachlicher Soll-Zustand für die Weiterentwicklung der Suche.
Sie beschreibt bewusst nicht in jedem Punkt das aktuelle Ist-Verhalten der Anwendung.

## Fachliche Regeln

1. Die Suche durchsucht garantiert `Identifier`, `Titel` und `Keywords`.
2. Datenreihen müssen zusätzlich über Metadaten ihrer Ausgaben auffindbar sein.
3. Historische und aktuelle Ausgaben einer Datenreihe dürfen die Datenreihe auffindbar machen, erscheinen aber nicht als eigene Top-Level-Treffer in der Resultatliste.
4. Substring-Matching gilt garantiert für `Identifier`, `Titel` und `Keywords`.
5. Der Suchtext wird in einzelne Suchwörter oder Tokens zerlegt.
6. Zwischen Suchwörtern gilt `AND`: Jedes Suchwort muss irgendwo matchen.
7. Zwischen den durchsuchten Feldern gilt `OR`: Ein Suchwort darf in unterschiedlichen Feldern matchen.
8. Filter werden zusätzlich zur Textsuche angewendet.
9. Zwischen Filterkategorien gilt `AND`.
10. Innerhalb einer Filterkategorie gilt `OR`.
11. Suchanfragen mit 1-2 Zeichen gelten fachlich als leer und lösen keine aktive Textsuche aus.
12. Sonderzeichen oder sonstige ungültige Eingaben dürfen nicht zu einer Fehlermeldung oder einem Serverfehler führen; die Suche behandelt solche Eingaben tolerant.
13. Die sichtbare Reihenfolge der Treffer richtet sich nicht nach Relevanz, sondern nach der gewählten UI-Sortierung.
14. Im MVP sind die sichtbaren Sortierungen `Neueste zuerst` und `Titel A-Z`.
15. Ein internes Ranking darf existieren, ist fachlich aber keine eigene Sortieroption und kein sichtbares Bedienkonzept.
16. `Thema`, `Fachstelle / Amt` und `Beschreibung` dürfen aus Kompatibilitätsgründen weiterhin sekundär textsuchbar bleiben.
17. Für diese sekundären Felder ist kein gleich starkes oder gleich stabiles Substring-Verhalten garantiert wie für `Identifier`, `Titel` und `Keywords`.

## Beispiele

- Suche nach `boden`: Treffer sind erlaubt, wenn `boden` als Teilstring in Identifier, Titel oder Keywords vorkommt.
- Suche nach `boden amt`: Treffer sind nur erlaubt, wenn sowohl `boden` als auch `amt` matchen.
- Suche nach `boden amt` kann trotzdem gültig sein, wenn `boden` im Titel und `amt` in den Keywords vorkommt.
- Suche nach `ab` gilt fachlich als leer, weil der Begriff kürzer als drei Zeichen ist.
- Suche mit problematischen Zeichen wie `+++` oder `)(` darf die Suche nicht abbrechen.

## Abgrenzung

Diese fachliche Soll-Definition orientiert sich für Substring-Matching sowie für `AND zwischen Tokens` und `OR zwischen Feldern` an der Suchsemantik von `../sodata-ng`.

Substring wird bewusst nicht als gleich starke Hauptregel auf Beschreibungen, Themen oder Fachstellen ausgedehnt, damit die Suche fachlich präzise und für die Resultatqualität kontrollierbar bleibt.

## Betriebsverhalten bei Fehlern

Die Textsuche liest die vollständige Lucene-Treffermenge, bevor die
fachlichen Java-Filter angewendet werden. Dadurch kann ein passender Treffer
nicht durch eine versteckte Vorabgrenze verloren gehen.

Wenn der Suchindex geschlossen ist oder ein Lucene-Fehler auftritt, wird dies
nicht als „keine Treffer“ behandelt. Die Anwendung zeigt stattdessen eine
verständliche HTTP-503-Fehlerseite; bei HTMX-Anfragen wird mit
`HX-Refresh: true` ein vollständiger Seitenaufbau angefordert.
