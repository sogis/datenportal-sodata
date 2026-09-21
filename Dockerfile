# syntax=docker/dockerfile:1

# JVM-Betriebsimage der Datenportal-Webanwendung.
#
# Der Build laeuft vollstaendig im Container; auf dem Host wird nur Docker
# benoetigt. Ein spaeteres GraalVM-Native-Image kann als eigene Stage ergaenzt
# werden, ohne den Stack-Vertrag zu aendern.

ARG TEMURIN_JDK_IMAGE=eclipse-temurin:25-jdk
ARG TEMURIN_JRE_IMAGE=eclipse-temurin:25-jre
ARG NODE_IMAGE=node:22-bookworm-slim

FROM ${NODE_IMAGE} AS node-runtime

FROM ${TEMURIN_JDK_IMAGE} AS build

# Node 22 fuer den von Gradle gestarteten Explore-/WebR-Build
# (Vite verlangt Node ^20.19.0 || >=22.12.0).
COPY --from=node-runtime /usr/local/bin/node /usr/local/bin/node
COPY --from=node-runtime /usr/local/lib/node_modules /usr/local/lib/node_modules
RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends libstdc++6 \
    && rm -rf /var/lib/apt/lists/* \
    && ln -sf /usr/local/lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm \
    && ln -sf /usr/local/lib/node_modules/npm/bin/npx-cli.js /usr/local/bin/npx \
    && node --version \
    && npm --version

WORKDIR /workspace

COPY gradlew settings.gradle gradle.properties build.gradle ./
COPY gradle ./gradle
COPY src ./src
COPY spec ./spec

ARG GIT_COMMIT=unknown

RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/root/.npm \
    ./gradlew --no-daemon bootJar -PgitCommit="${GIT_COMMIT}"

FROM ${TEMURIN_JRE_IMAGE} AS runtime

ARG IMAGE_VERSION=local
ARG GIT_COMMIT=unknown

LABEL org.opencontainers.image.title="datenportal-sodata" \
      org.opencontainers.image.description="Datenportal Webanwendung Kanton Solothurn" \
      org.opencontainers.image.version="${IMAGE_VERSION}" \
      org.opencontainers.image.revision="${GIT_COMMIT}"

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends curl tzdata \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system datenportal \
    && useradd --system --gid datenportal --home-dir /opt/datenportal --shell /usr/sbin/nologin datenportal \
    && mkdir -p /opt/datenportal \
    && chown datenportal:0 /opt/datenportal

WORKDIR /opt/datenportal

# Die Katalog-Fixture inklusive catalog.duckdb liegt ueber spec/fixtures im Jar.
COPY --from=build --chown=datenportal:datenportal /workspace/build/libs/datenportal-sodata-*.jar app.jar

# OpenShift kann dem Container zur Laufzeit eine beliebige UID mit Gruppe 0
# zuweisen. Die Anwendung schreibt nicht in dieses Verzeichnis, benötigt aber
# Leserechte unabhängig vom konkreten Laufzeitbenutzer.
RUN chgrp -R 0 /opt/datenportal \
    && chmod -R g=u /opt/datenportal

USER datenportal
EXPOSE 8080

ENV TZ=Europe/Zurich \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=12 \
    CMD curl -fsS http://127.0.0.1:8080/actuator/health/liveness >/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/opt/datenportal/app.jar"]
