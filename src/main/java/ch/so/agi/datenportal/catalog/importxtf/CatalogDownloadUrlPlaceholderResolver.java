package ch.so.agi.datenportal.catalog.importxtf;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

public final class CatalogDownloadUrlPlaceholderResolver {

    public static final String DOWNLOAD_URL_PLACEHOLDER = "${DOWNLOAD_URL}";

    private static final Pattern DOWNLOAD_URL_TOKEN_WITH_SLASHES =
            Pattern.compile(Pattern.quote(DOWNLOAD_URL_PLACEHOLDER) + "/*");

    public CatalogBytes resolve(CatalogBytes bytes, String downloadUrl) {
        Objects.requireNonNull(bytes, "bytes must not be null");

        String content = new String(bytes.bytes(), StandardCharsets.UTF_8);
        if (!content.contains(DOWNLOAD_URL_PLACEHOLDER)) {
            return bytes;
        }

        String normalizedDownloadUrl = normalizeDownloadUrl(downloadUrl);
        if (normalizedDownloadUrl == null) {
            throw new CatalogSourceException(
                    "Catalog source contains ${DOWNLOAD_URL}, but datenportal.catalog.download-url is not configured");
        }

        var matcher = DOWNLOAD_URL_TOKEN_WITH_SLASHES.matcher(content);
        String transformed = matcher.replaceAll(match -> {
            boolean tokenHasFollowingSlashes = match.group().length() > DOWNLOAD_URL_PLACEHOLDER.length();
            return tokenHasFollowingSlashes
                    ? java.util.regex.Matcher.quoteReplacement(joinPrefix(normalizedDownloadUrl))
                    : java.util.regex.Matcher.quoteReplacement(normalizedDownloadUrl);
        });

        return new CatalogBytes(
                transformed.getBytes(StandardCharsets.UTF_8),
                bytes.sourceDescription(),
                bytes.fetchedAt());
    }

    public static String normalizeDownloadUrl(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        while (normalized.endsWith("/") && !"/".equals(normalized)) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String joinPrefix(String normalizedDownloadUrl) {
        return "/".equals(normalizedDownloadUrl) ? "/" : normalizedDownloadUrl + "/";
    }
}
