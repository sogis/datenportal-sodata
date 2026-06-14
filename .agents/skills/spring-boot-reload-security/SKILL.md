---
name: spring-boot-reload-security
description: Use for the protected runtime reload endpoint, security choice, token handling, audit logging, and atomic reload behavior in Spring Boot.
compatibility: codex, opencode
metadata:
  project: datenportal
---

# Skill: Spring Boot Reload Security

Use this skill when implementing or changing the protected reload endpoint.

## Goal

Provide an operational endpoint that reloads the PublishedCatalog XTF/XML during runtime without restarting the application.

## Security options

A simple shared secret header is acceptable for the first implementation if documented and tested. Spring Security may be used when it keeps the solution clearer.

Recommended simple contract:

```http
POST /admin/catalog/reload
X-Reload-Token: <secret>
```

The token must come from configuration/environment, never from source code.

## Endpoint behavior

- Only accept POST.
- Reject missing or invalid token.
- Do not expose the token in logs.
- Return clear status for success/failure.
- On failure, keep the previous snapshot and index active.
- Include enough error detail for operators, but do not leak secrets or stack traces to users.
- Do not allow cache/proxy prefetching to mutate state.

## Atomic reload workflow

1. Resolve configured XTF URL or fixture path.
2. Download/read candidate content.
3. Parse candidate.
4. Validate candidate snapshot.
5. Build candidate search index.
6. Atomically swap active snapshot and index.
7. Record success/failure in logs.

## Configuration

Document properties such as:

```yaml
catalog:
  xtf-url: http://localhost:8089/published_catalog.xtf
  reload-token: ${CATALOG_RELOAD_TOKEN:}
```

If the token is empty in production profile, startup should fail or the endpoint should be disabled. Choose one behavior and document it.

## Tests

Cover:

- reload denied without token
- reload denied with wrong token
- reload succeeds with valid token
- invalid candidate keeps old snapshot
- reload rebuilds search index
- endpoint does not accept GET for mutation
- token is not logged

## Logging

Log:

- reload requested
- source identifier or URL host/path without credentials
- success/failure
- entry counts after success
- validation error summary after failure

Never log:

- reload token
- full credentialed URL
- raw secret environment variables
