# Container-Deployment

Diese Anleitung beschreibt den Containerbetrieb der Datenportal-Webanwendung.
Das Standardimage `datenportal-sodata` enthält ein GraalVM Native Image. Das
JVM-Fallback wird separat als `datenportal-sodata-jvm` veröffentlicht. Beide
Images verwenden Port `8080`, dieselben Umgebungsvariablen und denselben
Liveness-Healthcheck.

Der Dockerfile und seine Buildlogik liegen bewusst in diesem Repository. Der
Dev-Stack `datenportal-dev-stack` baut und verwendet dieses Image, definiert
aber keine eigene Kopie der Buildlogik.

## Betriebsmodell

- Spring-Boot-Anwendung, nativ kompiliert oder auf einer JVM ausgeführt,
  Container-Port `8080`
- non-root-Benutzer, kein TLS im Container
- Katalog- und DuckDB-Quelle werden beim Start und bei jedem Reload gelesen;
  ohne gültige Quelle startet die Anwendung nicht
- ein Manifest mit `catalog: null` ist eine gültige, leere Erstlieferung;
  ein fehlendes oder ungültiges `current.json` bleibt ein Fehler
- der Container-Healthcheck prüft `/actuator/health/liveness`; die öffentliche
  Antwort zeigt nur den Gesamtstatus
- Im Dev-Stack werden XTF und DuckDB gemeinsam aus dem Manifest geladen.
  GRETL erzeugt und prüft die DuckDB. `spec/fixtures` bleibt ausschliesslich
  für explizite Classpath-Konfigurationen und Tests verfügbar.

## Voraussetzungen

- Docker mit BuildKit (für die Cache-Mounts des Builds)
- Netzwerkzugriff während des Builds: Maven Central, npm-Registry und der
  gespiegelte WebR-Paketindex
- Registry-Zugriff, wenn statt des lokalen Builds ein veröffentlichtes Image
  verwendet wird

Der Docker-Build läuft vollständig in BuildKit-Stages. Auf dem Host werden für
den Containerweg weder JDK noch Node installiert. Für `bootRun` genügt JDK 25;
für den lokalen Native-Compile braucht es GraalVM 25 mit Native Image und Node 22
im `PATH`.

## Image bauen

```bash
docker build -t datenportal-sodata:local .
docker build --target jvm-runtime -t datenportal-sodata-jvm:local .
```

Der erste Befehl baut das Native-Image und ist der Standard-Target des
Dockerfiles. Der zweite Befehl baut das JVM-Fallback.

Für einen Native-Compile direkt auf dem Entwicklungsrechner kann die installierte
GraalVM über SDKMAN aktiviert werden:

```bash
sdk use java 25.0.3-graal
./gradlew nativeCompile
```

Das erzeugte Programm ist für Betriebssystem und Prozessorarchitektur des
Build-Rechners bestimmt. Der Docker-Build verwendet Linux-Builder und erzeugt
das für den Container passende Programm.

Buildargumente:

| Argument | Default | Wirkung |
|---|---|---|
| `TEMURIN_JDK_IMAGE` | `eclipse-temurin:25-jdk` | JVM-Build-Stage mit Gradle und Node |
| `JVM_RUNTIME_IMAGE` | `registry.access.redhat.com/ubi9/openjdk-25-runtime` | Red Hat UBI Laufzeitbasis für das JVM-Image |
| `GRAALVM_NATIVE_IMAGE` | `ghcr.io/graalvm/native-image-community:25-ol9` | GraalVM Native-Image-Builder auf Oracle Linux 9 |
| `NATIVE_RUNTIME_IMAGE` | `registry.access.redhat.com/ubi9/ubi-minimal:latest` | Red Hat UBI 9 Laufzeitbasis für das Native-Image |
| `NODE_IMAGE` | `node:22-bookworm-slim` | Quelle für Node 22 im Build-Stage |
| `IMAGE_VERSION` | `local` | Label `org.opencontainers.image.version` |
| `GIT_COMMIT` | `unknown` | Label `org.opencontainers.image.revision` und Commit in `build-info.properties` |

Mit Commit-Kennung:

```bash
docker build \
  --build-arg IMAGE_VERSION=0.1.0 \
  --build-arg GIT_COMMIT="$(git rev-parse --short=12 HEAD)" \
  -t datenportal-sodata:0.1.0 \
  .
```

Der erste Build lädt Gradle-, npm- und WebR-Abhängigkeiten und dauert deshalb
länger. BuildKit-Cache-Mounts halten Gradle- und npm-Cache zwischen Builds
erhalten; ein erneuter Build nach einer Quelländerung nutzt sie.

## Veröffentlichte Images

Der Workflow `.github/workflows/container-image.yml` baut beide Images für
`linux/amd64` und `linux/arm64`. Die Builds laufen auf passenden nativen
GitHub-Runnern; beide Varianten bestehen ihre Container-Smoke-Checks, bevor
der Workflow die gemeinsamen Versions- und `latest`-Tags als Multiarch-Manifeste
veröffentlicht. Docker wählt beim Pull automatisch die passende Architektur.
Pull Requests bauen und prüfen beide Architekturen, publizieren aber keine Images.

Die kanonischen Tags bleiben:

```text
docker.io/sogis/datenportal-sodata:0.1.<github-run-number>
docker.io/sogis/datenportal-sodata:latest
docker.io/sogis/datenportal-sodata-jvm:0.1.<github-run-number>
docker.io/sogis/datenportal-sodata-jvm:latest
ghcr.io/sogis/datenportal-sodata:0.1.<github-run-number>
ghcr.io/sogis/datenportal-sodata:latest
ghcr.io/sogis/datenportal-sodata-jvm:0.1.<github-run-number>
ghcr.io/sogis/datenportal-sodata-jvm:latest
```

Zusätzlich bleiben die geprüften Architektur-Images unter Versions-Tags mit
Suffix verfügbar, zum Beispiel:

```text
docker.io/sogis/datenportal-sodata:0.1.<github-run-number>-amd64
docker.io/sogis/datenportal-sodata:0.1.<github-run-number>-arm64
```

Für jedes Image in beiden Registries wird zuerst das Versions-Manifest aus
diesen beiden Varianten erstellt. `latest` wird erst aktualisiert, nachdem alle
Versions-Manifeste erfolgreich veröffentlicht wurden. Die Manifest-Inhalte
lassen sich mit folgendem Befehl prüfen:

```bash
docker buildx imagetools inspect docker.io/sogis/datenportal-sodata:latest
```

Die Ausgabe muss sowohl `linux/amd64` als auch `linux/arm64` aufführen.

Der Präfix `0.1` ist als Workflow-Variable manuell gepflegt. `latest` zeigt je
Repository auf den zuletzt erfolgreich für beide Architekturen geprüften Push
auf `main`; produktive Deployments können weiterhin auf einen konkreten
Versionstag zeigen.

Für den Push nach Docker Hub müssen im GitHub-Repository die Secrets
`DOCKERHUB_USERNAME` und `DOCKERHUB_TOKEN` hinterlegt sein. Der Token muss
Schreibrechte auf `sogis/datenportal-sodata` und
`sogis/datenportal-sodata-jvm` besitzen. Für GHCR verwendet der
Workflow den automatisch bereitgestellten `GITHUB_TOKEN` mit
`packages: write`. Beide Docker-Hub-Repositories müssen vor dem ersten Lauf
angelegt sein. Nach dem ersten erfolgreichen GHCR-Push müssen die neu
angelegten Packages in den GitHub-Package-Einstellungen auf öffentlich gestellt
werden, sofern die Organisationsvorgaben dies nicht bereits automatisch tun.

## OpenShift und beliebige UIDs

Die Runtime-Stages laufen nicht als root: Das JVM-Image verwendet UID `185`,
das Native-Image UID `1001`. Die Dateien unter `/opt/datenportal` sind zusätzlich
über Gruppe `0` lesbar, sodass OpenShift beide Images mit einer zufällig
zugewiesenen Nicht-root-UID starten kann. Der Workflow prüft dieses Verhalten
mit einer simulierten UID vor dem Registry-Push.

Für ein OpenShift-Deployment können die Standard-Sicherheitsvorgaben explizit
beibehalten werden:

```yaml
securityContext:
  runAsNonRoot: true
  allowPrivilegeEscalation: false
  capabilities:
    drop: [ALL]
  seccompProfile:
    type: RuntimeDefault
```

Als HTTP-Probes eignen sich `/actuator/health/liveness` auf Port `8080` für
Liveness und `/actuator/health/readiness` auf Port `8080` für Readiness.

Der lokale Build und der Dev-Stack bleiben unverändert:

```bash
docker build -t datenportal-sodata:local .
```

## Container-Regressionstest für Native und JVM

Der gemeinsame Test benötigt Python 3 und Docker auf dem Host. Er startet das
fertig gebaute Runtime-Image mit UID `1001230000:0`, einem eindeutigen Namen und
einem dynamischen Port auf `127.0.0.1`. Er verwendet ausschliesslich gebündelte
Classpath-Fixtures und entfernt seinen Container auch bei einem Fehler.
Es sind weder ein Dev-Stack noch S3 oder Schwester-Repositories erforderlich.

```bash
docker build --target native-runtime -t datenportal-sodata:native-test .
python3 tools/test-container.py --image datenportal-sodata:native-test --runtime native

docker build --target jvm-runtime -t datenportal-sodata-jvm:jvm-test .
python3 tools/test-container.py --image datenportal-sodata-jvm:jvm-test --runtime jvm
```

`--runtime native` verlangt den Entrypoint `/opt/datenportal/app`; der Test
führt damit das kompilierte native Binary im Runtime-Container aus.
Die Prüfungen umfassen Liveness, tatsächliche UID, Startseite, DuckDB-Datei und
Explore-HTML sowie JSON-Kontext für `ch.so.bauinventar`, die aktuelle Ausgabe
von `ch.so.abstimmungsresultate` und deren Ausgabe 2025. Der eingebettete Kontext
muss mit dem JSON-Endpunkt übereinstimmen. Geprüft werden insbesondere Tabellen,
Spalten, Rezepte, optionale Chart-Konfigurationen, Enum-Werte und das R-Labor.
Unbekannte Datensätze müssen weiterhin HTTP 404 liefern. Fehler melden den
betroffenen Aufruf und Containerlogs; der Prozess endet mit Exitcode 1.

Der Workflow führt denselben Test vor der Veröffentlichung für Native und JVM
auf den vorhandenen AMD64- und ARM64-Runnern aus. Ein fehlgeschlagener Test
blockiert den nachfolgenden Image-Push dieser Architektur und die gemeinsamen
Multiarch-Tags. Der Test ergänzt `./gradlew clean check`: Browserseitige SQL-
und R-Ausführung bleiben Aufgabe der bestehenden Frontend-/Playwright-Tests.

### Native-Reflection für den Explore-Kontext

Explore erzeugt JSON manuell mit Jackson und gibt es als String aus. Spring MVC
kann deshalb den DTO-Typ nicht aus dem Rückgabetyp der Endpunkte ableiten.
`CatalogResourceRuntimeHints` registriert den gesamten Typbaum ab
`ExploreContextDto` mit `BindingReflectionHintsRegistrar`, einschliesslich
Record-Accessoren und generischer `List`-/`Optional`-Elemente. Die drei
Explore-Enums registrieren zusätzlich ihre `@JsonValue`-Methoden, damit die
bestehenden kleingeschriebenen JSON-Werte erhalten bleiben.

Das veröffentlichte native Image `0.1.10` enthält diese Hints noch nicht und
liefert bei Explore HTTP 500 (`UnsupportedFeatureError` für
`ExploreContextDto`). Als einmalige Negativkontrolle muss der Test deshalb
an diesem Image scheitern:

```bash
docker pull sogis/datenportal-sodata:0.1.10
python3 tools/test-container.py --image sogis/datenportal-sodata:0.1.10 --runtime native
```

Erwartet wird ein Fehler beim Explore-Aufruf, kein Start- oder Registryfehler.
Diese historische Negativkontrolle ist kein regulärer CI-Schritt.

## Lokaler Schnelltest mit Fixtures

Für einen Smoke-Test ohne externe Quellen werden die gebündelten Fixtures
explizit per Umgebungsvariablen gewählt:

```bash
docker run --rm -p 18082:8080 \
  -e DATENPORTAL_CATALOG_SOURCE_TYPE=classpath \
  -e DATENPORTAL_CATALOG_CLASSPATH_LOCATION=published_catalog_full_62_entries.xtf \
  -e DATENPORTAL_CATALOG_DOWNLOAD_URL=http://localhost:8081/ch.so.datenportal/downloads \
  -e DATENPORTAL_CATALOG_DUCKDB_SOURCE_TYPE=classpath \
  -e DATENPORTAL_CATALOG_DUCKDB_CLASSPATH_LOCATION=catalog.duckdb \
  datenportal-sodata:local
```

Erwartete Prüfungen:

```bash
curl -fS http://127.0.0.1:18082/actuator/health
curl -fS http://127.0.0.1:18082/ >/dev/null
curl -fSI http://127.0.0.1:18082/catalog/catalog.duckdb >/dev/null
```

Das `SPRING_PROFILES_ACTIVE=local`-Profil eignet sich nur für den
Host-`bootRun`: Es aktiviert den JTE-Development-Mode, der die Template-Quellen
aus dem Checkout erwartet. Im Container werden die vorkompilierten Templates
aus dem Jar verwendet. Die Downloadbasis im Beispiel ist die feste lokale
Fixture-Basis und nicht für den Stackbetrieb gedacht.

## Laufzeitkonfiguration

Die Anwendung liest dieselben Properties wie beim Host-Start; im Container
werden sie als Umgebungsvariablen übergeben (Spring-Boot-Relaxed-Binding):

| Variable | Bedeutung |
|---|---|
| `SPRING_PROFILES_ACTIVE` | nur für Host-`bootRun`; im Container nicht verwenden (JTE-Development-Mode erwartet Template-Quellen aus dem Checkout) |
| `DATENPORTAL_ADMIN_RELOAD_TOKEN` | Token für `/admin/catalog/reload` und `/admin/catalog/status`; ohne Wert antworten die Endpunkte `503` |
| `DATENPORTAL_CATALOG_SOURCE_TYPE` | `manifest` im Stackbetrieb; alternativ `classpath`, `http`, `file` |
| `DATENPORTAL_CATALOG_HTTP_URL` | Manifestadresse, im Compose-Netz z. B. `http://downloads:8081/ch.so.daten/current.json` |
| `DATENPORTAL_CATALOG_DOWNLOAD_URL` | öffentlich erreichbare Downloadbasis für den Browser, z. B. `http://localhost:8081/ch.so.daten` |
| `DATENPORTAL_CATALOG_DUCKDB_SOURCE_TYPE` | `manifest` (gemeinsam mit XTF), `classpath` (Fixtures), `file` oder `http` |
| `DATENPORTAL_CATALOG_DUCKDB_CLASSPATH_LOCATION` | `catalog.duckdb` für die gebündelte Fixture |
| `SERVER_PORT` | optional; Default im Container ist `8080` |

Beispiel mit Manifestquelle:

```bash
docker run --rm -p 18082:8080 \
  -e DATENPORTAL_ADMIN_RELOAD_TOKEN=change-me \
  -e DATENPORTAL_CATALOG_SOURCE_TYPE=manifest \
  -e DATENPORTAL_CATALOG_HTTP_URL=http://host.docker.internal:8081/ch.so.daten/current.json \
  -e DATENPORTAL_CATALOG_DOWNLOAD_URL=http://localhost:8081/ch.so.daten \
  -e DATENPORTAL_CATALOG_DUCKDB_SOURCE_TYPE=manifest \
  datenportal-sodata:local
```

## Betrieb im Dev-Stack

Der Dev-Stack startet die Anwendung als Compose-Service `sodata`:

- Host-Port `8082` auf Container-Port `8080`
- Manifest intern über `http://downloads:8081/...`, öffentliche Downloadbasis
  über den Host-Port
- Reload-Token kommt aus derselben lokalen `.env` wie der Jenkins-Aufruf
- vor der ersten Veröffentlichung startet der Dev-Stack den Sodata-Container
  nicht, wenn `current.json` mit HTTP 404 fehlt; die Erstpublikation muss zuerst
  über den dokumentierten Jenkins-/Bootstrap-Ablauf erfolgen
- nach der Erstpublikation wird Sodata mit denselben Compose-Overrides explizit
  gestartet und auf seinen Healthcheck geprüft

Details, Startoptionen und die Reihenfolge nach der Erstpublikation stehen in
der Stack-Dokumentation
(`datenportal-dev-stack/README.md` und `docs/biblios/inbetriebnahme.adoc`).

## Health, Probes und Neustart

Das Image und OpenShift verwenden getrennte Zustände:

- `/actuator/health/liveness` prüft ausschließlich, ob der Prozess intern lebt.
  Dieser Check darf nicht von Garage, `current.json` oder anderen externen
  Diensten abhängen.
- `/actuator/health/readiness` prüft den aktiven Snapshot und den Suchindex.
  Ein gültiges Manifest mit `catalog: null` ist dabei bereit und liefert eine
  leere öffentliche Sicht.
- `/actuator/health` bleibt der allgemeine öffentliche Gesamtstatus.

Die Anwendung erzeugt kein `current.json`. Vor einem OpenShift-Deployment muss
ein vorgelagerter Bootstrap-Job oder die Deployment-Pipeline ein gültiges
Manifest bereitstellen. Dadurch wird ein erwarteter Erstzustand nicht als
Container-Crash behandelt. Ein fehlendes oder beschädigtes Manifest bleibt
hingegen ein echter Konfigurations-/Datenfehler.

Für OpenShift sind die Probes sinngemäss so zu verdrahten:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
startupProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
```

Die konkreten Deployment-, Secret-, Registry- und Zeitparameter gehören in
das zuständige OpenShift-/Inbetriebnahme-Repository. Dieses Repository liefert
keine konkreten OpenShift-Manifeste.

Nach der administrativen Initialpublikation kann ein laufender lokaler
Container so geprüft werden:

```bash
curl -fS http://localhost:8082/actuator/health/liveness
curl -fS http://localhost:8082/actuator/health/readiness
```

## Native Image und JVM-Fallback

Der Native-Compile wird über das GraalVM Native Build Tools Gradle-Plugin
ausgeführt. Das Spring-Boot-Gradle-Plugin bindet dafür die AOT-Verarbeitung ein.
Der Native- und der JVM-Container verwenden getrennte Build- und Runtime-Stages;
beide behalten dieselbe Anwendungskonfiguration und den Port `8080`.

Die Native-Hinweise registrieren die gebündelten XTF-/DuckDB-Ressourcen sowie
die generierten JTE-Templates für Reflection. JTE lädt diese Klassen zur
Laufzeit dynamisch und ruft deren `renderMap`-Methode reflektiv auf. Der
Hint-Registrar leitet die Templateklassen aus den generierten `.class`-Dateien
ab, damit neu hinzukommende Templates ebenfalls im Native-Image funktionieren.

## Fehlerdiagnose

| Symptom | Prüfung |
|---|---|
| Container startet wiederholt neu | Manifestadresse und `current.json` prüfen; Logs mit `docker compose logs sodata` bzw. `docker logs <container>` |
| Healthcheck bleibt `unhealthy`, Anwendung läuft | Startzeit verkürzen/`start-period` prüfen; `/actuator/health/liveness` direkt mit `curl` testen |
| Port belegt | Host-Port `8082` frei machen oder `SODATA_PORT` ändern |
| Reload `503` | `DATENPORTAL_ADMIN_RELOAD_TOKEN` im Container leer |
| Reload `401` | Token stimmt nicht mit Jenkins `DATENPORTAL_PORTAL_RELOAD_TOKEN` überein |
| Erkunden zeigt falsche/fehlende Tabellen | Manifestgeneration, Reload und View-Erzeugung im GRETL-Job prüfen; offene Playgrounds neu laden |
