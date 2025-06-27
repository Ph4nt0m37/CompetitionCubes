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
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {
    
    @Value("${wca.app.id}")
    private String appId;

    @Value("${wca.app.secret}")
    private String appSecret;

    @GetMapping("/wca-auth")
    public RedirectView wcaAuthRedirect(HttpServletRequest request) {
        String authURL = "https://www.worldcubeassociation.org/oauth/authorize?client_id=" + appId
        + "&redirect_uri=" + request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/wca-auth/callback"
        + "&response_type=code&scope=public+profile";

        return new RedirectView(authURL);
    }

    @GetMapping("/wca-auth/callback")
    public boolean wcaAuthCallback(@RequestParam("code") String code, HttpServletRequest request) throws MalformedURLException, IOException, InterruptedException {
        //getting token through a post request
        String redirectUri = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()+"/wca-auth/callback";

        HashMap<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", appId);
        params.put("client_secret", appSecret);
        params.put("code", code);
        params.put("redirect_uri", redirectUri);

        String tokenJsonString = WebRequests.sendPostRequest("https://www.worldcubeassociation.org/oauth/token", params, redirectUri);
        JSONObject tokenJson = new JSONObject(tokenJsonString);
        

        return true;
    }
}
