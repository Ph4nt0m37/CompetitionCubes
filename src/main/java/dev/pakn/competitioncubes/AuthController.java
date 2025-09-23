package dev.pakn.competitioncubes;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AuthController {
    
    @Value("${wca.app.id}")
    private String appId;

    @Value("${wca.app.secret}")
    private String appSecret;

    @GetMapping("/wca-auth")
    public RedirectView wcaAuthRedirect(HttpServletRequest request) {
        /*String authURL = "https://www.worldcubeassociation.org/oauth/authorize?client_id=" + appId
        + "&redirect_uri=" + request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/wca-auth/callback"
        + "&response_type=code&scope=public";*/

        /*String authURL = "https://www.worldcubeassociation.org/oauth/authorize?client_id=" + appId
        + "&redirect_uri=" + request.getScheme()+"://"+request.getServerName()+"/wca-auth/callback"
        + "&response_type=code&scope=public";*/

        String authURL = "https://www.worldcubeassociation.org/oauth/authorize?client_id=" + appId
        + "&redirect_uri=https://compcube.pakn.dev/wca-auth/callback"
        + "&response_type=code&scope=public";

        return new RedirectView(authURL);
    }

    @GetMapping("/wca-auth/callback")
    public RedirectView wcaAuthCallback(@RequestParam("code") String code, HttpServletRequest request, HttpServletResponse response) throws MalformedURLException, IOException, InterruptedException {
        String finalRedirectURL = "/create-account";
        
        //getting token through a post request
        //String redirectUri = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()+"/wca-auth/callback";
        //String redirectUri = request.getScheme() + "://" + request.getServerName() + "/wca-auth/callback";
        String redirectUri = "https://compcube.pakn.dev/wca-auth/callback";

        HashMap<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", appId);
        params.put("client_secret", appSecret);
        params.put("code", code);
        params.put("redirect_uri", redirectUri);

        try {
            String tokenJsonString = WebRequests.sendPostRequest("https://www.worldcubeassociation.org/oauth/token", params);
            JSONObject tokenJson = new JSONObject(tokenJsonString);

            String accessToken = tokenJson.getString("access_token");

            SecureRandom secureRandom = new SecureRandom();
            byte[] tokenBytes = new byte[32]; // 256 bits = plenty of entropy (fr fr)
            secureRandom.nextBytes(tokenBytes);
            String userSecret = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
            Cookie secretCookie = new Cookie("user_secret", userSecret);
            secretCookie.setSecure(true);
            secretCookie.setHttpOnly(true);
            secretCookie.setMaxAge(604800);
            secretCookie.setPath("/");
            response.addCookie(secretCookie);

            String userWcaId = AuthController.getWCAId(accessToken);
            if (userWcaId!=null && DBController.getUserDataByWCAId(userWcaId).next()) {
                DBController.setUserSecretByWCAId(userWcaId, userSecret);
                finalRedirectURL="/";
            }else {
                //saving access token as cookie for account creation
                Cookie tokenCookie = new Cookie("wca_access_token",accessToken);
                tokenCookie.setSecure(true);
                tokenCookie.setHttpOnly(true);
                tokenCookie.setPath("/");
                tokenCookie.setMaxAge(3600);
                response.addCookie(tokenCookie);
            }
        
        }catch (Exception e) {
            e.printStackTrace();
        }

        return new RedirectView(finalRedirectURL);
    }

    @DeleteMapping("/wca-auth/sign-out")
    public void wcaSignOut(HttpServletRequest request, HttpServletResponse response) {
        Cookie userSecretCookie = WebUtils.getCookie(request, "user_secret");
        userSecretCookie.setMaxAge(0);
        userSecretCookie.setSecure(true);
        userSecretCookie.setHttpOnly(true);
        userSecretCookie.setPath("/");

        response.addCookie(userSecretCookie);
    }

    public static String getWCAId(String accessToken) {
        try {
            //getting me data, including wca id
            String meJsonString = WebRequests.sendGetRequest("https://www.worldcubeassociation.org/api/v0/me", accessToken);
            JSONObject meJson = new JSONObject(meJsonString);
            String userWcaId = meJson.getJSONObject("me").getString("wca_id");
            return userWcaId;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
