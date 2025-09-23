package dev.pakn.competitioncubes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LinkMappings {
    @RequestMapping("/competition")
    public String compPage(@CookieValue(value="user_secret", required = false) String userSecret, @RequestParam("roomId") String roomIdStr) {
        User user = DBController.getUserBySecret(userSecret);
        for (Match match:MatchController.getMatches()) {
            if (match.getRoomId()==Integer.parseInt(roomIdStr)) {
                for (int matchUserId:match.getUsers()) {
                    if (matchUserId==user.getUserId()) {
                        return "comp.html";
                    }
                }
            }
        }
        return "forward:/error.html";
    }

    @RequestMapping("/")
    public String mainPage(@CookieValue(value="user_secret", required = false) String userSecret, HttpServletResponse response) {
        //prevent browser caching
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache"); // For HTTP/1.0
        response.setDateHeader("Expires", 0); // For proxies

        if (userSecret!=null) {
            //auto login here!
            User user = DBController.getUserBySecret(userSecret);
            //if login succeeded. if they somehow have user_secret cookie but it doesn't exist, user will be null
            if (user!=null) return "main-logged-in.html";
        }
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

    @RequestMapping("/rankings")
    public String rankingsPage() {
        return "leaderboard.html";
    }

    @RequestMapping("/search") 
    public String searchPage() {
        return "search-page.html";
    }

    @RequestMapping("/rules")
    public String rulesPage() {
        return "rules.html";
    }
}
