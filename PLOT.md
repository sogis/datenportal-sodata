# R-Labor Plot-Ueberlegungen

Die aktuellen Plot-Rezepte sind gute Einstiege, bleiben aber bewusst heuristisch. Sie waehlen aus dem uebernommenen SQL-Resultat je eine plausible Messwert-, Kategorie- und Datumsspalte und erzeugen daraus Histogramm, Boxplot oder Trend. Das ist schnell, aber nicht immer die Frage, die eine Benutzerin gerade beantworten will.

Ein besserer naechster Schritt waere ein kleiner, codegenerierender Plot-Builder im R-Labor. Die Benutzerin weiss oft bereits: Ich will ein Histogramm, einen Boxplot, eine Zeitreihe oder ein Balkendiagramm. Nach der SQL-Ausfuehrung sollte sie deshalb den Plottyp waehlen koennen und danach nur noch kompatible Spalten aus dem aktuellen Dataframe sehen.

Empfohlener V1-Ansatz:

- Plottyp waehlen: `Histogramm`, `Boxplot`, `Trend/Linie`, `Balken`.
- Spalten danach filtern: Histogramm braucht eine numerische Spalte; Boxplot braucht numerisch plus Kategorie; Trend braucht Datum/Jahr plus numerisch; Balken braucht Kategorie plus Kennzahl oder eine Aggregation.
- Optional Aggregation waehlen: `Anzahl`, `Mittelwert`, `Summe`, `Minimum`, `Maximum`.
- Optional Kategorien begrenzen: Top-N fuer Boxplots und Balken, damit lange Textspalten nicht unlesbar werden.
- Rohdatenplots standardmaessig auf `10'000` Zeilen begrenzen.

Wichtig ist: Der Builder sollte keinen versteckten Chart-Zustand pflegen. Er sollte lesbaren R-Code in den Editor schreiben oder aktualisieren. Danach kann die Benutzerin den Code ausfuehren, editieren, kopieren und exportieren wie jeden anderen R-Code. So bleibt das Labor reproduzierbar und erklaerbar.

Die Metadaten dafuer liegen bereits vor: `SqlResultSnapshot` und `daten_schema` enthalten Spaltennamen, R-Typen, DuckDB-Typen, Rollen und Nullable-Info. Der Builder kann damit inkompatible Kombinationen deaktivieren, ohne Serverlogik oder neue Backend-Endpunkte zu brauchen.

UI-seitig passt der Ansatz am besten neben die bestehende Rezeptauswahl: Entweder als Eintrag `Plot erstellen...`, der kompakte Controls einblendet, oder als eigener kleiner Plotmodus in der R-Toolbar. Die bestehenden Rezepte bleiben als schnelle Vorschlaege erhalten; der Builder waere der Weg, wenn die automatische Auswahl nicht zur fachlichen Frage passt.

Nicht fuer V1 empfehlenswert sind ein voll visueller Drag-and-Drop-Chart-Builder, persistente Chart-Konfigurationen, verdeckte ggplot-Objektmanipulation oder KI-generierter Plotcode. Das waere maechtiger, aber deutlich schwerer zu testen und weniger transparent.

Akzeptanzkriterien fuer eine erste Umsetzung:

- Der erzeugte R-Code ist deterministisch, kurz und gut lesbar.
- Spaltennamen im UI verwenden Schweizer Anfuehrungszeichen.
- Unpassende Spaltenkombinationen sind nicht auswaehlbar.
- Histogramm und Boxplot nutzen `plot_limit <- 10000`.
- Der erzeugte Plot funktioniert mit dem aktuellen `daten`-Dataframe ohne Backend-Aenderung.
