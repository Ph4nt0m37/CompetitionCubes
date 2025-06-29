package dev.pakn.competitioncubes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;

@RestController
public class DBController {

    @Value("${postgres.username}")
    private String dbUsername;

    @Value("${postgres.password}")
    private String dbPassword;

    @Value("${postgres.url}")
    private String dbURL;

    private static String staticDbUsername;
    private static String staticDbPassword;
    private static String staticDbURL;

    @PostConstruct
    public void init() {
        staticDbURL=dbURL;
        staticDbUsername=dbUsername;
        staticDbPassword=dbPassword;
    }
    
    @PostMapping("/api/create-user")
    public void createUser(@RequestBody String userDataJSON, @CookieValue(value="wca_access_token", required = false) String accessToken, @CookieValue(value="user_secret", required = false) String userSecret) {
        try {
            //connect to DB
            Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword);

            if (accessToken!=null) {
                String userWcaId = AuthController.getWCAId(accessToken);
                String username = new JSONObject(userDataJSON).getString("username");
            
                //creating sql query
                String sqlQuery = "INSERT INTO users (wcaid, username, elo, usersecret) VALUES (?, ?, ?, ?);";
                PreparedStatement statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, userWcaId);
                statement.setString(2, username);
                statement.setInt(3, 100);
                statement.setString(4, userSecret);

                //sending sql query
                statement.executeUpdate();
            }

            conn.close();
        }catch (Exception e) {
            System.out.println("Failed to connect to db!");
            e.printStackTrace();
        }
    }

    public static User getUserBySecret(String userSecret) {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //checking for userSecret
            String findUserSecretQuery = "SELECT wcaid FROM users WHERE usersecret=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setString(1, userSecret);

            //getting resultset
            ResultSet usersFound = userSecretStatement.executeQuery();
            
            if (usersFound.next()) {
                System.out.println("WCA ID: "+usersFound.getString("wcaid"));
                int userId = usersFound.getInt("userid");
                String wcaId = usersFound.getString("wcaid");
                String username = usersFound.getString("username");
                int userElo = usersFound.getInt("elo");
                conn.close();
                return new User(userId,username,userElo,null);
            }else {
                conn.close();
                return null;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean userExists(String userSecret) {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //checking for userSecret
            String findUserSecretQuery = "SELECT wcaid FROM users WHERE usersecret=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setString(1, userSecret);

            //getting resultset
            ResultSet usersFound = userSecretStatement.executeQuery();

            conn.close();
            return usersFound.next();
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
