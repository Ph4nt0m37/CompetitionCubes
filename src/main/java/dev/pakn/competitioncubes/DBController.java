package dev.pakn.competitioncubes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    private static HashMap<Integer, User> userList = new HashMap<>();

    @PostConstruct
    public void init() {
        staticDbURL=dbURL;
        staticDbUsername=dbUsername;
        staticDbPassword=dbPassword;

        try {
            ResultSet userResultSet = getAllUsers();
            while (userResultSet.next()) {
                int userId = userResultSet.getInt("userid");
                String username = userResultSet.getString("username");
                int userElo = userResultSet.getInt("elo");
                userList.put(userId, new User(userId,username,userElo,null));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
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

    @GetMapping("/api/get-user-data")
    public User getUserBySecretRequest(@CookieValue(value="user_secret", required = false) String userSecret) {
        return getUserBySecret(userSecret);
    }

    @GetMapping("/api/get-user-data-by-id/{userId}")
    public User getUserByUserIDRequest(@PathVariable int userId) {
        return userList.get(userId);
    }

    public static User getUserBySecret(String userSecret) {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //getting resultset
            ResultSet usersFound = getUserDataBySecret(userSecret);
            
            if (usersFound.next()) {
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

    public static User getUserByID(int userId) {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //getting resultset
            ResultSet usersFound = getUserDataById(userId);
            
            if (usersFound.next()) {
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
            String findUserSecretQuery = "SELECT * FROM users WHERE usersecret=?";
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

    public static boolean userExists(int userId) {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //checking for userSecret
            String findUserSecretQuery = "SELECT * FROM users WHERE userid=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setInt(1, userId);

            //getting resultset
            ResultSet usersFound = userSecretStatement.executeQuery();

            conn.close();
            return usersFound.next();
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ResultSet getUserDataBySecret(String userSecret) {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //checking for userSecret
            String findUserSecretQuery = "SELECT * FROM users WHERE usersecret=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setString(1, userSecret);

            //getting resultset
            ResultSet usersFound = userSecretStatement.executeQuery();

            conn.close();
            return usersFound;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet getUserDataByWCAId(String wcaId) {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //checking for userSecret
            String findUserSecretQuery = "SELECT * FROM users WHERE wcaid=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setString(1, wcaId);

            //getting resultset
            ResultSet usersFound = userSecretStatement.executeQuery();

            conn.close();
            return usersFound;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet getUserDataById(int userId) {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //checking for userId
            String findUserSecretQuery = "SELECT * FROM users WHERE userId=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setInt(1, userId);

            //getting resultset
            ResultSet usersFound = userSecretStatement.executeQuery();

            conn.close();
            return usersFound;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setUserSecretByWCAId(String wcaId, String newSecret) {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //checking for userSecret
            String findUserSecretQuery = "UPDATE users SET usersecret=? WHERE wcaid=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setString(1, newSecret);
            userSecretStatement.setString(2, wcaId);

            //getting resultset
            int rowsChanged = userSecretStatement.executeUpdate();

            conn.close();
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    public static boolean saveUserData(User user) {
        try {
            //connect to DB
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);
        
            //creating sql query
            String sqlQuery = "UPDATE users SET username=?, elo=? WHERE userid=?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, user.getUsername());
            statement.setInt(2, user.getElo());
            statement.setInt(3, user.getUserId());

            //sending sql query
            statement.executeUpdate();

            conn.close();
            return true;
        }catch (Exception e) {
            System.out.println("Failed to connect to db!");
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet getAllUsers() {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //checking for userId
            String findUsersQuery = "SELECT * FROM users";
            PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);

            //getting resultset
            ResultSet usersFound = usersQueryStatement.executeQuery();

            conn.close();
            return usersFound;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<Integer, User> getUsers() {
        return userList;
    }

    @GetMapping("/api/get-sorted-users-by-elo")
    public ArrayList<User> getUserBySecretRequest() {
        return getSortedUsersByEloList();
    }

    private static ResultSet getSortedUsersByEloDB() {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //checking for userId
            String findUsersQuery = "SELECT * FROM users ORDER BY elo DESC;";
            PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);

            //getting resultset
            ResultSet usersFound = usersQueryStatement.executeQuery();

            conn.close();
            return usersFound;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<User> getSortedUsersByEloList() {
        ArrayList<User> eloSortedUsers = new ArrayList<>();
        ResultSet sortedUsersDB = getSortedUsersByEloDB();
        try {
            while (sortedUsersDB.next()) {
                int userId = sortedUsersDB.getInt("userid");
                String username = sortedUsersDB.getString("username");
                int userElo = sortedUsersDB.getInt("elo");
                eloSortedUsers.add(new User(userId,username,userElo,null));
            }
            return eloSortedUsers;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
