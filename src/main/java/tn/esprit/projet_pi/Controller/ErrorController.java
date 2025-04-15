package tn.esprit.projet_pi.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        // Provide your custom error page logic here
        return "error";  // Return the error page view name
    }


    public String getErrorPath() {
        return "/error";
    }
}
