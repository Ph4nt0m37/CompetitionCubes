package dev.pakn.competitioncubes;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
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
            System.out.println("exists");
            return "main-logged-in.html";
        }
        System.out.println("not found");
        return "main.html";
    }

    @RequestMapping("/create-account")
    public String createAccountPage() {
        return "createAccount.html";
    }

    @RequestMapping("/user/{userId}")
    public String userPage(@PathVariable int userId) {
        if (DBController.userExists(userId)) {
            return "forward:/profile.html";
        }else{
            return "forward:/error.html";
        }
    }
}
