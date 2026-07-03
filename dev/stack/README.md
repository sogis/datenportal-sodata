# Datenportal Dev Stack

Lokaler Docker-Compose-Stack für die Entwicklung und Demo der Datenportal-App.

Der Stack ist bewusst unter `dev/stack/` abgelegt, damit klar bleibt: Das ist keine Produktions-Infrastruktur, sondern eine lokale Mini-Umgebung mit Object Storage, Public-Download-Endpunkt und Hilfswerkzeugen. Spätere Services wie Postgres, Mailpit oder Keycloak können im gleichen `compose.yaml` ergänzt und über Compose Profiles aktiviert werden.

## Struktur

```text
dev/stack/
  compose.yaml
  .env.example
  README.md

  services/
    garage/
      garage.toml
      init/
        seed.sh
        website.json
        cors.json
    caddy/
      Caddyfile

  seed/
    object-storage/
      ch.so.datenportal/
        index.html
        downloads/
        metadata/
        models/
        testdata/

  scripts/
    up.sh
    down.sh
    reset.sh
    seed.sh
    list-objects.sh
    presign.sh
    upload-one-with-metadata.sh

  var/
    .gitkeep
```

## Enthaltene Services

- `garage`: Single-Node Garage mit S3 API, Website Endpoint und Admin API
- `downloads`: Caddy-Proxy für öffentliche Downloads unter `http://localhost:8081/ch.so.datenportal`
- `seed`: Einmaliger Seed-Container für Website/CORS und Upload der Demo-Daten
- `aws`: AWS-CLI-Hilfscontainer für lokale S3-Kommandos gegen Garage

## Dev-Credentials

Nur für lokale Entwicklung verwenden.

```text
Bucket:              ch.so.datenportal
Region:              garage
S3 API, Host:         http://localhost:3900
S3 API, Docker:       http://garage:3900
Public Downloads:    http://localhost:8081/ch.so.datenportal
Access key ID:       GK0123456789abcdef0123456789abcdef
Secret access key:   0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef
```

Die Defaults sind direkt in `compose.yaml` hinterlegt. Für lokale Overrides kannst du `.env.example` nach `.env` kopieren.
Für die Webapp passt dazu `DOWNLOAD_URL=http://localhost:8081/ch.so.datenportal/downloads`.

## Start

Aus dem Repo-Root:

```bash
cd dev/stack
./scripts/up.sh
```

Danach:

```bash
open http://localhost:8081/ch.so.datenportal/index.html
```

Beispiel:

```bash
curl -I http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2026.csv
```

## Nützliche Kommandos

Objekte auflisten:

```bash
./scripts/list-objects.sh
```

Seed-Daten erneut synchronisieren:

```bash
./scripts/seed.sh
```

Presigned URL erzeugen:

```bash
./scripts/presign.sh downloads/ch.so.abstimmungsresultate_2026.csv
```

Ein einzelnes File mit S3-Metadaten hochladen:

```bash
./scripts/upload-one-with-metadata.sh \
  ../../build/data/demo.csv \
  downloads/demo.csv \
  "dataset_id=ch.so.demo,uploaded_by=gretl,environment=dev"
```

Stoppen:

```bash
./scripts/down.sh
```

Alles zurücksetzen, inklusive lokaler Garage-Daten:

```bash
./scripts/reset.sh --yes
```

## Nutzung aus der Datenportal-App

Für eine lokal auf dem Host laufende Spring-Boot-App:

```properties
datenportal.s3.endpoint-url=http://localhost:3900
datenportal.s3.region=garage
datenportal.s3.bucket=ch.so.datenportal
datenportal.s3.access-key-id=GK0123456789abcdef0123456789abcdef
datenportal.s3.secret-access-key=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef
datenportal.download-base-url=http://localhost:8081/ch.so.datenportal
```

Wenn die App selbst im gleichen Compose-Netz läuft:

```properties
datenportal.s3.endpoint-url=http://garage:3900
datenportal.download-base-url=http://downloads:8081/ch.so.datenportal
```

## Nutzung aus Jenkins/GRETL lokal

Wenn Jenkins auf dem Host läuft:

```bash
export AWS_ENDPOINT_URL=http://localhost:3900
export AWS_DEFAULT_REGION=garage
export AWS_ACCESS_KEY_ID=GK0123456789abcdef0123456789abcdef
export AWS_SECRET_ACCESS_KEY=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef
export DATENPORTAL_BUCKET=ch.so.datenportal
```

Wenn Jenkins als Container im gleichen Docker-Netz läuft:

```bash
export AWS_ENDPOINT_URL=http://garage:3900
export AWS_DEFAULT_REGION=garage
export AWS_ACCESS_KEY_ID=GK0123456789abcdef0123456789abcdef
export AWS_SECRET_ACCESS_KEY=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef
export DATENPORTAL_BUCKET=ch.so.datenportal
```

Beispiel mit AWS CLI:

```bash
aws --endpoint-url "$AWS_ENDPOINT_URL" s3 sync build/data/ "s3://$DATENPORTAL_BUCKET/downloads/" --delete
aws --endpoint-url "$AWS_ENDPOINT_URL" s3 cp metadata.json "s3://$DATENPORTAL_BUCKET/metadata/metadata.json" \
  --metadata "dataset_id=ch.so.demo,uploaded_by=gretl,environment=dev"
```

## Gitignore-Empfehlung

In die `.gitignore` im Repo-Root:

```gitignore
# Lokale Overrides und Laufzeitdaten des Docker-Dev-Stacks
/dev/stack/.env
/dev/stack/var/*
!/dev/stack/var/.gitkeep

# Optional: lokale Dumps/temporäre Seed-Dateien, falls ihr solche Ordner später nutzt
/dev/stack/tmp/
/dev/stack/dumps/
```

Die Seed-Daten unter `dev/stack/seed/object-storage/` sind bewusst nicht ignoriert. Wenn sie zu gross werden, sollte man stattdessen nur ein kleines Fixture-Set versionieren und grosse Demo-Daten über ein Skript herunterladen oder generieren.
