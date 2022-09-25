package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List.*

// Annotation
@Controller
// Main class
class QuotationsController {

    @RequestMapping("/hello")
    @ResponseBody

    // Method
    fun getQuotation(): MutableList<String> {
        return of("hello")
    }
}