package dev.pakn.competitioncubes;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.slf4j.Logger;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LinkMappings {
    private static Logger logger = LoggerFactory.getLogger(LinkMappings.class);

    /*@GetMapping("/{path:[^\\.]*}")
    public String catchAll() {
        try {
            if (!ServerInfo.hasLaunched()) {
                return "launch-waiting.html";
            }else {
                throw new HttpNotFoundException();
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw new HttpForbiddenException(); 
        }
    }*/

    @GetMapping("/competition")
    public String compPage(@CookieValue(value="launch_override",required = false) String override, @CookieValue(value="user_secret", required = false) String userSecret, @RequestParam("roomId") String roomIdStr) {
        if (!ServerInfo.hasLaunched() && (override==null || !override.equals(ServerInfo.overridePassword))) {
            return "launch-waiting.html";
        }
        User user = DBController.getUserBySecret(userSecret);
        if (user!=null) {
            if (user.getCurrentMatch()!=null && user.getCurrentMatch().isPrivate()) {
                return "comp_private.html";
            }
            for (Match match:MatchController.getMatches()) {
                if (match.getRoomId()==Integer.parseInt(roomIdStr)) {
                    for (int matchUserId:match.getUsers()) {
                        if (matchUserId==user.getUserId()) {
                            return "comp.html";
                        }
                    }
                }
            }
        }else {
            throw new HttpInternalServerException();
        }
        throw new HttpForbiddenException();
    }

    @GetMapping("/")
    //im NOT using @AuthenticationPrinciple here because I need to accept unauthorized users. if I add @PreAuthorized, then non-authed users will get 401
    public String mainPage(@CookieValue(value="launch_override",required = false) String override, @CookieValue(value="user_secret", required = false) String userSecret, HttpServletResponse response) {
        //prevent browser caching
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache"); // For HTTP/1.0
        response.setDateHeader("Expires", 0); // For proxies

        if (!ServerInfo.hasLaunched() && (override==null || !override.equals(ServerInfo.overridePassword))) {
            return "launch-waiting.html";
        }

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

    @GetMapping("/create-account")
    public String createAccountPage(@CookieValue(value="launch_override",required = false) String override, @CookieValue(value="user_secret", required = false) String userSecret) {
        if (!ServerInfo.hasLaunched() && (override==null || !override.equals(ServerInfo.overridePassword))) {
            return "launch-waiting.html";
        }
        if (userSecret==null) {
            throw new HttpUnauthorizedException();
        }
        return "createAccount.html";
    }

    @GetMapping("/user/{userId}")
    public String userPage(@CookieValue(value="launch_override",required = false) String override, @CookieValue(value="user_secret", required = false) String userSecret, @PathVariable int userId, HttpServletResponse response) {
        //prevent browser caching (for users with userinfoaccess)
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache"); // For HTTP/1.0
        response.setDateHeader("Expires", 0); // For proxies

        if (!ServerInfo.hasLaunched() && (override==null || !override.equals(ServerInfo.overridePassword))) {
            return "launch-waiting.html";
        }
        
        User user = DBController.getUserBySecret(userSecret);
        if (DBController.userExists(userId)) {
            if (user == null) {
                return "forward:/profile_pages/profile_unauthed.html";
            }else {
                if (user.getPermissionLevel().hasBanAccess() && user.getUserId()!=userId)
                    return "forward:/profile_pages/profile_admin.html";
                if (user.getPermissionLevel().hasUserInfoAccess() || user.getUserId()==userId)
                    return "forward:/profile_pages/profile_user.html";
            }
            return "forward:/profile_pages/profile.html";
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/rankings")
    public String rankingsPage(@CookieValue(value="launch_override",required = false) String override) {
        if (!ServerInfo.hasLaunched() && (override==null || !override.equals(ServerInfo.overridePassword))) {
            return "launch-waiting.html";
        }
        return "leaderboard.html";
    }

    @GetMapping("/tutorial")
    public String tutorialPage(@CookieValue(value="launch_override",required = false) String override) {
        if (!ServerInfo.hasLaunched() && (override==null || !override.equals(ServerInfo.overridePassword))) {
            return "launch-waiting.html";
        }
        return "tutorial.html";
    }

    @GetMapping("/demo")
    public String demoPage() {
        return "demo.html";
    }

    @GetMapping("/search") 
    public String searchPage(@CookieValue(value="launch_override",required = false) String override) {
        if (!ServerInfo.hasLaunched() && (override==null || !override.equals(ServerInfo.overridePassword))) {
            return "launch-waiting.html";
        }
        return "search-page.html";
    }

    @GetMapping("/rules")
    public String rulesPage(@CookieValue(value="launch_override",required = false) String override) {
        if (!ServerInfo.hasLaunched() && (override==null || !override.equals(ServerInfo.overridePassword))) {
            return "launch-waiting.html";
        }
        return "rules.html";
    }

    @GetMapping("/settings")
    public String settingsPage(@CookieValue(value="launch_override",required = false) String override, @CookieValue(value="user_secret", required = false) String userSecret) {
        if (!ServerInfo.hasLaunched() && (override==null || !override.equals(ServerInfo.overridePassword))) {
            return "launch-waiting.html";
        }
        if (userSecret!=null) {
            //auto login here!
            User user = DBController.getUserBySecret(userSecret);
            //if login succeeded. if they somehow have user_secret cookie but it doesn't exist, user will be null
            if (user!=null) {
                return "settings.html";
            }
        }
        throw new HttpUnauthorizedException();
    }

    @GetMapping("/admin")
    public String adminDashboard(@CookieValue(value="launch_override",required = false) String override, @CookieValue(value="user_secret", required = false) String userSecret) {
        if (!ServerInfo.hasLaunched() && (override==null || !override.equals(ServerInfo.overridePassword))) {
            return "launch-waiting.html";
        }
        if (userSecret!=null) {
            //auto login here!
            User user = DBController.getUserBySecret(userSecret);
            //if login succeeded. if they somehow have user_secret cookie but it doesn't exist, user will be null
            if (user!=null && user.getPermissionLevel().hasAdminDashboardAccess()) return "admin-dashboard.html";
        }
        throw new HttpForbiddenException();
    }

    @GetMapping("/donate")
    public String donatePage(@CookieValue(value="launch_override",required = false) String override) {
        if (!ServerInfo.hasLaunched() && (override==null || !override.equals(ServerInfo.overridePassword))) {
            return "launch-waiting.html";
        }
        return "donate.html";
    }

    @GetMapping("/mobile-help")
    public String mobileHelpPage() {
        throw new MobileNotSupportedException();
    }
}
