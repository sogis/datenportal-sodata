package ch.so.agi.datenportal.admin.reload;

import ch.so.agi.datenportal.catalog.service.CatalogReloadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/catalog")
public final class AdminCatalogController {

    private final CatalogReloadService reloadService;
    private final ReloadTokenVerifier tokenVerifier;

    public AdminCatalogController(
            CatalogReloadService reloadService,
            ReloadTokenVerifier tokenVerifier) {
        this.reloadService = reloadService;
        this.tokenVerifier = tokenVerifier;
    }

    @PostMapping("/reload")
    public ResponseEntity<ReloadResponse> reload(
            @RequestHeader(value = ReloadTokenVerifier.HEADER_NAME, required = false) String token) {
        ResponseEntity<ReloadResponse> rejected = rejectIfUnauthorized(token);
        if (rejected != null) {
            return rejected;
        }

        ReloadResult result = reloadService.reload();
        return ResponseEntity
                .status(HttpStatusCode.valueOf(result.httpStatus()))
                .body(ReloadResponse.from(result));
    }

    @GetMapping("/status")
    public ResponseEntity<ReloadStatusResponse> status(
            @RequestHeader(value = ReloadTokenVerifier.HEADER_NAME, required = false) String token) {
        if (!tokenVerifier.isConfigured()) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ReloadStatusResponse.disabled());
        }
        if (!tokenVerifier.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(ReloadStatusResponse.from(reloadService.status()));
    }

    private ResponseEntity<ReloadResponse> rejectIfUnauthorized(String token) {
        if (!tokenVerifier.isConfigured()) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ReloadResponse.rejected("Reload endpoint is disabled.", ReloadFailureType.UNEXPECTED));
        }
        if (!tokenVerifier.isValid(token)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ReloadResponse.rejected("Unauthorized.", ReloadFailureType.UNEXPECTED));
        }
        return null;
    }
}
