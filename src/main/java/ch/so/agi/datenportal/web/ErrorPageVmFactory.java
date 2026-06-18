package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.web.view.ErrorPageVm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public final class ErrorPageVmFactory {

    private static final String CATALOG_HREF = "/datasets";
    private static final String CATALOG_LABEL = "Zurück zu Daten und Statistiken";

    private final PageChromeFactory pageChromeFactory;

    public ErrorPageVmFactory(PageChromeFactory pageChromeFactory) {
        this.pageChromeFactory = pageChromeFactory;
    }

    public ErrorPageVm notFound(String message) {
        return new ErrorPageVm(
                pageChromeFactory.notFoundPage(),
                HttpStatus.NOT_FOUND.value(),
                "Seite nicht gefunden",
                message,
                CATALOG_HREF,
                CATALOG_LABEL);
    }

    public ErrorPageVm serverError(int statusCode) {
        return new ErrorPageVm(
                pageChromeFactory.errorPage("Fehler | Datenportal", "Fehler"),
                statusCode,
                "Es ist ein Fehler aufgetreten",
                "Die Anfrage konnte nicht verarbeitet werden. Bitte versuchen Sie es später erneut.",
                CATALOG_HREF,
                CATALOG_LABEL);
    }

    public ErrorPageVm forStatus(int statusCode) {
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return notFound("Die angeforderte Seite konnte nicht gefunden werden.");
        }
        return serverError(statusCode);
    }
}
