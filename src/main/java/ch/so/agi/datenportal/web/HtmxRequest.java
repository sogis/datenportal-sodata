package ch.so.agi.datenportal.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public final class HtmxRequest {

    private HtmxRequest() {}

    public static boolean isHtmx(HttpServletRequest request) {
        return "true".equalsIgnoreCase(request.getHeader("HX-Request"));
    }

    public static Optional<String> target(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("HX-Target"))
                .filter(value -> !value.isBlank());
    }

    public static boolean targetsResults(HttpServletRequest request) {
        return isHtmx(request)
                && target(request).map("dataset-results-shell"::equals).orElse(false);
    }
}
