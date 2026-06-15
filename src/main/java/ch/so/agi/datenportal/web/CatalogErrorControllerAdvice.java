package ch.so.agi.datenportal.web;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public final class CatalogErrorControllerAdvice {

    private final PageChromeFactory pageChromeFactory;

    public CatalogErrorControllerAdvice(PageChromeFactory pageChromeFactory) {
        this.pageChromeFactory = pageChromeFactory;
    }

    @ExceptionHandler(CatalogNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(CatalogNotFoundException exception, Model model) {
        model.addAttribute("chrome", pageChromeFactory.notFoundPage());
        model.addAttribute("title", "Seite nicht gefunden");
        model.addAttribute("message", "Der angeforderte Katalogeintrag konnte nicht gefunden werden.");
        return "pages/notFound";
    }
}
