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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
                String wcaId = userResultSet.getString("wcaid");
                int matchesWon = userResultSet.getInt("matcheswon");
                int matchesLost = userResultSet.getInt("matcheslost");
                Integer[] badgesArray = (Integer[]) userResultSet.getArray("badges").getArray();
                userList.put(userId, new User(userId,username,wcaId,getElosByUserId(userId,conn),getSinglesByUserId(userId, conn),getAveragesByUserId(userId, conn),badgesArray,matchesWon,matchesLost,null));
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
                String sqlQuery = "INSERT INTO users (wcaid, username, usersecret, matcheswon, matcheslost, badges) VALUES (?, ?, ?, ?, ?, ?);";
                PreparedStatement statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, userWcaId);
                statement.setString(2, username);
                statement.setString(3, userSecret);
                statement.setInt(4, 0);
                statement.setInt(5, 0);
                statement.setArray(6, conn.createArrayOf("INT", new Integer[]{7}));

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
                    String eventSqlQuery = "INSERT INTO "+eventDB+" (userid, elo, single, average) VALUES (?, ?, ?, ?);";
                    PreparedStatement eventStatement = conn.prepareStatement(eventSqlQuery);
                    eventStatement.setInt(1, userId);
                    eventStatement.setInt(2, 100);
                    eventStatement.setDouble(3, -1);
                    eventStatement.setDouble(4, -1);

                    //sending sql query
                    eventStatement.executeUpdate();
                }

                //adding user to userList
                userList.put(userId, new User(userId,username,userWcaId,getElosByUserId(userId,conn),getSinglesByUserId(userId, conn),getAveragesByUserId(userId, conn),new Integer[0],0,0,null));

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
                int matchesWon = usersFound.getInt("matcheswon");
                int matchesLost = usersFound.getInt("matcheslost");
                HashMap<Event, Integer> userElos = getElosByUserId(userId, conn);
                HashMap<Event, Double> userSingles = getSinglesByUserId(userId, conn);
                HashMap<Event, Double> userAverages = getAveragesByUserId(userId, conn);
                Integer[] badgesArray = (Integer[]) usersFound.getArray("badges").getArray();
                conn.close();
                return new User(userId,username,wcaId,userElos,userSingles,userAverages,badgesArray,matchesWon,matchesLost,null);
            }else {
                conn.close();
                return null;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //in theory useless since we have userList but I don't want to delete it because im scared smth will break
    public static User getUserByID(int userId, Connection conn) {
        try {
            //getting resultset
            ResultSet usersFound = getUserDataById(userId, conn);
            
            if (usersFound.next()) {
                String wcaId = usersFound.getString("wcaid");
                String username = usersFound.getString("username");
                int matchesWon = usersFound.getInt("matcheswon");
                int matchesLost = usersFound.getInt("matcheslost");
                HashMap<Event, Integer> userElos = getElosByUserId(userId, conn);
                HashMap<Event, Double> userSingles = getSinglesByUserId(userId, conn);
                HashMap<Event, Double> userAverages = getAveragesByUserId(userId, conn);
                Integer[] badgesArray = (Integer[]) usersFound.getArray("badges").getArray();
                return new User(userId,username,wcaId,userElos,userSingles,userAverages,badgesArray,matchesWon,matchesLost,null);
            }else {
                conn.close();
                return null;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static User getUserByIDList(int userId) {
        return userList.get((Integer) userId);
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

    public static HashMap<Event, Double> getSinglesByUserId(int userId, Connection conn) {
        try {
            //creating elo map
            HashMap<Event, Double> userSingles = new HashMap<>();

            for (Event event:eventDBNames.keySet()) {
                //checking for userId
                String findUsersQuery = "SELECT single FROM "+eventDBNames.get(event)+" WHERE userid=?";
                PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);
                usersQueryStatement.setInt(1, userId);

                //getting resultset
                ResultSet usersFound = usersQueryStatement.executeQuery();
                usersFound.next();
                userSingles.put(event, usersFound.getDouble(1));
            }
            return userSingles;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<Event, Double> getAveragesByUserId(int userId, Connection conn) {
        try {
            //creating elo map
            HashMap<Event, Double> userAverages = new HashMap<>();

            for (Event event:eventDBNames.keySet()) {
                //checking for userId
                String findUsersQuery = "SELECT average FROM "+eventDBNames.get(event)+" WHERE userid=?";
                PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);
                usersQueryStatement.setInt(1, userId);

                //getting resultset
                ResultSet usersFound = usersQueryStatement.executeQuery();
                usersFound.next();
                userAverages.put(event, usersFound.getDouble(1));
            }
            return userAverages;
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
            String sqlQuery = "UPDATE users SET username=?,matcheswon=?,matcheslost=? WHERE userid=?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, user.getUsername());
            statement.setInt(2, user.getMatchesWon());
            statement.setInt(3, user.getMatchesLost());
            statement.setInt(4, user.getUserId());

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

    public static boolean saveDataForEvent(int userId, Event event, int newElo, double newSingle, double newAverage) {
        try {
            //connect to DB
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);
        
            //creating sql query
            String sqlQuery = "UPDATE "+eventDBNames.get(event)+" SET elo=?, single=?, average=? WHERE userid=?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setInt(1, newElo);
            statement.setDouble(2, newSingle);
            statement.setDouble(3, newAverage);
            statement.setInt(4, userId);

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
    public static HashMap<Event, Integer> getUserRanks(@PathVariable int userId) {
        try {
            HashMap<Event, Integer> userRanks = new HashMap<>();
            //for (Event event: Event.values()) {
            Event event = Event.THREE_BY_THREE;
                ArrayList<LeaderboardEntry> users = getSortedUsersByEloList(event);
                for (int i=1;i<=users.size();i++) {
                    LeaderboardEntry entry = users.get(i-1);
                    int entryUserId = entry.getUserId();
                    if (entryUserId==userId) {
                        int rank = i+1;
                        int dbRankCheckIndex = i-1;
                        while (dbRankCheckIndex>=0 && users.get(dbRankCheckIndex).getStat()<=entry.getStat()) {
                            dbRankCheckIndex--;
                            rank=dbRankCheckIndex+2;
                        }
                        userRanks.put(event, rank);
                        break;
                    }
                }
            //}
            return userRanks;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getUserRank(int userId, Event event) {
        try {
            int userRank = -1;
            ArrayList<LeaderboardEntry> users = getSortedUsersByEloList(event);
            for (int i=1;i<=users.size();i++) {
                LeaderboardEntry entry = users.get(i-1);
                int entryUserId = entry.getUserId();
                if (entryUserId==userId) {
                    int rank = i+1;
                    int dbRankCheckIndex = i-1;
                    while (dbRankCheckIndex>=0 && users.get(dbRankCheckIndex).getStat()<=entry.getStat()) {
                        dbRankCheckIndex--;
                        rank=dbRankCheckIndex+2;
                    }
                    userRank = rank;
                    break;
                }
            }
            return userRank;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
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
            while (sortedUsersDB.next() && usersFound<=resultLimit) {
                int userId = sortedUsersDB.getInt("userid");
                String username = userList.get(userId).getUsername();
                int userElo = sortedUsersDB.getInt("elo");
                eloSortedUsers.add(new LeaderboardEntry(userId, username, event, userElo));
                usersFound++;
            }
            return eloSortedUsers;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<LeaderboardEntry> getSortedUsersByEloList(Event event) {
        try {
            ArrayList<LeaderboardEntry> eloSortedUsers = new ArrayList<>();
            ResultSet sortedUsersDB = getSortedUsersByEloDB(event);
            while (sortedUsersDB.next()) {
                int userId = sortedUsersDB.getInt("userid");
                String username = userList.get(userId).getUsername();
                int userElo = sortedUsersDB.getInt("elo");
                eloSortedUsers.add(new LeaderboardEntry(userId, username, event, userElo));
            }
            return eloSortedUsers;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/api/get-sorted-users-by-single/{event}")
    public ArrayList<LeaderboardEntry> getSingleSortedListRequest(@PathVariable String event) {
        return getSortedUsersBySingleList(stringToEventMap.get(event),100);
    }

    private static ResultSet getSortedUsersBySingleDB(Event event) {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //checking for userId
            String findUsersQuery = "SELECT * FROM "+eventDBNames.get(event)+" ORDER BY single;";
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

    public static ArrayList<LeaderboardEntry> getSortedUsersBySingleList(Event event, int resultLimit) {
        try {
            ArrayList<LeaderboardEntry> singleSortedUsers = new ArrayList<>();
            ResultSet sortedUsersDB = getSortedUsersBySingleDB(event);
            int usersFound = 0;
            while (sortedUsersDB.next() && usersFound<=resultLimit) {
                double userSingle = sortedUsersDB.getDouble("single");
                if (userSingle>=0) {
                    int userId = sortedUsersDB.getInt("userid");
                    String username = userList.get(userId).getUsername();
                    singleSortedUsers.add(new LeaderboardEntry(userId, username, event, userSingle));
                    usersFound++;
                }
            }
            return singleSortedUsers;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<LeaderboardEntry> getSortedUsersBySingleList(Event event) {
        try {
            ArrayList<LeaderboardEntry> singleSortedUsers = new ArrayList<>();
            ResultSet sortedUsersDB = getSortedUsersBySingleDB(event);
            while (sortedUsersDB.next()) {
                int userId = sortedUsersDB.getInt("userid");
                String username = userList.get(userId).getUsername();
                double userSingle = sortedUsersDB.getDouble("single");
                singleSortedUsers.add(new LeaderboardEntry(userId, username, event, userSingle));
            }
            return singleSortedUsers;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/api/get-sorted-users-by-average/{event}")
    public ArrayList<LeaderboardEntry> getSortedAverageListRequest(@PathVariable String event) {
        return getSortedUsersByAverageList(stringToEventMap.get(event),100);
    }

    private static ResultSet getSortedUsersByAverageDB(Event event) {
        try {
            //connect to DB 
            Connection conn = DriverManager.getConnection(staticDbURL, staticDbUsername, staticDbPassword);

            //checking for userId
            String findUsersQuery = "SELECT * FROM "+eventDBNames.get(event)+" ORDER BY average;";
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

    public static ArrayList<LeaderboardEntry> getSortedUsersByAverageList(Event event, int resultLimit) {
        try {
            ArrayList<LeaderboardEntry> averageSortedUsers = new ArrayList<>();
            ResultSet sortedUsersDB = getSortedUsersByAverageDB(event);
            int usersFound = 0;
            while (sortedUsersDB.next() && usersFound<=resultLimit) {
                double userAvg = sortedUsersDB.getDouble("average");
                if (userAvg>=0) {
                    int userId = sortedUsersDB.getInt("userid");
                    String username = userList.get(userId).getUsername();
                    averageSortedUsers.add(new LeaderboardEntry(userId, username, event, userAvg));
                    usersFound++;
                }
            }
            for (LeaderboardEntry entry:averageSortedUsers) {
                System.out.println(entry.getStat());
            }
            return averageSortedUsers;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<LeaderboardEntry> getSortedUsersByAverageList(Event event) {
        try {
            ArrayList<LeaderboardEntry> averageSortedUsers = new ArrayList<>();
            ResultSet sortedUsersDB = getSortedUsersByAverageDB(event);
            while (sortedUsersDB.next()) {
                int userId = sortedUsersDB.getInt("userid");
                String username = userList.get(userId).getUsername();
                double userAvg = sortedUsersDB.getDouble("average");
                averageSortedUsers.add(new LeaderboardEntry(userId, username, event, userAvg));
            }
            return averageSortedUsers;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/api/search/{query}")
    private ArrayList<SearchResult> getUsersByQuery(@PathVariable("query") String queryStr) {
        try {
            ArrayList<SearchResult> searchResults = new ArrayList<>();

            Connection conn = DriverManager.getConnection(staticDbURL,staticDbUsername,staticDbPassword);

            //search results by userId
            try {
                String idStatementString = "SELECT * FROM users WHERE userid=?";
                PreparedStatement idStatement = conn.prepareStatement(idStatementString);
                idStatement.setInt(1,Integer.parseInt(queryStr));
                ResultSet idResults = idStatement.executeQuery();

                while (idResults.next()) {
                    int userId = idResults.getInt("userid");
                    String username = idResults.getString("username");
                    String wcaId = idResults.getString("wcaid");
                    SearchResult result = new SearchResult(userId, username, wcaId);
                    searchResults.add(result);
                }
            }catch (NumberFormatException e) {
                //do nothing. this just means the search query wasn't a pure number
            }

            //search results by userId
            String wcaIdStatementString = "SELECT * FROM users WHERE wcaid=?";
            PreparedStatement wcaIdStatement = conn.prepareStatement(wcaIdStatementString);
            wcaIdStatement.setString(1, queryStr.toUpperCase());
            ResultSet wcaIdResults = wcaIdStatement.executeQuery();

            while (wcaIdResults.next()) {
                int userId = wcaIdResults.getInt("userid");
                String username = wcaIdResults.getString("username");
                String wcaId = wcaIdResults.getString("wcaid");
                SearchResult result = new SearchResult(userId, username, wcaId);
                searchResults.add(result);
            }

            //searching by levenshtein. I'm not using OR here because lvenshtein needs to be ordered by the distance - wcaid and id do not
            String levStatementString = "SELECT * FROM users WHERE levenshtein(username,?) <=3 ORDER BY levenshtein(username, ?)";
            PreparedStatement levStatement = conn.prepareStatement(levStatementString);
            levStatement.setString(1, queryStr);
            levStatement.setString(2, queryStr);
            ResultSet levResults = levStatement.executeQuery();

            while (levResults.next()) {
                int userId = levResults.getInt("userid");
                String username = levResults.getString("username");
                String wcaId = levResults.getString("wcaid");
                SearchResult result = new SearchResult(userId, username, wcaId);
                searchResults.add(result);
            }
            return searchResults;
        }catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
