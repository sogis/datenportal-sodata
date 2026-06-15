package ch.so.agi.datenportal.catalog.importxtf;

public class XtfParseException extends RuntimeException {

    public XtfParseException(String sourceDescription, XtfElementPath path, String message) {
        super(format(sourceDescription, path, message));
    }

    public XtfParseException(String sourceDescription, XtfElementPath path, String message, Throwable cause) {
        super(format(sourceDescription, path, message), cause);
    }

    private static String format(String sourceDescription, XtfElementPath path, String message) {
        return "Failed to parse " + sourceDescription + " at " + path.display() + ": " + message;
    }
}
