package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.importxtf.CatalogSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public final class CatalogArtifactController {

    private final CatalogSource catalogSource;
    private final CatalogSource catalogDuckDbSource;

    public CatalogArtifactController(
            CatalogSource catalogSource,
            @Qualifier("catalogDuckDbSource") CatalogSource catalogDuckDbSource) {
        this.catalogSource = catalogSource;
        this.catalogDuckDbSource = catalogDuckDbSource;
    }

    @GetMapping(value = "/catalog/published-catalog.xtf", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> publishedCatalog() {
        return artifact(
                catalogSource,
                "published-catalog.xtf",
                MediaType.APPLICATION_XML);
    }

    @GetMapping(value = "/catalog/catalog.duckdb", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> duckDbCatalog() {
        return artifact(
                catalogDuckDbSource,
                "catalog.duckdb",
                MediaType.APPLICATION_OCTET_STREAM);
    }

    private static ResponseEntity<byte[]> artifact(CatalogSource source, String fileName, MediaType mediaType) {
        byte[] bytes = source.load().bytes();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(bytes.length)
                .cacheControl(CacheControl.noCache().cachePrivate())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(fileName).build().toString())
                .body(bytes);
    }
}
