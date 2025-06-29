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
    public String mainPage(@CookieValue(value="user_secret", required = false) String userSecret) {
        if (userSecret!=null) {
            //auto login here!
            User user = DBController.getUserBySecret(userSecret);
        }
        return "main.html";
    }

    @RequestMapping("/create-account")
    public String createAccountPage() {
        return "createAccount.html";
    }
}
