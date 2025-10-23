package dev.pakn.competitioncubes;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorMapper implements ErrorController {
    //error mapping
    @RequestMapping("/error")
    public String error(HttpServletRequest req) {
        System.out.println("bruh");
        Object status = req.getAttribute("jakarta.servlet.error.status_code");
        System.out.println(status); 
        if (status!=null) {
            int code = Integer.parseInt(status.toString());
            System.out.println(code);
            if (code==HttpStatus.NOT_FOUND.value()) {
                return "error-404.html";
            }else if (code==HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error-500.html";
            }
        }
        return "error.html";
    }
}
