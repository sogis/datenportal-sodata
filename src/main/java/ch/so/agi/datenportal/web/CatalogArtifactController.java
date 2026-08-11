package ch.so.agi.datenportal.web;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import ch.so.agi.datenportal.catalog.importxtf.CatalogBytes;
import ch.so.agi.datenportal.catalog.service.CatalogService;
import java.time.Duration;
import java.util.Arrays;

@Controller
public final class CatalogArtifactController {

    private final CatalogService catalogService;

    public CatalogArtifactController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping(value = "/catalog/published-catalog.xtf", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Resource> publishedCatalog(
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        return catalogService.withSnapshot(snapshot -> artifact(
                snapshot.publishedCatalog(),
                "published-catalog.xtf",
                MediaType.APPLICATION_XML,
                null,
                ifNoneMatch));
    }

    @GetMapping(value = "/catalog/catalog.duckdb", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> duckDbCatalog(
            @RequestParam(value = "v", required = false) String version,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        return catalogService.withSnapshot(snapshot -> artifact(
                snapshot.duckDbCatalog(),
                "catalog.duckdb",
                MediaType.APPLICATION_OCTET_STREAM,
                version,
                ifNoneMatch));
    }

    private static ResponseEntity<Resource> artifact(
            CatalogBytes artifact,
            String fileName,
            MediaType mediaType,
            String version,
            String ifNoneMatch) {
        String etag = quotedEtag(artifact.contentHash());
        if (version != null && !version.isBlank() && !version.equals(artifact.contentHash())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header(HttpHeaders.ETAG, etag)
                    .build();
        }
        if (matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .header(HttpHeaders.ETAG, etag)
                    .build();
        }

        CacheControl cacheControl = version == null || version.isBlank()
                ? CacheControl.noCache().cachePrivate()
                : CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(artifact.sizeInBytes())
                .cacheControl(cacheControl)
                .header(HttpHeaders.ETAG, etag)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(fileName).build().toString())
                .body(new InputStreamResource(artifact.inputStream()));
    }

    private static String quotedEtag(String contentHash) {
        return '"' + contentHash + '"';
    }

    private static boolean matches(String ifNoneMatch, String etag) {
        if (ifNoneMatch == null || ifNoneMatch.isBlank()) {
            return false;
        }
        return Arrays.stream(ifNoneMatch.split(","))
                .map(String::trim)
                .anyMatch(candidate -> "*".equals(candidate)
                        || etag.equals(candidate)
                        || ("W/" + etag).equals(candidate));
    }
}
