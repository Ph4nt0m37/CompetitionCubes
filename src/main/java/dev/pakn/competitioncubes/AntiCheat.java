package dev.pakn.competitioncubes;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AntiCheat {
    private static Logger logger = LoggerFactory.getLogger(AntiCheat.class);

    public static double getWCASingle(String wcaId, Event event) {
        if (!wcaId.isEmpty() && wcaId!=null) {
            try {
                HttpResponse<String> response = WebRequests.sendGetRequest("https://www.worldcubeassociation.org/api/v0/persons/"+wcaId);
                if (response.statusCode()!=HttpStatus.OK.value()) {
                    throw new Exception("Got bad status code (not 200). Code: "+response.statusCode());
                }else {
                    int single = -1;

                    JSONObject responseJson = new JSONObject(response.body());
                    JSONObject eventJson = responseJson.getJSONObject("personal_records").getJSONObject(event.getEventId());
                    if (eventJson!=null) {
                        single = eventJson.getJSONObject("single").getInt("best");
                    }else {
                        return -1;
                    }
                    if (single!=-1) {
                        return single/100.0;
                    }else {
                        return -1;
                    }
                }
            }catch (Exception e) {
                logger.error("Something went wrong with the request! ",e);
                return -1;
            }
        }else {
            return -1;
        }
    }

    public static double getWCAAverage(String wcaId, Event event) {
        if (!wcaId.isEmpty() && wcaId!=null) {
            try {
                HttpResponse<String> response = WebRequests.sendGetRequest("https://www.worldcubeassociation.org/api/v0/persons/"+wcaId);
                if (response.statusCode()!=HttpStatus.OK.value()) {
                    throw new Exception("Got bad status code (not 200). Code: "+response.statusCode());
                }else {
                    int average = -1;

                    JSONObject responseJson = new JSONObject(response.body());
                    JSONObject eventJson = responseJson.getJSONObject("personal_records").getJSONObject(event.getEventId());
                    if (eventJson!=null) {
                        average = eventJson.getJSONObject("average").getInt("best");
                    }else {
                        return -1;
                    }
                    if (average!=-1) {
                        return average/100.0;
                    }else {
                        return -1;
                    }
                }
            }catch (Exception e) {
                logger.error("Something went wrong with the request! ",e);
                return -1;
            }
        }else {
            return -1;
        }
    }

    //saves 1 GET request
    public static double[] getWCAPbs(String wcaId, Event event) {
        double[] pbs = new double[2];
        if (!wcaId.isEmpty() && wcaId!=null) {
            try {
                HttpResponse<String> response = WebRequests.sendGetRequest("https://www.worldcubeassociation.org/api/v0/persons/"+wcaId);
                if (response.statusCode()!=HttpStatus.OK.value()) {
                    throw new Exception("Got bad status code (not 200). Code: "+response.statusCode());
                }else {
                    int single = -1;
                    int average = -1;

                    JSONObject responseJson = new JSONObject(response.body());
                    JSONObject eventJson = responseJson.getJSONObject("personal_records").getJSONObject(event.getEventId());
                    if (eventJson!=null) {
                        single = eventJson.getJSONObject("single").getInt("best");
                        average = eventJson.getJSONObject("average").getInt("best");
                    }else {
                        return new double[]{-1,-1};
                    }

                    //getting single pb
                    if (single!=-1) {
                        pbs[0] = single/100.0;
                    }else {
                        pbs[0] = -1;
                    }

                    //getting average pb
                    if (average!=-1) {
                        pbs[1] = average/100.0;
                    }else {
                        pbs[1] = -1;
                    }

                    return pbs;
                }
            }catch (Exception e) {
                logger.error("Something went wrong with the request! ",e);
                return new double[]{-1,-1};
            }
        }else {
            return new double[]{-1,-1};
        }
    }

    //3x3 ONLY. need to fix equation because it breaks down below 2s average
    public static boolean validateSolve(double time, double wcaAveragePb) {
        if (time>0) {
            double invalidTime = Math.pow(wcaAveragePb, 1.015)-(Math.pow(wcaAveragePb, 1.03)/2.0);
            return time > invalidTime;
        }else {
            return true;
        }
    }

    //3x3 ONLY. need to fix equation because it breaks down below 2s average
    public static boolean validateSolve(SolveData solve, double wcaAveragePb, double wcaSinglePb) {
        double time = solve.getPenalizedTime();
        if (time>0) {
            double flaggedTime = Math.pow(wcaAveragePb, 1.05)-(Math.pow(wcaAveragePb, 1.03)/2.0);
            double invalidTime = Math.pow(wcaAveragePb, 1.015)-(Math.pow(wcaAveragePb, 1.03)/2.0);
            boolean isFlagged = solve.getPenalizedTime()<flaggedTime;
            solve.setFlagged(isFlagged);
            boolean isValid = solve.getPenalizedTime()>invalidTime;
            solve.setValidity(isValid);
            if (!isValid) {
                solve.setPenalty(Penalty.DNF);
                warnUser(solve.getUserId(),"Potentially Invalid Solve");
            }
            logger.debug("Solve: "+solve.getPenalizedTime()+" | Flagged: "+solve.isFlagged()+" | Validity: "+solve.isValid());
            if (isFlagged) AntiCheat.addInvalidSingle(solve, TimeConversions.doubleToTime(wcaSinglePb), TimeConversions.doubleToTime(wcaAveragePb));
            return isValid;
        }else {
            return true;
        }
    }

    //3x3 ONLY
    public static boolean validateAverage(double average, double wcaAveragePb) {
        if (average>0) {
            double invalidTime = Math.pow(wcaAveragePb, 1.015)-(Math.pow(wcaAveragePb, 1.03)/2.75);
            return average > invalidTime;
        }else {
            return true;
        }
    }

    //3x3 ONLY
    public static boolean validateAverage(User user, Event event, double average, double wcaAveragePb, double wcaSinglePb) {
        if (average>0) {
            double flaggedTime = Math.pow(wcaAveragePb, 1.05)-(Math.pow(wcaAveragePb, 1.03)/2.75);
            double invalidTime = Math.pow(wcaAveragePb, 1.015)-(Math.pow(wcaAveragePb, 1.03)/2.75);
            boolean isFlagged = average < flaggedTime;
            boolean isValid = average > invalidTime;
            if (!isValid)
                warnUser(user.getUserId(),"Potentially Invalid Average");

            logger.debug("Average: "+average+" | Flagged: "+isFlagged+" | Validity: "+isValid);
            if (isFlagged) AntiCheat.addInvalidAverage(user, event, average, TimeConversions.doubleToTime(wcaSinglePb), TimeConversions.doubleToTime(wcaAveragePb));
            return isValid;
        }else {
            return true;
        }
    }

    public static void addInvalidSingle(SolveData solve, String wcaSingle, String wcaAverage) {
        DBController.addInvalidSingle(solve, wcaSingle, wcaAverage);
    }

    public static void addInvalidAverage(User user, Event event, double avg, String wcaSingle, String wcaAverage) {
        DBController.addInvalidAverage(user, event, avg, wcaSingle, wcaAverage);
    }

    @PostMapping("/api/ok-single")
    public ResponseEntity<String> okSingle(@AuthenticationPrincipal User admin, @RequestBody PostRequestClass.DNFTime dnfTime) {
        int userId = dnfTime.getUserId();
        String event = dnfTime.getEvent();
        double time = dnfTime.getTime();
        String scramble = dnfTime.getScramble();
        try {
            PermissionLevel adminPermLevel = admin.getPermissionLevel();
            if (adminPermLevel!=PermissionLevel.OWNER && (!adminPermLevel.hasAdminDashboardAccess() || userId==admin.getUserId())) {
                return new ResponseEntity<>("You are not allowed to do this.",HttpStatus.FORBIDDEN);
            }
            DBController.removeSingle(userId, Event.valueOf(event), time, scramble);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/ok-average")
    public ResponseEntity<String> okAverage(@AuthenticationPrincipal User admin, @RequestBody PostRequestClass.DNFTime dnfTime) {
        int userId = dnfTime.getUserId();
        String event = dnfTime.getEvent();
        double time = dnfTime.getTime();
        try {
            PermissionLevel adminPermLevel = admin.getPermissionLevel();
            if (adminPermLevel!=PermissionLevel.OWNER && (!adminPermLevel.hasAdminDashboardAccess() || userId==admin.getUserId())) {
                return new ResponseEntity<>("You are not allowed to do this.",HttpStatus.FORBIDDEN);
            }
            DBController.removeAverage(userId, Event.valueOf(event), time);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/dnf-single")
    public ResponseEntity<String> dnfSingle(@AuthenticationPrincipal User admin, @RequestBody PostRequestClass.DNFTime dnfTime) {
        int userId = dnfTime.getUserId();
        String event = dnfTime.getEvent();
        double time = dnfTime.getTime();
        String scramble = dnfTime.getScramble();
        try {
            PermissionLevel adminPermLevel = admin.getPermissionLevel();
            if (adminPermLevel!=PermissionLevel.OWNER && (!adminPermLevel.hasAdminDashboardAccess() || userId==admin.getUserId())) {
                return new ResponseEntity<>("You are not allowed to do this.",HttpStatus.FORBIDDEN);
            }
            DBController.dnfSingle(userId, Event.valueOf(event), time, scramble);
            //as of now, dnfing a single won't warn the user since the anticheat has likely already caught it
            //warnUser(userId, "You have been warned for a suspicious solve.");
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/dnf-average")
    public ResponseEntity<String> dnfAverage(@AuthenticationPrincipal User admin, @RequestBody PostRequestClass.DNFTime dnfTime) {
        int userId = dnfTime.getUserId();
        String event = dnfTime.getEvent();
        double time = dnfTime.getTime();
        try {
            PermissionLevel adminPermLevel = admin.getPermissionLevel();
            if (adminPermLevel!=PermissionLevel.OWNER && (!adminPermLevel.hasAdminDashboardAccess() || userId==admin.getUserId())) {
                return new ResponseEntity<>("You are not allowed to do this.",HttpStatus.FORBIDDEN);
            }
            DBController.dnfAverage(userId, Event.valueOf(event), time);
            //as of now, dnfing an average won't warn the user since the anticheat has likely already caught it
            //warnUser(userId, "Potentially Invalid Previous Average");
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/report-solve")
    public void reportUser(@RequestParam int userId) {
        try {
            Match userMatch = DBController.getUserByIDList(userId).getCurrentMatch();
            ArrayList<SolveData> userSolves = userMatch.getUserSolves().get(userId);
            if (userSolves.size()>0) {
                userSolves.get(userSolves.size()-1).setFlagged(true);
                addInvalidSingle(userSolves.get(userSolves.size()-1), TimeConversions.doubleToTime(userMatch.getUserWcaPbSingle(userId)), TimeConversions.doubleToTime(userMatch.getUserWcaPbAvg(userId)));
            }else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/api/report-user")
    public ResponseEntity<String> reportUser(@RequestBody PostRequestClass.UserReport userReport) {
        try {
            logger.info("report received: "+userReport.toString());
            DBController.addUserReport(userReport.getUserId(), userReport.getReason(), userReport.getInfo());
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/remove-user-report")
    public ResponseEntity<String> removeUserReport(@AuthenticationPrincipal User admin, @RequestBody ReportedUser reportedUser) {
        try {
            PermissionLevel adminPermLevel = admin.getPermissionLevel();
            if (adminPermLevel!=PermissionLevel.OWNER && (!adminPermLevel.hasAdminDashboardAccess() || reportedUser.getUserId()==admin.getUserId())) {
                return new ResponseEntity<>("You are not allowed to do this.",HttpStatus.FORBIDDEN);
            }
            DBController.removeUserReport(reportedUser);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/warn-user")
    public ResponseEntity<String> warnUser(@AuthenticationPrincipal User admin, @RequestBody PostRequestClass.UserWarningReq userWarning) {
        int userId = userWarning.getUserId();
        long duration = userWarning.getDuration();
        String reason = userWarning.getReason();

        return warnUser(admin, userId, reason, duration);
    }

    public static boolean warnUser(int userId, String reason) {
        //for now only default is one month. I don't forsee needing to change this
        long expirationDate = System.currentTimeMillis()+2629800000l;

        try {
            DBController.addUserWarning(userId, expirationDate, reason);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static ResponseEntity<String> warnUser(User admin, int userId, String reason, long duration) {
        long expirationDate = System.currentTimeMillis()+duration;

        try {
            PermissionLevel adminPermLevel = admin.getPermissionLevel();
            PermissionLevel userPermLevel = DBController.getUserByIDList(userId).getPermissionLevel();
            if (adminPermLevel!=PermissionLevel.OWNER && (!adminPermLevel.hasBanAccess() || userPermLevel.getPermissionValue()>=adminPermLevel.getPermissionValue() || userId==admin.getUserId())) {
                return new ResponseEntity<>("You are not allowed to warn this user.",HttpStatus.FORBIDDEN);
            }
            DBController.addUserWarning(userId, expirationDate, reason);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/api/set-user-warnings")
    public ResponseEntity<String> setUserWarnings(@AuthenticationPrincipal User admin, @RequestBody PostRequestClass.SetUserWarningsReq userWarnReq) {
        try {
            PermissionLevel adminPermLevel = admin.getPermissionLevel();
            PermissionLevel userPermLevel = DBController.getUserByIDList(userWarnReq.getWarnedId()).getPermissionLevel();
            if (adminPermLevel!=PermissionLevel.OWNER && (!adminPermLevel.hasBanAccess() || userPermLevel.getPermissionValue()>=adminPermLevel.getPermissionValue() || userWarnReq.getWarnedId()==admin.getUserId())) {
                return new ResponseEntity<>("You are not allowed to change this user's warnings.",HttpStatus.FORBIDDEN);
            }
            DBController.setUserWarnings(userWarnReq.getWarnedId(), userWarnReq.getWarnings());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/ban-user")
    public ResponseEntity<String> banUser(@AuthenticationPrincipal User admin, @RequestBody PostRequestClass.UserBan userBan) {
        int bannedId = userBan.getUserId();
        long duration = userBan.getDuration();
        String reason = userBan.getReason();

        return banUser(admin, bannedId, duration, reason);
    }

    public static ResponseEntity<String> banUser(User admin, int bannedId, long duration, String reason) {
        long expirationDate = System.currentTimeMillis()+duration;

        try {
            PermissionLevel adminPermLevel = admin.getPermissionLevel();
            PermissionLevel bannedPermLevel = DBController.getUserByIDList(bannedId).getPermissionLevel();
            if (adminPermLevel!=PermissionLevel.OWNER && (!adminPermLevel.hasBanAccess() || bannedPermLevel.getPermissionValue()>=adminPermLevel.getPermissionValue() || bannedId==admin.getUserId())) {
                return new ResponseEntity<>("You are not allowed to ban this user.",HttpStatus.FORBIDDEN);
            }
            if (duration<0) {
                DBController.addBannedUser(bannedId, -1, reason);
            }else {
                DBController.addBannedUser(bannedId, expirationDate, reason);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static ResponseEntity<String> banUser(int bannedId, long duration, String reason) {
        long expirationDate = System.currentTimeMillis()+duration;

        try {
            if (duration<0) {
                DBController.addBannedUser(bannedId, -1, reason);
            }else {
                DBController.addBannedUser(bannedId, expirationDate, reason);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/unban-user")
    public ResponseEntity<String> unbanUser(@AuthenticationPrincipal User admin, @RequestParam("id") int userId) {
        try {
            PermissionLevel adminPermLevel = admin.getPermissionLevel();
            PermissionLevel unBannedPermLevel = DBController.getUserByIDList(userId).getPermissionLevel();
            if (adminPermLevel!=PermissionLevel.OWNER && (!adminPermLevel.hasBanAccess() || unBannedPermLevel.getPermissionValue()>=adminPermLevel.getPermissionValue() || userId==admin.getUserId())) {
                return new ResponseEntity<>("You are not allowed to unban this user.",HttpStatus.FORBIDDEN);
            }
            DBController.removeBannedUser(userId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/rename-user-random")
    public ResponseEntity<String> renameUserRandom(@AuthenticationPrincipal User admin, @RequestParam("id") int userId) {
        try {
            //if getUserByIDList returns null, it ok since it will be caught!
            PermissionLevel adminPermLevel = admin.getPermissionLevel();
            PermissionLevel actionedPermLevel = DBController.getUserByIDList(userId).getPermissionLevel();
            if (adminPermLevel!=PermissionLevel.OWNER && (!adminPermLevel.hasRenameUserAccess() || actionedPermLevel.getPermissionValue()>adminPermLevel.getPermissionValue() || userId==admin.getUserId())) {
                return new ResponseEntity<>("You are not allowed to rename this user.", HttpStatus.FORBIDDEN);
            }
            String[] adjectives = {"Fast","Speedy","Average","Smooth","Dry","Sandy","Tough","Smart","Nimble","Goofy","Focused","Ready","Broken","Bent","Chipped"};
            String[] nouns = {"Cube","Plastic","DNF","Corner","Edge","Center","Screw","Timer","Mat","Cover","Puzzle","Judge","Winner","Podium"};
            Random rand = new Random();
            String generatedUsername = adjectives[rand.nextInt(adjectives.length)] + nouns[rand.nextInt(nouns.length)] + String.format("%02d",rand.nextInt(99)+1);
            boolean success =  DBController.changeUsername(userId, generatedUsername);
            if (success) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }









    //Checks durations for warnings and bans
    //This method is primarily for redundancy, as if a user never searches for a match, their ban/warning will never be removed from the database. this method will ensure they are removed from the database
    //This method also reloads the user bans directly from the database in case I added from there
    //This method also has an initial delay of 1 second to ensure the database can finish its init() function
    @Scheduled(initialDelay = 1000, fixedRate = 3600000) //1 hour
    private void checkDurations() {
        //refresh from db
        DBController.loadUserBans();

        ArrayList<UserBan> userBans = DBController.getBannedUsers();
        long currTimeMillis = System.currentTimeMillis();

        //go through all users bans and check if the current time is past their expiration date
        for (UserBan userBan:userBans) {
            if (userBan.getExpirationDate()>=0 && currTimeMillis>userBan.getExpirationDate()) {
                DBController.removeBannedUser(userBan.getUserId());
            }
        }

        //idk why i did the bans differently than this, but whatever
        //O(n^2), but users can only really have 5 active warnings until they're permanently banned, so worst case is 5n
        for (int userId:DBController.getUsers().keySet()) {
            ArrayList<UserWarning> userWarnings = DBController.getUserByIDList(userId).getUserWarnings();
            for (UserWarning warning:userWarnings) {
                if (warning.getExpirationDate()>=0 && currTimeMillis>warning.getExpirationDate()) {
                    //will only remove them from database!
                    DBController.removeUserWarning(userId, warning);
                }
            }
        }
    }
}
