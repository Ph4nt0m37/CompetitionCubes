package dev.pakn.competitioncubes;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LinkMappings {
    @RequestMapping("/competition")
    public String compPage() {
        return "comp.html";
    }

    @RequestMapping("/")
    public String mainPage(@CookieValue(value="wca_access_token", required = false) String accessToken) {
        if (accessToken!=null) {
            //auto login here!
            System.out.println("auto log in found");
        }
        return "main.html";
    }

    @RequestMapping("/auth/callback")
    public String createAccountPage() {
        return "createAccount.html";
    }
}
