package ch.so.agi.datenportal.catalog.service;

import ch.so.agi.datenportal.admin.reload.ReloadFailureType;
import ch.so.agi.datenportal.admin.reload.ReloadResult;
import ch.so.agi.datenportal.admin.reload.ReloadStatus;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.importxtf.CatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.CatalogSourceException;
import ch.so.agi.datenportal.catalog.importxtf.CatalogValidationException;
import ch.so.agi.datenportal.catalog.importxtf.XtfParseException;
import ch.so.agi.datenportal.search.CatalogSearchIndexBuildException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public final class CatalogReloadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogReloadService.class);

    private final CatalogSource catalogSource;
    private final CatalogSnapshotBuilder snapshotBuilder;
    private final CatalogService catalogService;
    private final Clock clock;
    private final ReentrantLock reloadLock = new ReentrantLock();
    private final AtomicReference<ReloadResult> lastSuccessfulReload = new AtomicReference<>();
    private final AtomicReference<ReloadResult> lastFailedReload = new AtomicReference<>();

    public CatalogReloadService(
            CatalogSource catalogSource,
            CatalogSnapshotBuilder snapshotBuilder,
            CatalogService catalogService,
            Clock clock) {
        this.catalogSource = Objects.requireNonNull(catalogSource, "catalogSource must not be null");
        this.snapshotBuilder = Objects.requireNonNull(snapshotBuilder, "snapshotBuilder must not be null");
        this.catalogService = Objects.requireNonNull(catalogService, "catalogService must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public ReloadResult reload() {
        Instant startedAt = clock.instant();
        String attemptId = UUID.randomUUID().toString();
        if (!reloadLock.tryLock()) {
            ReloadResult conflict = ReloadResult.failure(
                    ReloadFailureType.CONFLICT,
                    409,
                    "A catalog reload is already running.",
                    startedAt,
                    clock.instant());
            lastFailedReload.set(conflict);
            LOGGER.warn("Catalog reload {} rejected because another reload is already running.", attemptId);
            return conflict;
        }

        LOGGER.info("Catalog reload {} started from source {}.", attemptId, catalogSource.description());
        try {
            var bytes = catalogSource.load();
            LOGGER.info(
                    "Catalog reload {} downloaded {} bytes from {}.",
                    attemptId,
                    bytes.sizeInBytes(),
                    bytes.sourceDescription());

            CatalogBuildResult buildResult = snapshotBuilder.build(bytes);
            CatalogSnapshot newSnapshot = buildResult.snapshot();
            boolean published = false;
            try {
                catalogService.replaceSnapshot(newSnapshot);
                published = true;
            } finally {
                if (!published) {
                    newSnapshot.close();
                }
            }

            Instant finishedAt = clock.instant();
            ReloadResult result = ReloadResult.success(newSnapshot, buildResult.warnings(), startedAt, finishedAt);
            lastSuccessfulReload.set(result);
            LOGGER.info(
                    "Catalog reload {} replaced snapshot and index with {} visible entries in source {}.",
                    attemptId,
                    result.visibleEntries(),
                    result.sourceDescription());
            LOGGER.info("Catalog reload {} finished successfully.", attemptId);
            return result;
        } catch (CatalogSourceException ex) {
            return failure(attemptId, ReloadFailureType.SOURCE, 502, "Catalog download failed: " + ex.getMessage(), startedAt, ex);
        } catch (XtfParseException ex) {
            return failure(attemptId, ReloadFailureType.PARSE, 422, "Catalog parsing failed: " + ex.getMessage(), startedAt, ex);
        } catch (CatalogValidationException ex) {
            return failure(attemptId, ReloadFailureType.VALIDATION, 422, "Catalog validation failed: " + ex.getMessage(), startedAt, ex);
        } catch (CatalogSearchIndexBuildException ex) {
            return failure(attemptId, ReloadFailureType.INDEX, 500, "Catalog search index build failed.", startedAt, ex);
        } catch (RuntimeException ex) {
            return failure(attemptId, ReloadFailureType.UNEXPECTED, 500, "Unexpected catalog reload failure.", startedAt, ex);
        } finally {
            reloadLock.unlock();
        }
    }

    public ReloadStatus status() {
        return catalogService.withSnapshot(snapshot -> ReloadStatus.from(
                snapshot,
                lastSuccessfulReload.get(),
                lastFailedReload.get(),
                reloadLock.isLocked()));
    }

    private ReloadResult failure(
            String attemptId,
            ReloadFailureType failureType,
            int httpStatus,
            String message,
            Instant startedAt,
            RuntimeException ex) {
        Instant finishedAt = clock.instant();
        ReloadResult result = ReloadResult.failure(failureType, httpStatus, message, startedAt, finishedAt);
        lastFailedReload.set(result);
        LOGGER.warn("Catalog reload {} failed: {}", attemptId, message, ex);
        LOGGER.info("Catalog reload {} finished with failure.", attemptId);
        return result;
    }
}
