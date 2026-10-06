# Beschreibende Metadaten erfassen

Beschreibende Metadaten unterstützen einen eingeschränkten Markdown-Umfang.
Damit lassen sich technische Begriffe und Hinweise lesbar erfassen, ohne HTML
zu schreiben. Die PublishedCatalog-Felder bleiben `TEXT` beziehungsweise
`MTEXT`; ein zusätzliches Formatfeld ist nicht erforderlich.

## Felder

Die folgenden Felder verwenden das Profil:

- `Catalog.description`: Einführung auf der Katalogseite; fehlt sie, erscheint der bisherige Einführungstext.
- `Dataset.description`, `DatasetSeries.description` und `DatasetIssue.description`.
- `DatasetAttribute.description`.
- Die Freitexte `surveyMethod`, `auxiliaryData` und `furtherUses`, soweit sie in der Lieferung enthalten sind.

Titel, Identifier, Attributnamen, Datentypen, Einheiten, Schlagworte,
Modellnamen, Kontaktangaben, URLs und `dataAvailableFrom` bleiben normale
Textwerte. Auch ein Wort wie `**Titel**` wird dort wörtlich angezeigt.

## Schreibweise

| Zweck | Erfassung | Darstellung |
| --- | --- | --- |
| Attribut oder technischer Wert | `` `gemeinde_nr` `` oder `` `2401` `` | In einer Monospace-Schrift hervorgehoben |
| Fett | `**Wichtig**` | **Wichtig** |
| Kursiv | `*Hinweis*` | *Hinweis* |
| Zeilenumbruch | Eine tatsächlich erfasste neue Zeile | Sichtbarer Umbruch auf Detailseiten und in der Katalogeinführung |
| Neuer Absatz | Eine Leerzeile | Getrennter Absatz |
| Aufzählung | `- Eintrag` am Zeilenanfang | Unnummerierte Liste |
| Nummerierte Liste | `1. Eintrag` am Zeilenanfang | Nummerierte Liste |
| Wörtliche Markdown-Zeichen | `\*wörtlich\*` | Sterne bleiben sichtbar |

Die Inline-Syntax folgt [CommonMark](https://spec.commonmark.org/0.31.2/),
einschliesslich verschachtelter Hervorhebungen, Unterstrichen als alternativen
Hervorhebungsmarkern und Backslash-Maskierung. Unterstriche innerhalb von
Identifiern wie `gemeinde_nr` erzeugen keine Hervorhebung. Listen können
verschachtelt werden; nummerierte Listen behalten ihre Startnummer.

Ein Umbruch benötigt keine zwei Leerzeichen am Zeilenende. LF, CRLF und CR
werden gleich behandelt. Die Zeichenfolge `\n` ersetzt keine tatsächlich
erfasste neue Zeile. Mehrzeilige Inhalte gehören in `MTEXT`-Felder.

Beispiel einer Beschreibung:

```text
Das Attribut `gemeinde_nr` enthält die Gemeindenummer.
Der Wert `2401` bezeichnet eine bestimmte Gemeinde.

**Hinweise zur Verwendung:**
- Führende Nullen beibehalten.
- *Fehlende Werte* gesondert behandeln.
```

Backticks kennzeichnen technische Begriffe unabhängig davon, ob es sich um
Attribute oder Werte handelt. Sie erzeugen keine Verknüpfung mit dem
Attributverzeichnis und keine fachliche Referenzprüfung.

HTML und weitere Markdown-Funktionen wie Überschriften, Links, Bilder,
Blockzitate, Trennlinien und Codeblöcke werden als sichtbarer Text behandelt.
Auch deren Marker und Linkziele bleiben sichtbar. Unvollständige
Auszeichnungen erzeugen keinen Importfehler. HTML wird beim Rendern maskiert
und niemals als erfasste Auszeichnung ausgeführt.

## Darstellung und Suche

Detailseiten und die Katalogeinführung zeigen Absätze, Listen und Umbrüche.
In Liste, Karten und aufgeklappten Ausgabezeilen bleiben technische Begriffe,
fett und kursiv erhalten. Absatz-, Listen- und Zeilengrenzen werden dort zu
Leerzeichen; sie erzeugen keine zusätzlichen Blöcke.

Karten zeigen höchstens 50 vollständige Wörter. Die Kürzung erfolgt auf dem
sichtbaren Text und schliesst alle Auszeichnungen vor `...` und dem Link
`[Details anzeigen]`. Zusammengesetzte Wörter und technische Namen mit
Unterstrichen werden als ein Wort gezählt.

Lucene indexiert die Klartextdarstellung der Beschreibungen. Beschreibungen
aktueller und historischer Ausgaben machen weiterhin ihre Datenreihe
auffindbar. Explore erhält Beschreibungen als Klartext, damit seine
Textansichten keine Markdown-Marker anzeigen.

Bestehende Texte sollten bei der Erfassung auf wörtlich gemeinte Sterne,
Backticks und andere Marker geprüft und bei Bedarf maskiert werden. Das
Profil wird einheitlich auf die genannten Felder angewendet; es gibt keine
automatische Erkennung eines anderen Textformats.

## Verarbeitung

`MetadataTextParser` verwendet `org.commonmark:commonmark:0.30.0` und übernimmt
den Syntaxbaum in eine unveränderliche `MetadataDocument`-Struktur. Unterstützte
Elemente werden als Absätze, Listen, Text, Code, Hervorhebungen und Umbrüche
repräsentiert. Nicht unterstützte Konstrukte werden anhand ihrer Quellpositionen
als wörtlicher Text übernommen.

`MetadataTextRenderer` erzeugt vollständiges HTML, kompaktes HTML,
Wortvorschauen und Klartext. Nur er kann den JTE-Inhaltstyp
`MetadataTextRenderer.Html` erzeugen. Er maskiert Text und schreibt ausschliesslich
`p`, `br`, `strong`, `em`, `code`, `ul`, `ol` und `li`; die Startnummer einer
nummerierten Liste stammt aus dem Parser. ViewModels enthalten vorbereitete
Inhalte, während das Domain-Modell die importierten Texte behält.

Ein späterer PDF-Renderer kann dieselbe Dokumentstruktur verwenden, ohne
HTML in den Metadaten zu benötigen. Die PDF-Erzeugung ist noch nicht umgesetzt.
