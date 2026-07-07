package ch.so.agi.datenportal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Objects;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class SecurityHeadersConfiguration {

    private final SecurityCspProperties cspProperties;
    private final CatalogProperties catalogProperties;

    public SecurityHeadersConfiguration(
            SecurityCspProperties cspProperties,
            CatalogProperties catalogProperties) {
        this.cspProperties = Objects.requireNonNull(cspProperties, "cspProperties must not be null");
        this.catalogProperties = Objects.requireNonNull(catalogProperties, "catalogProperties must not be null");
    }

    @Bean
    FilterRegistrationBean<OncePerRequestFilter> securityHeadersFilter() {
        var registration = new FilterRegistrationBean<OncePerRequestFilter>();
        registration.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain) throws ServletException, IOException {
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                response.setHeader("X-Frame-Options", "DENY");
                response.setHeader("Permissions-Policy", "accelerometer=(), camera=(), geolocation=(), gyroscope=(), microphone=(), payment=(), usb=()");
                response.setHeader("Content-Security-Policy", csp(request));
                filterChain.doFilter(request, response);
            }
        });
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    String csp(HttpServletRequest request) {
        return csp(normalizedPath(request));
    }

    String csp(String requestPath) {
        return csp(requiresBrowserRuntimeEval(requestPath));
    }

    String csp() {
        return csp(false);
    }

    private String csp(boolean allowUnsafeEval) {
        return "default-src 'self'; "
                + "script-src 'self' 'wasm-unsafe-eval'" + (allowUnsafeEval ? " 'unsafe-eval'" : "") + "; "
                + "style-src 'self' 'unsafe-inline'; "
                + "img-src 'self' data:; "
                + "font-src 'self'; "
                + "connect-src " + connectSrc() + "; "
                + "worker-src 'self' blob:; "
                + "object-src 'none'; "
                + "base-uri 'self'; "
                + "frame-ancestors 'none'; "
                + "form-action 'self'";
    }

    static boolean requiresBrowserRuntimeEval(String requestPath) {
        if (requestPath == null || requestPath.isBlank()) {
            return false;
        }
        if (requestPath.equals("/explore")
                || requestPath.startsWith("/explore/")
                || requestPath.startsWith("/webr/")
                || requestPath.startsWith("/webr-packages/")) {
            return true;
        }
        return (requestPath.startsWith("/datasets/") || requestPath.startsWith("/series/"))
                && requestPath.contains("/explore");
    }

    private static String normalizedPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            return path.substring(contextPath.length());
        }
        return path;
    }

    private String connectSrc() {
        var values = new LinkedHashSet<>(cspProperties.connectSrc());
        if (cspProperties.includeCatalogDownloadOrigin()) {
            catalogDownloadOrigin().ifPresent(values::add);
        }
        return String.join(" ", values);
    }

    private java.util.Optional<String> catalogDownloadOrigin() {
        String downloadUrl = catalogProperties.downloadUrl();
        if (downloadUrl == null || downloadUrl.startsWith("/")) {
            return java.util.Optional.empty();
        }
        URI uri = URI.create(downloadUrl);
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            return java.util.Optional.empty();
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(new URI(
                    uri.getScheme(),
                    null,
                    uri.getHost(),
                    uri.getPort(),
                    null,
                    null,
                    null).toString());
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Invalid datenportal.catalog.download-url origin", ex);
        }
    }
}
