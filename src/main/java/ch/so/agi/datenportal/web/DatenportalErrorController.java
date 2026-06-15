package ch.so.agi.datenportal.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public final class DatenportalErrorController implements ErrorController {

    private final ErrorPageVmFactory errorPageVmFactory;

    public DatenportalErrorController(ErrorPageVmFactory errorPageVmFactory) {
        this.errorPageVmFactory = errorPageVmFactory;
    }

    @RequestMapping("/error")
    public String error(HttpServletRequest request, HttpServletResponse response, Model model) {
        int statusCode = errorStatusCode(request, response);
        response.setStatus(statusCode);
        var page = errorPageVmFactory.forStatus(statusCode);
        model.addAttribute("page", page);
        return statusCode == HttpStatus.NOT_FOUND.value() ? "pages/notFound" : "pages/error";
    }

    private static int errorStatusCode(HttpServletRequest request, HttpServletResponse response) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status instanceof Number number) {
            return number.intValue();
        }
        if (response.getStatus() >= 400) {
            return response.getStatus();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
