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
import org.springframework.web.bind.annotation.RequestParam;
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

    private static HashMap<Event, String> eventDBNames = new HashMap<>();
    public static HashMap<String, Event> stringToEventMap = new HashMap<>();

    @PostConstruct
    public void init() {
        staticDbURL=dbURL;
        staticDbUsername=dbUsername;
        staticDbPassword=dbPassword;

        eventDBNames.put(Event.THREE_BY_THREE, "threestats");
        stringToEventMap.put("3x3", Event.THREE_BY_THREE);

        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);
            ResultSet userResultSet = getAllUsers();
            while (userResultSet.next()) {
                int userId = userResultSet.getInt("userid");
                String username = userResultSet.getString("username");
                Integer[] badgesArray = (Integer[]) userResultSet.getArray("badges").getArray();
                userList.put(userId, new User(userId,username,getElosByUserId(userId,conn),badgesArray,null));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @PostMapping("/api/create-user")
    public boolean createUser(@RequestBody String userDataJSON, @CookieValue(value="wca_access_token", required = false) String accessToken, @CookieValue(value="user_secret", required = false) String userSecret) {
        try {
            //connect to DB
            Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword);

            if (accessToken!=null) {
                String userWcaId = AuthController.getWCAId(accessToken);
                String username = new JSONObject(userDataJSON).getString("username");
            
                //creating sql query
                String sqlQuery = "INSERT INTO users (wcaid, username, usersecret, badges) VALUES (?, ?, ?, ?);";
                PreparedStatement statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, userWcaId);
                statement.setString(2, username);
                statement.setString(3, userSecret);
                statement.setArray(4, conn.createArrayOf("INT", new Integer[0]));

                //sending sql query
                statement.executeUpdate();

                String userIdQuery = "SELECT userid FROM users WHERE usersecret=?";
                PreparedStatement userIdStatement = conn.prepareStatement(userIdQuery);
                userIdStatement.setString(1, userSecret);
                ResultSet userIdSet = userIdStatement.executeQuery();
                int userId = -1;
                if (userIdSet.next()){
                    userId = userIdSet.getInt(1);
                }else {
                    throw new Exception("User not found!");
                }

                for (String eventDB:eventDBNames.values()) {
                    String eventSqlQuery = "INSERT INTO "+eventDB+" (userid, elo) VALUES (?, ?);";
                    PreparedStatement eventStatement = conn.prepareStatement(eventSqlQuery);
                    eventStatement.setInt(1, userId);
                    eventStatement.setInt(2, 100);

                    //sending sql query
                    eventStatement.executeUpdate();
                }
                conn.close();
                return true;
            }

            conn.close();
            return false;
        }catch (Exception e) {
            System.out.println("Failed to connect to db!");
            e.printStackTrace();
            return false;
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
                HashMap<Event, Integer> userElos = getElosByUserId(userId, conn);
                Integer[] badgesArray = (Integer[]) usersFound.getArray("badges").getArray();
                conn.close();
                return new User(userId,username,userElos,badgesArray,null);
            }else {
                conn.close();
                return null;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static User getUserByID(int userId, Connection conn) {
        try {
            //getting resultset
            ResultSet usersFound = getUserDataById(userId, conn);
            
            if (usersFound.next()) {
                String wcaId = usersFound.getString("wcaid");
                String username = usersFound.getString("username");
                HashMap<Event, Integer> userElos = getElosByUserId(userId, conn);
                Integer[] badgesArray = (Integer[]) usersFound.getArray("badges").getArray();
                return new User(userId,username,userElos,badgesArray,null);
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

    public static ResultSet getUserDataById(int userId, Connection conn) {
        try {
            //checking for userId
            String findUserSecretQuery = "SELECT * FROM users WHERE userId=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setInt(1, userId);

            //getting resultset
            ResultSet usersFound = userSecretStatement.executeQuery();

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

    public static HashMap<Event, Integer> getElosByUserId(int userId, Connection conn) {
        try {
            //creating elo map
            HashMap<Event, Integer> userElos = new HashMap<>();

            for (Event event:eventDBNames.keySet()) {
                //checking for userId
                String findUsersQuery = "SELECT elo FROM "+eventDBNames.get(event)+" WHERE userid=?";
                PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);
                usersQueryStatement.setInt(1, userId);

                //getting resultset
                ResultSet usersFound = usersQueryStatement.executeQuery();
                usersFound.next();
                userElos.put(event, usersFound.getInt(1));
            }
            return userElos;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public static boolean saveUserData(User user) {
        try {
            //connect to DB
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);
        
            //creating sql query
            String sqlQuery = "UPDATE users SET username=? WHERE userid=?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, user.getUsername());
            statement.setInt(2, user.getUserId());

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

    public static boolean saveEloForEvent(int userId, Event event, int newElo) {
        try {
            //connect to DB
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);
        
            //creating sql query
            String sqlQuery = "UPDATE "+eventDBNames.get(event)+" SET elo=? WHERE userid=?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setInt(1, newElo);
            statement.setInt(2, userId);

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

    @GetMapping("/api/get-sorted-users-by-elo/{event}")
    public ArrayList<LeaderboardEntry> getEloSortedListRequest(@PathVariable String event) {
        return getSortedUsersByEloList(stringToEventMap.get(event),100);
    }

    @GetMapping("/api/get-user-ranks/{userId}")
    public static HashMap<Event, Integer> getUserRank(@PathVariable int userId) {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);
            HashMap<Event, Integer> userRanks = new HashMap<>();
            //for (Event event: Event.values()) {
            Event event = Event.THREE_BY_THREE;
                ArrayList<LeaderboardEntry> users = getSortedUsersByEloList(event,conn);
                for (int i=1;i<=users.size();i++) {
                    LeaderboardEntry entry = users.get(i-1);
                    int entryUserId = entry.getUserId();
                    if (entryUserId==userId) {
                        int rank = i+1;
                        int dbRankCheckIndex = i-1;
                        while (dbRankCheckIndex>=0 && users.get(dbRankCheckIndex).getElo()<=entry.getElo()) {
                            dbRankCheckIndex--;
                            rank=dbRankCheckIndex+2;
                        }
                        userRanks.put(event, rank);
                    }
                }
            //}
            return userRanks;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ResultSet getSortedUsersByEloDB(Event event) {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //checking for userId
            String findUsersQuery = "SELECT * FROM "+eventDBNames.get(event)+" ORDER BY elo DESC;";
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

    public static ArrayList<LeaderboardEntry> getSortedUsersByEloList(Event event, int resultLimit) {
        try {
            ArrayList<LeaderboardEntry> eloSortedUsers = new ArrayList<>();
            ResultSet sortedUsersDB = getSortedUsersByEloDB(event);
            int usersFound = 0;
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);
            while (sortedUsersDB.next() && usersFound<=resultLimit) {
                int userId = sortedUsersDB.getInt("userid");
                String username = getUserByID(userId,conn).getUsername();
                int userElo = sortedUsersDB.getInt("elo");
                eloSortedUsers.add(new LeaderboardEntry(userId, username, event, userElo));
                usersFound++;
            }
            conn.close();
            return eloSortedUsers;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<LeaderboardEntry> getSortedUsersByEloList(Event event, Connection conn) {
        try {
            ArrayList<LeaderboardEntry> eloSortedUsers = new ArrayList<>();
            ResultSet sortedUsersDB = getSortedUsersByEloDB(event);
            while (sortedUsersDB.next()) {
                int userId = sortedUsersDB.getInt("userid");
                String username = getUserByID(userId,conn).getUsername();
                int userElo = sortedUsersDB.getInt("elo");
                eloSortedUsers.add(new LeaderboardEntry(userId, username, event, userElo));
            }
            conn.close();
            return eloSortedUsers;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
