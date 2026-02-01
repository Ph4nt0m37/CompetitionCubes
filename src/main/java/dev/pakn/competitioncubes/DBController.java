package dev.pakn.competitioncubes;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.sql.DataSource;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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

    /*@Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.url}")
    private String dbURL;*/

    @Autowired
    DataSource dataSource;

    static DataSource staticDataSource;

    /*private static String staticDbUsername;
    private static String staticDbPassword;
    private static String staticDbURL;*/

    private static HashMap<Integer, User> userList = new HashMap<>();
    private static HashMap<Integer, UserBan> bannedUserList = new HashMap<>();

    private static HashMap<Event, String> eventDBNames = new HashMap<>();
    public static HashMap<String, Event> stringToEventMap = new HashMap<>();

    @PostConstruct
    public void init() {
        /*staticDbURL=dbURL;
        staticDbUsername=dbUsername;
        staticDbPassword=dbPassword;*/

        staticDataSource = dataSource;

        eventDBNames.put(Event.THREE_BY_THREE, "threestats");
        stringToEventMap.put("3x3", Event.THREE_BY_THREE);

        try (Connection conn = dataSource.getConnection();) {
            loadUserBans(conn);
            //checking for userId
            String findUsersQuery = "SELECT * FROM users";
            PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);

            //getting resultset
            ResultSet userResultSet = usersQueryStatement.executeQuery();
            while (userResultSet.next()) {
                int userId = userResultSet.getInt("userid");
                String username = userResultSet.getString("username");
                String wcaId = userResultSet.getString("wcaid");
                int permLevel = userResultSet.getInt("permlevel");
                int matchesWon = userResultSet.getInt("matcheswon");
                int matchesLost = userResultSet.getInt("matcheslost");
                int strikes = userResultSet.getInt("strikes");
                int bans = userResultSet.getInt("bans");
                Integer[] badgesArray = (Integer[]) userResultSet.getArray("badges").getArray();
                User user = new User(userId,username,wcaId,permLevel,getElosByUserId(userId,conn),getSinglesByUserId(userId, conn),getAveragesByUserId(userId, conn),badgesArray,matchesWon,matchesLost,null,getPrevSinglesByUserId(userId, conn),getPrevAveragesByUserId(userId, conn), strikes, bans, getUserSettings(userId));
                user.setUserBan(bannedUserList.get(user.getUserId()));
                userList.put(userId, user);
            }

            loadUserWarnings();

            
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @PostMapping("/api/create-user")
    public boolean createUser(@RequestBody String userDataJSON, @CookieValue(value="wca_access_token", required = false) String accessToken, @CookieValue(value="user_secret", required = false) String userSecret) {
        try (Connection conn = dataSource.getConnection();) {

            if (accessToken!=null) {
                String userWcaId = AuthController.getWCAId(accessToken);
                String username = new JSONObject(userDataJSON).getString("username");
            
                //creating sql query
                String sqlQuery = "INSERT INTO users (wcaid, username, usersecret, matcheswon, matcheslost, badges, strikes, bans, permlevel) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
                PreparedStatement statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, userWcaId);
                statement.setString(2, username);
                statement.setString(3, userSecret);
                statement.setInt(4, 0);
                statement.setInt(5, 0);
                statement.setArray(6, conn.createArrayOf("INT", new Integer[0]));
                statement.setInt(7, 0);
                statement.setInt(8, 0);
                statement.setInt(9, 0);

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
                    String eventSqlQuery = "INSERT INTO "+eventDB+" (userid, elo, single, average, old_singles, old_averages) VALUES (?, ?, ?, ?, ?, ?);";
                    PreparedStatement eventStatement = conn.prepareStatement(eventSqlQuery);
                    eventStatement.setInt(1, userId);
                    eventStatement.setInt(2, 100);
                    eventStatement.setDouble(3, -1);
                    eventStatement.setDouble(4, -1);
                    eventStatement.setArray(5, conn.createArrayOf("DOUBLE PRECISION", new Double[0]));
                    eventStatement.setArray(6, conn.createArrayOf("DOUBLE PRECISION", new Double[0]));

                    //sending sql query
                    eventStatement.executeUpdate();
                }

                String settingsQuery = "INSERT INTO user_settings (id, inspection_audio, match_sounds) VALUES (?, ?, ?);";
                PreparedStatement settingsStatement = conn.prepareStatement(settingsQuery);
                settingsStatement.setInt(1, userId);
                settingsStatement.setBoolean(2, true);
                settingsStatement.setBoolean(3, true);

                settingsStatement.executeUpdate();

                //adding user to userList
                userList.put(userId, new User(userId,username,userWcaId,0,getElosByUserId(userId,conn),getSinglesByUserId(userId, conn),getAveragesByUserId(userId, conn),new Integer[0],0,0,null,getPrevSinglesByUserId(userId, conn),getPrevAveragesByUserId(userId, conn), 0, 0, new UserSettings(true, true)));
                
                return true;
            }

            
            return false;
        }catch (Exception e) {
            System.out.println("Failed to connect to db!");
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/api/get-user-data")
    public ResponseEntity<User> getUserBySecretRequest(@CookieValue(value="user_secret", required = false) String userSecret) {
        User user = getUserBySecret(userSecret);
        if (user!=null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/api/get-user-data-by-id/{userId}")
    public User getUserByUserIDRequest(@PathVariable int userId) {
        return userList.get(userId);
    }

    public static User getUserBySecret(String userSecret) {
        try (Connection conn = staticDataSource.getConnection();) {
            //getting user data
            String findUserSecretQuery = "SELECT * FROM users WHERE usersecret=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setString(1, userSecret);

            //getting resultset
            ResultSet usersFound = userSecretStatement.executeQuery();
            
            if (usersFound.next()) {
                int userId = usersFound.getInt("userid");
                return userList.get(userId);
            }else {
                
                return null;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //should only use if you want to get directly from database, as databse updates more frequently than userList
    public static User getUserByID(int userId, Connection conn) {
        try {
            //getting user by id
            String findUserSecretQuery = "SELECT * FROM users WHERE userId=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setInt(1, userId);

            //getting resultset
            ResultSet usersFound = userSecretStatement.executeQuery();
            
            if (usersFound.next()) {
                String wcaId = usersFound.getString("wcaid");
                String username = usersFound.getString("username");
                int permLevel = usersFound.getInt("permlevel");
                int matchesWon = usersFound.getInt("matcheswon");
                int matchesLost = usersFound.getInt("matcheslost");
                HashMap<Event, Integer> userElos = getElosByUserId(userId, conn);
                HashMap<Event, Double> userSingles = getSinglesByUserId(userId, conn);
                HashMap<Event, Double> userAverages = getAveragesByUserId(userId, conn);
                Integer[] badgesArray = (Integer[]) usersFound.getArray("badges").getArray();
                int strikes = usersFound.getInt("strikes");
                int bans = usersFound.getInt("bans");
                return new User(userId,username,wcaId,permLevel,userElos,userSingles,userAverages,badgesArray,matchesWon,matchesLost,null,getPrevSinglesByUserId(userId, conn),getPrevAveragesByUserId(userId, conn), strikes, bans, getUserSettings(userId));
            }else {
                
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
        try (Connection conn = staticDataSource.getConnection();) {

            //checking for userSecret
            String findUserSecretQuery = "SELECT * FROM users WHERE usersecret=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setString(1, userSecret);

            //getting resultset
            ResultSet usersFound = userSecretStatement.executeQuery();

            
            return usersFound.next();
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean userExists(int userId) {
        try (Connection conn = staticDataSource.getConnection();) {

            //checking for userSecret
            String findUserSecretQuery = "SELECT * FROM users WHERE userid=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setInt(1, userId);

            //getting resultset
            ResultSet usersFound = userSecretStatement.executeQuery();

            return usersFound.next();
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User getUserDataByWCAId(String wcaId) {
        try (Connection conn = staticDataSource.getConnection();) {

            //checking for userSecret
            String findUserSecretQuery = "SELECT * FROM users WHERE wcaid=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setString(1, wcaId);

            //getting resultset
            ResultSet usersFound = userSecretStatement.executeQuery();
            if (usersFound.next()) {
                int userId = usersFound.getInt("userid");
                String username = usersFound.getString("username");
                int permLevel = usersFound.getInt("permlevel");
                int matchesWon = usersFound.getInt("matcheswon");
                int matchesLost = usersFound.getInt("matcheslost");
                HashMap<Event, Integer> userElos = getElosByUserId(userId, conn);
                HashMap<Event, Double> userSingles = getSinglesByUserId(userId, conn);
                HashMap<Event, Double> userAverages = getAveragesByUserId(userId, conn);
                Integer[] badgesArray = (Integer[]) usersFound.getArray("badges").getArray();
                int strikes = usersFound.getInt("strikes");
                int bans = usersFound.getInt("bans");
                return new User(userId,username,wcaId,permLevel,userElos,userSingles,userAverages,badgesArray,matchesWon,matchesLost,null,getPrevSinglesByUserId(userId, conn),getPrevAveragesByUserId(userId, conn), strikes, bans, getUserSettings(userId));
            }

            return null ;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setUserSecretByWCAId(String wcaId, String newSecret) {
        try (Connection conn = staticDataSource.getConnection();) {

            //checking for userSecret
            String findUserSecretQuery = "UPDATE users SET usersecret=? WHERE wcaid=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setString(1, newSecret);
            userSecretStatement.setString(2, wcaId);

            //getting resultset
            int rowsChanged = userSecretStatement.executeUpdate();

            
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
        try (Connection conn = staticDataSource.getConnection();) {
        
            //creating sql query
            String sqlQuery = "UPDATE users SET username=?,matcheswon=?,matcheslost=?,badges=? WHERE userid=?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, user.getUsername());
            statement.setInt(2, user.getMatchesWon());
            statement.setInt(3, user.getMatchesLost());
            statement.setArray(4, conn.createArrayOf("INT",user.getBadges().toArray()));
            statement.setInt(5, user.getUserId());

            //sending sql query
            statement.executeUpdate();

            
            return true;
        }catch (Exception e) {
            System.out.println("Failed to connect to db!");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveDataForEvent(int userId, Event event, int newElo, double newSingle, double newAverage) {
        try (Connection conn = staticDataSource.getConnection();) {
        
            //creating sql query
            String sqlQuery = "UPDATE "+eventDBNames.get(event)+" SET elo=?, single=?, average=?, old_singles=?, old_averages=? WHERE userid=?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setInt(1, newElo);
            statement.setDouble(2, newSingle);
            statement.setDouble(3, newAverage);

            User user = getUserByIDList(userId);

            statement.setArray(4, conn.createArrayOf("DOUBLE PRECISION",user.getAllSinglesArray(event)));
            statement.setArray(5, conn.createArrayOf("DOUBLE PRECISION",user.getAllAveragesArray(event)));

            statement.setInt(6, userId);

            //sending sql query
            statement.executeUpdate();

            
            return true;
        }catch (Exception e) {
            System.out.println("Failed to connect to db!");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveSingleForEvent(int userId, Event event, double newSingle) {
        try (Connection conn = staticDataSource.getConnection();) {
        
            //creating sql query
            String sqlQuery = "UPDATE "+eventDBNames.get(event)+" SET single=?, old_singles=? WHERE userid=?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setDouble(1, newSingle);

            User user = getUserByIDList(userId);

            statement.setArray(2, conn.createArrayOf("DOUBLE PRECISION",user.getAllSinglesArray(event)));

            statement.setInt(3, userId);

            //sending sql query
            statement.executeUpdate();

            
            return true;
        }catch (Exception e) {
            System.out.println("Failed to connect to db!");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveAverageForEvent(int userId, Event event, double newAverage) {
        try (Connection conn = staticDataSource.getConnection();) {
        
            //creating sql query
            String sqlQuery = "UPDATE "+eventDBNames.get(event)+" SET average=?, old_averages=? WHERE userid=?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setDouble(1, newAverage);

            User user = getUserByIDList(userId);

            statement.setArray(2, conn.createArrayOf("DOUBLE PRECISION",user.getAllAveragesArray(event)));

            statement.setInt(3, userId);

            //sending sql query
            statement.executeUpdate();

            
            return true;
        }catch (Exception e) {
            System.out.println("Failed to connect to db!");
            e.printStackTrace();
            return false;
        }
    }

    private static HashMap<Event, LinkedList<Double>> getPrevSinglesByUserId(int userId, Connection conn) {
        try {
            //creating elo map
            HashMap<Event, LinkedList<Double>> userPrevSingles = new HashMap<>();

            for (Event event:eventDBNames.keySet()) {
                //checking for userId
                String findUsersQuery = "SELECT old_singles FROM "+eventDBNames.get(event)+" WHERE userid=?";
                PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);
                usersQueryStatement.setInt(1, userId);

                //getting resultset
                ResultSet usersFound = usersQueryStatement.executeQuery();
                usersFound.next();

                Array pbArr = usersFound.getArray(1);
                if (pbArr!=null) {
                    Double[] prevPbsArrays = (Double[]) pbArr.getArray();
                    LinkedList<Double> prevPbs = new LinkedList<>();
                    for (Double prevPb:prevPbsArrays) {
                        prevPbs.offerLast(prevPb);
                    }
                    userPrevSingles.put(event, prevPbs);
                }
            }
            return userPrevSingles;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static HashMap<Event, LinkedList<Double>> getPrevAveragesByUserId(int userId, Connection conn) {
        try {
            //creating elo map
            HashMap<Event, LinkedList<Double>> userPrevAverages = new HashMap<>();

            for (Event event:eventDBNames.keySet()) {
                //checking for userId
                String findUsersQuery = "SELECT old_averages FROM "+eventDBNames.get(event)+" WHERE userid=?";
                PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);
                usersQueryStatement.setInt(1, userId);

                //getting resultset
                ResultSet usersFound = usersQueryStatement.executeQuery();
                usersFound.next();

                Array pbArr = usersFound.getArray(1);
                if (pbArr!=null) {
                    Double[] prevPbsArrays = (Double[]) pbArr.getArray();
                    LinkedList<Double> prevPbs = new LinkedList<>();
                    for (Double prevPb:prevPbsArrays) {
                        prevPbs.offerLast(prevPb);
                    }

                    userPrevAverages.put(event, prevPbs);
                }
            }
            return userPrevAverages;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<User> getAllUsers(Connection conn) {
        try {
            ArrayList<User> foundUsersList = new ArrayList<>();

            //checking for userId
            String findUsersQuery = "SELECT userid FROM users";
            PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);

            //getting resultset
            ResultSet usersFound = usersQueryStatement.executeQuery();
            while (usersFound.next()) {
                foundUsersList.add(getUserByID(usersFound.getInt("userid"),conn));
            }

            return foundUsersList;
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

    @GetMapping("/api/get-user-elo-ranks/{userId}")
    public static HashMap<Event, Integer> getUserEloRanks(@PathVariable int userId) {
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

    public static int getUserEloRank(int userId, Event event) {
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

    @GetMapping("/api/get-user-single-ranks/{userId}")
    public static HashMap<Event, Integer> getUserSingleRanks(@PathVariable int userId) {
        try {
            HashMap<Event, Integer> userRanks = new HashMap<>();
            //for (Event event: Event.values()) {
            Event event = Event.THREE_BY_THREE;
                ArrayList<LeaderboardEntry> users = getSortedUsersBySingleList(event);
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

    public static int getUserSingleRank(int userId, Event event) {
        try {
            int userRank = -1;
            ArrayList<LeaderboardEntry> users = getSortedUsersBySingleList(event);
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

    @GetMapping("/api/get-user-average-ranks/{userId}")
    public static HashMap<Event, Integer> getUserAverageRanks(@PathVariable int userId) {
        try {
            HashMap<Event, Integer> userRanks = new HashMap<>();
            //for (Event event: Event.values()) {
            Event event = Event.THREE_BY_THREE;
                ArrayList<LeaderboardEntry> users = getSortedUsersByAverageList(event);
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

    public static int getUserAverageRank(int userId, Event event) {
        try {
            int userRank = -1;
            ArrayList<LeaderboardEntry> users = getSortedUsersByAverageList(event);
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

    public static ArrayList<LeaderboardEntry> getSortedUsersByEloList(Event event, int resultLimit) {
        try (Connection conn = staticDataSource.getConnection();) {
            ArrayList<LeaderboardEntry> eloSortedUsers = new ArrayList<>();
            //checking for userId
            String findUsersQuery = "SELECT * FROM "+eventDBNames.get(event)+" ORDER BY elo DESC;";
            PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);

            //getting resultset
            ResultSet sortedUsersDB = usersQueryStatement.executeQuery();
            int usersFound = 0;
            while (sortedUsersDB.next() && usersFound<=resultLimit) {
                int userId = sortedUsersDB.getInt("userid");
                String username = userList.get(userId).getUsername();
                int userElo = sortedUsersDB.getInt("elo");
                String eloString = String.valueOf(userElo);
                eloSortedUsers.add(new LeaderboardEntry(userId, username, event, userElo, eloString));
                usersFound++;
            }
            return eloSortedUsers;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<LeaderboardEntry> getSortedUsersByEloList(Event event) {
        try (Connection conn = staticDataSource.getConnection();) {
            ArrayList<LeaderboardEntry> eloSortedUsers = new ArrayList<>();
            //checking for userId
            String findUsersQuery = "SELECT * FROM "+eventDBNames.get(event)+" ORDER BY elo DESC;";
            PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);
            ResultSet sortedUsersDB = usersQueryStatement.executeQuery();
            while (sortedUsersDB.next()) {
                int userId = sortedUsersDB.getInt("userid");
                String username = userList.get(userId).getUsername();
                int userElo = sortedUsersDB.getInt("elo");
                String eloString = String.valueOf(userElo);
                eloSortedUsers.add(new LeaderboardEntry(userId, username, event, userElo, eloString));
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

    public static ArrayList<LeaderboardEntry> getSortedUsersBySingleList(Event event, int resultLimit) {
        try (Connection conn = staticDataSource.getConnection();) {
            ArrayList<LeaderboardEntry> singleSortedUsers = new ArrayList<>();

            //checking for userId
            String findUsersQuery = "SELECT * FROM "+eventDBNames.get(event)+" ORDER BY single;";
            PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);

            //getting resultset
            ResultSet sortedUsersDB = usersQueryStatement.executeQuery();
            int usersFound = 0;
            while (sortedUsersDB.next() && usersFound<=resultLimit) {
                double userSingle = sortedUsersDB.getDouble("single");
                if (userSingle>=0) {
                    int userId = sortedUsersDB.getInt("userid");
                    String username = userList.get(userId).getUsername();
                    String singleString = TimeConversions.doubleToTime(userSingle);
                    singleSortedUsers.add(new LeaderboardEntry(userId, username, event, userSingle, singleString));
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
        try (Connection conn = staticDataSource.getConnection();) {
            ArrayList<LeaderboardEntry> singleSortedUsers = new ArrayList<>();

            //checking for userId
            String findUsersQuery = "SELECT * FROM "+eventDBNames.get(event)+" ORDER BY single;";
            PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);

            //getting resultset
            ResultSet sortedUsersDB = usersQueryStatement.executeQuery();
            while (sortedUsersDB.next()) {
                int userId = sortedUsersDB.getInt("userid");
                String username = userList.get(userId).getUsername();
                double userSingle = sortedUsersDB.getDouble("single");
                String singleString = TimeConversions.doubleToTime(userSingle);
                singleSortedUsers.add(new LeaderboardEntry(userId, username, event, userSingle, singleString));
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

    public static ArrayList<LeaderboardEntry> getSortedUsersByAverageList(Event event, int resultLimit) {
        try (Connection conn = staticDataSource.getConnection();) {

            //checking for userId
            String findUsersQuery = "SELECT * FROM "+eventDBNames.get(event)+" ORDER BY average;";
            PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);

            //getting resultset
            ResultSet sortedUsersDB = usersQueryStatement.executeQuery();
            ArrayList<LeaderboardEntry> averageSortedUsers = new ArrayList<>();
            int usersFound = 0;
            while (sortedUsersDB.next() && usersFound<=resultLimit) {
                double userAvg = sortedUsersDB.getDouble("average");
                if (userAvg>=0) {
                    int userId = sortedUsersDB.getInt("userid");
                    String username = userList.get(userId).getUsername();
                    String avgString = TimeConversions.doubleToTime(userAvg);
                    averageSortedUsers.add(new LeaderboardEntry(userId, username, event, userAvg, avgString));
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
        try (Connection conn = staticDataSource.getConnection();) {

            //checking for userId
            String findUsersQuery = "SELECT * FROM "+eventDBNames.get(event)+" ORDER BY average;";
            PreparedStatement usersQueryStatement = conn.prepareStatement(findUsersQuery);

            //getting resultset
            ResultSet sortedUsersDB = usersQueryStatement.executeQuery();
            ArrayList<LeaderboardEntry> averageSortedUsers = new ArrayList<>();
            while (sortedUsersDB.next()) {
                int userId = sortedUsersDB.getInt("userid");
                String username = userList.get(userId).getUsername();
                double userAvg = sortedUsersDB.getDouble("average");
                String avgString = TimeConversions.doubleToTime(userAvg);
                averageSortedUsers.add(new LeaderboardEntry(userId, username, event, userAvg, avgString));
            }
            return averageSortedUsers;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/api/search/{query}")
    private ArrayList<SearchResult> getUsersByQuery(@PathVariable("query") String queryStr) {
        try (Connection conn = dataSource.getConnection();) {
            ArrayList<SearchResult> searchResults = new ArrayList<>();

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

    //AntiCheat methods
    public static void addInvalidSingle(SolveData solve, String wcaSingle, String wcaAverage) {
        //connect to DB
        try (Connection conn = staticDataSource.getConnection()) {
            String sqlQuery = "INSERT INTO invalid_solves (id, username, scramble, single, event, wca_single, wca_average) VALUES (?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setInt(1, solve.getUserId());
            statement.setString(2, userList.get(solve.getUserId()).getUsername());
            statement.setString(3, solve.getScramble());
            statement.setDouble(4, solve.getTimeDouble());
            statement.setString(5, solve.getEvent().getEventId());
            statement.setString(6, wcaSingle);
            statement.setString(7, wcaAverage);

            //sending sql query
            statement.executeUpdate();

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void addInvalidAverage(User user, Event event, double average, String wcaSingle, String wcaAverage) {
        //connect to DB
        try (Connection conn = staticDataSource.getConnection()) {
            String sqlQuery = "INSERT INTO invalid_solves (id, username, average, event, wca_single, wca_average) VALUES (?, ?, ?, ?, ?, ?);";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setInt(1, user.getUserId());
            statement.setString(2, user.getUsername());
            statement.setDouble(3, average);
            statement.setString(4, event.getEventId());
            statement.setString(5, wcaSingle);
            statement.setString(6, wcaAverage);

            //sending sql query
            statement.executeUpdate();

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @GetMapping("/api/get-invalid-times")
    public ArrayList<InvalidTime> getInvalidTimes() {
        try (Connection conn = dataSource.getConnection()) {
            String sqlQuerySingle = "SELECT * FROM invalid_solves WHERE NOT single IS null;";
            PreparedStatement statementSingle = conn.prepareStatement(sqlQuerySingle);

            //sending sql query
            ResultSet setSingle = statementSingle.executeQuery();

            ArrayList<InvalidTime> invalidTimes= new ArrayList<>();

            while (setSingle.next()) {
                int userId = setSingle.getInt("id");
                String username = setSingle.getString("username");
                String scramble = setSingle.getString("scramble");
                double single = setSingle.getDouble("single");
                Event event = Event.eventIdToEvent(setSingle.getString("event"));
                String wcaSingle = setSingle.getString("wca_single");
                String wcaAverage = setSingle.getString("wca_average");
                InvalidTime time = new InvalidTime(userId, username, event, scramble, single, wcaSingle, wcaAverage);
                invalidTimes.add(time);
            }

            String sqlQueryAverage = "SELECT * FROM invalid_solves WHERE NOT average IS null;";
            PreparedStatement statementAverage = conn.prepareStatement(sqlQueryAverage);

            //sending sql query
            ResultSet setAverage = statementAverage.executeQuery();

            while (setAverage.next()) {
                int userId = setAverage.getInt("id");
                String username = setAverage.getString("username");
                String scramble = setAverage.getString("scramble");
                double average = setAverage.getDouble("average");
                Event event = Event.eventIdToEvent(setAverage.getString("event"));
                String wcaSingle = setAverage.getString("wca_single");
                String wcaAverage = setAverage.getString("wca_average");
                InvalidTime time = new InvalidTime(userId, username, event, scramble, average, wcaSingle, wcaAverage);
                invalidTimes.add(time);
            }

            return invalidTimes;

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static void removeSingle(int userId, Event event, double time, String scramble) {
        try (Connection conn = staticDataSource.getConnection()) {
            String sqlQuerySingle = "DELETE FROM invalid_solves WHERE id=? AND single=? AND scramble=? AND event=?;";
            PreparedStatement statementSingle = conn.prepareStatement(sqlQuerySingle);
            statementSingle.setInt(1, userId);
            statementSingle.setDouble(2, time);
            statementSingle.setString(3, scramble);
            statementSingle.setString(4, event.getEventId());

            //sending sql update
            statementSingle.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeAverage(int userId, Event event, double time) {
        try (Connection conn = staticDataSource.getConnection()) {
            String sqlQuerySingle = "DELETE FROM invalid_solves WHERE id=? AND average=? AND event=?;";
            PreparedStatement statementSingle = conn.prepareStatement(sqlQuerySingle);
            statementSingle.setInt(1, userId);
            statementSingle.setDouble(2, time);
            statementSingle.setString(3, event.getEventId());

            //sending sql update
            statementSingle.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dnfSingle(int userId, Event event, double time, String scramble) {
        User user = getUserByIDList(userId);
        user.removeSingle(event, time);
        removeSingle(userId, event, time, scramble);
    }

    public static void dnfAverage(int userId, Event event, double time) {
        User user = getUserByIDList(userId);
        user.removeAverage(event, time);
        removeAverage(userId, event, time);
    }

    public static void refreshUser(int userId) {
        try (Connection conn = staticDataSource.getConnection();) {

            //checking for userId
            String findUsersQuery = "SELECT * FROM users WHERE userid=?";
            PreparedStatement userQueryStatement = conn.prepareStatement(findUsersQuery);
            userQueryStatement.setInt(1, userId);

            //getting resultset
            ResultSet userResultSet = userQueryStatement.executeQuery();
            userResultSet.next();

            String username = userResultSet.getString("username");
            String wcaId = userResultSet.getString("wcaid");
            int permLevel = userResultSet.getInt("permlevel");
            int matchesWon = userResultSet.getInt("matcheswon");
            int matchesLost = userResultSet.getInt("matcheslost");
            int strikes = userResultSet.getInt("strikes");
            int bans = userResultSet.getInt("bans");
            Integer[] badgesArray = (Integer[]) userResultSet.getArray("badges").getArray();
            userList.put(userId, new User(userId,username,wcaId,permLevel,getElosByUserId(userId,conn),getSinglesByUserId(userId, conn),getAveragesByUserId(userId, conn),badgesArray,matchesWon,matchesLost,null,getPrevSinglesByUserId(userId, conn),getPrevAveragesByUserId(userId, conn), strikes, bans, getUserSettings(userId)));
            
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO: Add insane rate limiting
    @GetMapping("/api/refresh-user/{userId}")
    public boolean refreshUserReq(@PathVariable int userId) {
        try (Connection conn = staticDataSource.getConnection();) {

            //checking for userId
            String findUsersQuery = "SELECT * FROM users WHERE userid=?";
            PreparedStatement userQueryStatement = conn.prepareStatement(findUsersQuery);
            userQueryStatement.setInt(1, userId);

            //getting resultset
            ResultSet userResultSet = userQueryStatement.executeQuery();
            userResultSet.next();

            String username = userResultSet.getString("username");
            String wcaId = userResultSet.getString("wcaid");
            int permLevel = userResultSet.getInt("permlevel");
            int matchesWon = userResultSet.getInt("matcheswon");
            int matchesLost = userResultSet.getInt("matcheslost");
            int strikes = userResultSet.getInt("strikes");
            int bans = userResultSet.getInt("bans");
            Integer[] badgesArray = (Integer[]) userResultSet.getArray("badges").getArray();
            userList.put(userId, new User(userId,username,wcaId,permLevel,getElosByUserId(userId,conn),getSinglesByUserId(userId, conn),getAveragesByUserId(userId, conn),badgesArray,matchesWon,matchesLost,null,getPrevSinglesByUserId(userId, conn),getPrevAveragesByUserId(userId, conn), strikes, bans, getUserSettings(userId)));

            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/api/get-reported-users")
    public ArrayList<ReportedUser> getReportedUsers() {
        try (Connection conn = dataSource.getConnection()) {
            String sqlQuerySingle = "SELECT * FROM reported_users";
            PreparedStatement statementSingle = conn.prepareStatement(sqlQuerySingle);

            //sending sql query
            ResultSet usersSet = statementSingle.executeQuery();

            ArrayList<ReportedUser> reportedUsers= new ArrayList<>();

            while (usersSet.next()) {
                int userId = usersSet.getInt("id");
                String reason = usersSet.getString("reason");
                String info = usersSet.getString("info");
                reportedUsers.add(new ReportedUser(userId, getUserByIDList(userId).getUsername() ,reason, info));
            }

            return reportedUsers;

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }


    public static void addUserReport(int userId, String reason, String info) {
        //connect to DB
        try (Connection conn = staticDataSource.getConnection()) {
            String sqlQuery = "INSERT INTO reported_users (id, reason, info) VALUES (?, ?, ?);";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setInt(1, userId);
            statement.setString(2, reason);
            statement.setString(3, info);

            //sending sql query
            statement.executeUpdate();

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void removeUserReport(ReportedUser reportedUser) {
        try (Connection conn = staticDataSource.getConnection()) {
            String sqlQuerySingle = "DELETE FROM reported_users WHERE id=? AND reason=?;";
            PreparedStatement statementSingle = conn.prepareStatement(sqlQuerySingle);
            statementSingle.setInt(1, reportedUser.getUserId());
            statementSingle.setString(2, reportedUser.getReason());

            //sending sql update
            statementSingle.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean setUserWarnings(int userId, int strikes) {
        try (Connection conn = staticDataSource.getConnection();) {
        
            //creating sql query
            String sqlQuery = "UPDATE users SET strikes=? WHERE userid=?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setInt(1, strikes);
            statement.setInt(2, userId);

            //sending sql query
            statement.executeUpdate();

            
            return true;
        }catch (Exception e) {
            System.out.println("Failed to connect to db!");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addUserWarning(int userId, long expirationDate, String reason) {
        try (Connection conn = staticDataSource.getConnection();) {
            String sqlQuery = "INSERT INTO user_warnings (id, expirationdate, reason) VALUES (?, ?, ?);";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setInt(1, userId);
            statement.setLong(2, expirationDate);
            statement.setString(3, reason);

            //sending sql query
            statement.executeUpdate();
        
            //creating sql query
            sqlQuery = "UPDATE users SET strikes=strikes+1 WHERE userid=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setInt(1, userId);

            //sending sql query
            statement.executeUpdate();

            userList.get(userId).addStrike();
            userList.get(userId).addUserWarning(new UserWarning(userId, expirationDate, reason));
            return true;
        }catch (Exception e) {
            System.out.println("Failed to connect to db!");
            e.printStackTrace();
            return false;
        }
    }

    public static void addBannedUser(int userId, long expirationDate, String reason) {
        //connect to DB
        try (Connection conn = staticDataSource.getConnection()) {
            String sqlQuery = "INSERT INTO banned_users (id, expirationdate, reason) VALUES (?, ?, ?);";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setInt(1, userId);
            statement.setLong(2, expirationDate);
            statement.setString(3, reason);

            //sending sql query
            statement.executeUpdate();

            sqlQuery = "UPDATE users SET bans=bans+1 WHERE userid=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setInt(1, userId);

            //sending sql query
            statement.executeUpdate();
 
            UserBan userBan = new UserBan(userId, expirationDate, reason);
            bannedUserList.put(userId, userBan);
            User user = userList.get(userId);
            user.addBan();
            user.setUserBan(userBan);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void removeBannedUser(int userId) {
        //connect to DB
        try (Connection conn = staticDataSource.getConnection()) {
            String sqlQuery = "DELETE FROM banned_users WHERE id=?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setInt(1, userId);

            //sending sql query
            statement.executeUpdate();

            bannedUserList.remove(userId);
            userList.get(userId).setUserBan(null);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static ArrayList<UserBan> getBannedUsers(Connection conn) {
        try {
            String sqlQuerySingle = "SELECT * FROM banned_users";
            PreparedStatement statement = conn.prepareStatement(sqlQuerySingle);

            //sending sql query
            ResultSet usersSet = statement.executeQuery();

            ArrayList<UserBan> userBans= new ArrayList<>();

            while (usersSet.next()) {
                int userId = usersSet.getInt("id");
                long expirationDate = usersSet.getLong("expirationdate");
                String reason = usersSet.getString("reason");
                userBans.add(new UserBan(userId, expirationDate, reason));
            }

            return userBans;

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<UserBan> getBannedUsers() {
        try (Connection conn = staticDataSource.getConnection()) {
            String sqlQuerySingle = "SELECT * FROM banned_users";
            PreparedStatement statement = conn.prepareStatement(sqlQuerySingle);

            //sending sql query
            ResultSet usersSet = statement.executeQuery();

            ArrayList<UserBan> userBans= new ArrayList<>();

            while (usersSet.next()) {
                int userId = usersSet.getInt("id");
                long expirationDate = usersSet.getLong("expirationdate");
                String reason = usersSet.getString("reason");
                userBans.add(new UserBan(userId, expirationDate, reason));
            }

            return userBans;

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/api/get-banned-users")
    public ArrayList<UserBan> getBannedUsersRequest() {
        return getBannedUsers();
    }

    public static UserBan getBannedUser(int userId) {
        return bannedUserList.get(userId);
    }

    private static void loadUserBans(Connection conn) {
        ArrayList<UserBan> userBans = getBannedUsers(conn);
        for (UserBan userBan:userBans) {
            bannedUserList.put(userBan.getUserId(), userBan);
        }
    }

    public static void loadUserBans() {
        try (Connection conn = staticDataSource.getConnection();) {
            loadUserBans(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadUserWarnings() {
        try (Connection conn = dataSource.getConnection()) {
            String sqlQuerySingle = "SELECT * FROM user_warnings";
            PreparedStatement statement = conn.prepareStatement(sqlQuerySingle);

            //sending sql query
            ResultSet usersSet = statement.executeQuery();

            while (usersSet.next()) {
                int userId = usersSet.getInt("id");
                long expirationDate = usersSet.getLong("expirationdate");
                String reason = usersSet.getString("reason");
                User user = userList.get(userId);
                if (user!=null)
                    user.addUserWarning(new UserWarning(userId, expirationDate, reason));
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static boolean changeUsername(int userId, String newUsername) {
        try (Connection conn = staticDataSource.getConnection()) {
            String sqlQuerySingle = "UPDATE users SET username=? WHERE userid=?;";
            PreparedStatement statement = conn.prepareStatement(sqlQuerySingle);
            statement.setString(1, newUsername);
            statement.setInt(2, userId);

            //sending sql update
            statement.executeUpdate();
            userList.get(userId).setUsername(newUsername);
            return true;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/api/rename-user")
    public static ResponseEntity<?> changeUsernameReq(@RequestParam int userId, @RequestParam String newUsername) {
        boolean success = changeUsername(userId, newUsername);
        if (success) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/api/get-user-settings/{userId}")
    public ResponseEntity<UserSettings> getUserSettingsReq(@PathVariable int userId) {
        UserSettings userSettings = getUserSettings(userId);
        if (userSettings!=null) {
            return new ResponseEntity<UserSettings>(userSettings, HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public static UserSettings getUserSettings(int userId) {
        try (Connection conn = staticDataSource.getConnection();) {
            String queryString = "SELECT * FROM user_settings WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(queryString);
            statement.setInt(1, userId);

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                boolean inspectionAudio = results.getBoolean("inspection_audio");
                boolean matchSounds = results.getBoolean("match_sounds");
                return new UserSettings(inspectionAudio, matchSounds);
            }else {
                return null;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/api/save-user-settings")
    public ResponseEntity<String> saveUserSettings(@RequestBody PostRequestClass.UserSettingsReq userSettingsReq) {
        try (Connection conn = dataSource.getConnection();) {

            //checking for userSecret
            String findUserSecretQuery = "UPDATE user_settings SET inspection_audio=?, match_sounds=? WHERE id=?";
            PreparedStatement userSecretStatement = conn.prepareStatement(findUserSecretQuery);
            userSecretStatement.setBoolean(1, userSettingsReq.isInspectionAudio());
            userSecretStatement.setBoolean(2, userSettingsReq.isMatchSounds());
            userSecretStatement.setInt(3, userSettingsReq.getUserId());

            //getting resultset
            int rowsChanged = userSecretStatement.executeUpdate();

            User user = userList.get(userSettingsReq.getUserId());
            if (user!=null) {
                user.setUserSettings(new UserSettings(userSettingsReq.isInspectionAudio(), userSettingsReq.isMatchSounds()));
            }

            if (rowsChanged>0) {
                return new ResponseEntity<>("Success",HttpStatus.OK);
            }
            return new ResponseEntity<>("No user with this userId found.",HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong.",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
