package dev.pakn.competitioncubes;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

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
        throw new HttpForbiddenException();
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
            if (user!=null) {
                return "main-logged-in.html";
            }
        }
        return "main.html";
    }

    @RequestMapping("/create-account")
    public String createAccountPage() {
        return "createAccount.html";
    }

    @RequestMapping("/user/{userId}")
    public String userPage(@CookieValue(value="user_secret", required = false) String userSecret, @PathVariable int userId, HttpServletResponse response) {
        //prevent browser caching (for users with userinfoaccess)
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache"); // For HTTP/1.0
        response.setDateHeader("Expires", 0); // For proxies
        
        User user = DBController.getUserBySecret(userSecret);
        if (DBController.userExists(userId)) {
            if (user == null) {
                return "forward:/profile_pages/profile.html";
            }else {
                if (user.getPermissionLevel().hasBanAccess())
                    return "forward:/profile_pages/profile_admin.html";
                if (user.getPermissionLevel().hasUserInfoAccess() || user.getUserId()==userId)
                    return "forward:/profile_pages/profile_user.html";
            }
            return "forward:/profile_pages/profile.html";
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
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

    @RequestMapping("/settings")
    public String settingsPage() {
        return "settings.html";
    }

    @RequestMapping("/admin")
    public String adminDashboard(@CookieValue(value="user_secret", required = false) String userSecret) {
        if (userSecret!=null) {
            //auto login here!
            User user = DBController.getUserBySecret(userSecret);
            //if login succeeded. if they somehow have user_secret cookie but it doesn't exist, user will be null
            if (user!=null && user.getPermissionLevel().hasAdminDashboardAccess()) return "admin-dashboard.html";
        }
        throw new HttpForbiddenException();
    }
}
