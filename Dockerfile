# syntax=docker/dockerfile:1

# The default target is the GraalVM native image. Build the JVM fallback with
# --target jvm-runtime.
ARG TEMURIN_JDK_IMAGE=eclipse-temurin:25-jdk
ARG JVM_RUNTIME_IMAGE=registry.access.redhat.com/ubi9/openjdk-25-runtime
ARG GRAALVM_NATIVE_IMAGE=ghcr.io/graalvm/native-image-community:25-ol9
ARG NATIVE_RUNTIME_IMAGE=registry.access.redhat.com/ubi9/ubi-minimal:latest
ARG NODE_IMAGE=node:22-bookworm-slim

FROM ${NODE_IMAGE} AS node-runtime

FROM ${TEMURIN_JDK_IMAGE} AS jvm-build

# Node 22 for the Gradle-started Explore/WebR build (Vite requires
# ^20.19.0 || >=22.12.0).
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

FROM ${GRAALVM_NATIVE_IMAGE} AS native-build

# Node 22 runs on the Oracle Linux 9 GraalVM builder and drives the frontend
# asset build required by processResources.
COPY --from=node-runtime /usr/local/bin/node /usr/local/bin/node
COPY --from=node-runtime /usr/local/lib/node_modules /usr/local/lib/node_modules
RUN ln -sf /usr/local/lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm \
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
    ./gradlew --no-daemon nativeCompile -PgitCommit="${GIT_COMMIT}"

FROM ${JVM_RUNTIME_IMAGE} AS jvm-runtime

ARG IMAGE_VERSION=local
ARG GIT_COMMIT=unknown

LABEL org.opencontainers.image.title="datenportal-sodata-jvm" \
      org.opencontainers.image.description="Datenportal Webanwendung Kanton Solothurn (JVM)" \
      org.opencontainers.image.version="${IMAGE_VERSION}" \
      org.opencontainers.image.revision="${GIT_COMMIT}"

USER 0
RUN microdnf install -y tzdata ca-certificates \
    && microdnf clean all \
    && mkdir -p /opt/datenportal \
    && chown -R 185:0 /opt/datenportal

WORKDIR /opt/datenportal

# The catalog fixtures, including catalog.duckdb, are included in the boot JAR.
COPY --from=jvm-build --chown=185:0 /workspace/build/libs/datenportal-sodata-*.jar app.jar

# OpenShift may assign an arbitrary UID with group 0.
RUN chgrp -R 0 /opt/datenportal \
    && chmod -R g=u /opt/datenportal

USER 185:0
EXPOSE 8080

ENV TZ=Europe/Zurich \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=12 \
    CMD curl -fsS http://127.0.0.1:8080/actuator/health/liveness >/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/opt/datenportal/app.jar"]

FROM ${NATIVE_RUNTIME_IMAGE} AS native-runtime

ARG IMAGE_VERSION=local
ARG GIT_COMMIT=unknown

LABEL org.opencontainers.image.title="datenportal-sodata" \
      org.opencontainers.image.description="Datenportal Webanwendung Kanton Solothurn (GraalVM Native Image)" \
      org.opencontainers.image.version="${IMAGE_VERSION}" \
      org.opencontainers.image.revision="${GIT_COMMIT}"

RUN microdnf install -y tzdata ca-certificates libstdc++ \
    && microdnf clean all \
    && mkdir -p /opt/datenportal \
    && chown -R 1001:0 /opt/datenportal

WORKDIR /opt/datenportal

COPY --from=native-build --chown=1001:0 /workspace/build/native/nativeCompile/datenportal-sodata /opt/datenportal/app

# OpenShift may assign an arbitrary UID with group 0.
RUN chgrp -R 0 /opt/datenportal \
    && chmod -R g=u /opt/datenportal

USER 1001:0
EXPOSE 8080

ENV TZ=Europe/Zurich

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=12 \
    CMD curl -fsS http://127.0.0.1:8080/actuator/health/liveness >/dev/null || exit 1

ENTRYPOINT ["/opt/datenportal/app"]
