package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.search.CatalogSearchException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public final class CatalogErrorControllerAdvice {

    private final ErrorPageVmFactory errorPageVmFactory;

    public CatalogErrorControllerAdvice(ErrorPageVmFactory errorPageVmFactory) {
        this.errorPageVmFactory = errorPageVmFactory;
    }

    @ExceptionHandler(CatalogNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(CatalogNotFoundException exception, Model model) {
        model.addAttribute("page", errorPageVmFactory.notFound(
                "Der angeforderte Katalogeintrag konnte nicht gefunden werden."));
        return "pages/notFound";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String staticResourceNotFound(NoResourceFoundException exception, Model model) {
        model.addAttribute("page", errorPageVmFactory.notFound(
                "Die angeforderte Seite konnte nicht gefunden werden."));
        return "pages/notFound";
    }

    @ExceptionHandler(CatalogSearchException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String searchUnavailable(
            CatalogSearchException exception,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        if ("true".equalsIgnoreCase(request.getHeader("HX-Request"))) {
            response.setHeader("HX-Refresh", "true");
        }
        model.addAttribute("page", errorPageVmFactory.serviceUnavailable(
                "Der Suchdienst ist momentan nicht verfügbar. Bitte versuchen Sie es später erneut."));
        return "pages/error";
    }
}
