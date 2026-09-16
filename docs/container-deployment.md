# Container-Deployment

Diese Anleitung beschreibt den Containerbetrieb der Datenportal-Webanwendung.
Das aktuelle Betriebsmodell ist ein JVM-Image auf Basis von Eclipse Temurin.
Ein GraalVM-Native-Image ist als spätere, getrennte Stage vorgesehen; der
Stack-Vertrag (Port, Umgebungsvariablen, Healthcheck) soll dabei gleich bleiben.

Der Dockerfile und seine Buildlogik liegen bewusst in diesem Repository. Der
Dev-Stack `datenportal-dev-stack` baut und verwendet dieses Image, definiert
aber keine eigene Kopie der Buildlogik.

## Betriebsmodell

- Spring-Boot-JVM-Anwendung, Container-Port `8080`
- non-root-Benutzer, kein TLS im Container
- Katalog- und DuckDB-Quelle werden beim Start und bei jedem Reload gelesen;
  ohne gültige Quelle startet die Anwendung nicht
- ein Manifest mit `catalog: null` ist eine gültige, leere Erstlieferung;
  ein fehlendes oder ungültiges `current.json` bleibt ein Fehler
- der Container-Healthcheck prüft `/actuator/health/liveness`; die öffentliche
  Antwort zeigt nur den Gesamtstatus
- `catalog.duckdb` liegt als Fixture über `spec/fixtures` im Jar und wird mit
  `source-type=classpath` gelesen; Erzeugung und Synchronisierung mit neuen
  Lieferungen sind noch nicht implementiert

## Voraussetzungen

- Docker mit BuildKit (für die Cache-Mounts des Builds)
- Netzwerkzugriff während des Builds: Maven Central, npm-Registry und der
  gespiegelte WebR-Paketindex
- Registry-Zugriff, wenn statt des lokalen Builds ein veröffentlichtes Image
  verwendet wird

Der Build läuft vollständig im Container. Auf dem Host wird für den Containerweg
kein JDK und kein Node installiert; JDK 25 und Node/npm bleiben nur für den
manuellen `bootRun`-Entwicklungsstart nötig.

## Image bauen

```bash
docker build -t datenportal-sodata:local .
```

Buildargumente:

| Argument | Default | Wirkung |
|---|---|---|
| `TEMURIN_JDK_IMAGE` | `eclipse-temurin:25-jdk` | Basis des Build-Stage mit Gradle und Node |
| `TEMURIN_JRE_IMAGE` | `eclipse-temurin:25-jre` | Runtime-Basis |
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
| `DATENPORTAL_CATALOG_DUCKDB_SOURCE_TYPE` | `classpath` (Fixtures), `file` oder `http` |
| `DATENPORTAL_CATALOG_DUCKDB_CLASSPATH_LOCATION` | `catalog.duckdb` für die gebündelte Fixture |
| `SERVER_PORT` | optional; Default im Container ist `8080` |

Beispiel mit Manifestquelle:

```bash
docker run --rm -p 18082:8080 \
  -e DATENPORTAL_ADMIN_RELOAD_TOKEN=change-me \
  -e DATENPORTAL_CATALOG_SOURCE_TYPE=manifest \
  -e DATENPORTAL_CATALOG_HTTP_URL=http://host.docker.internal:8081/ch.so.daten/current.json \
  -e DATENPORTAL_CATALOG_DOWNLOAD_URL=http://localhost:8081/ch.so.daten \
  -e DATENPORTAL_CATALOG_DUCKDB_SOURCE_TYPE=classpath \
  -e DATENPORTAL_CATALOG_DUCKDB_CLASSPATH_LOCATION=catalog.duckdb \
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

## Ausblick GraalVM Native

Der Aufbau trennt `build`, `runtime` und die spätere Native-Stage. Für ein
Native-Image kommen zusätzlich Spring-AOT-Konfiguration und ein GraalVM-Builder
hinzu; das JVM-Image bleibt als Referenz und Rückfallweg erhalten. Der
Dev-Stack-Vertrag (Image-Name, Port, Umgebungsvariablen) soll dabei nicht
wechseln.

## Fehlerdiagnose

| Symptom | Prüfung |
|---|---|
| Container startet wiederholt neu | Manifestadresse und `current.json` prüfen; Logs mit `docker compose logs sodata` bzw. `docker logs <container>` |
| Healthcheck bleibt `unhealthy`, Anwendung läuft | Startzeit verkürzen/`start-period` prüfen; `/actuator/health/liveness` direkt mit `curl` testen |
| Port belegt | Host-Port `8082` frei machen oder `SODATA_PORT` ändern |
| Reload `503` | `DATENPORTAL_ADMIN_RELOAD_TOKEN` im Container leer |
| Reload `401` | Token stimmt nicht mit Jenkins `DATENPORTAL_PORTAL_RELOAD_TOKEN` überein |
| Erkunden zeigt falsche/fehlende Tabellen | Verwendete `catalog.duckdb` passt nicht zur Lieferung; Sync ist noch nicht implementiert |
