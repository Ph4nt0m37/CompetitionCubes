package dev.pakn.competitioncubes;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AntiCheat {
    public static double getWCASingle(String wcaId, Event event) {
        System.out.println("id:"+wcaId);
        if (!wcaId.isEmpty() && wcaId!=null) {
            try {
                HttpResponse<String> response = WebRequests.sendGetRequest("https://raw.githubusercontent.com/robiningelbrecht/wca-rest-api/master/api/persons/"+wcaId+".json");
                if (response.statusCode()!=HttpStatus.OK.value()) {
                    throw new Exception("Got bad status code (not 200). Code: "+response.statusCode());
                }else {
                    int single = -1;

                    JSONObject responseJson = new JSONObject(response.body());
                    JSONArray singles = responseJson.getJSONObject("rank").getJSONArray("singles");
                    for (int i=0;i<singles.length();i++) {
                        String currEvent = singles.getJSONObject(i).getString("eventId");
                        if (currEvent.equals(event.getEventId())) {
                            single = singles.getJSONObject(i).getInt("best");
                        }
                    }
                    if (single!=-1) {
                        return single/100.0;
                    }else {
                        return -1;
                    }
                }
            }catch (Exception e) {
                System.out.println("Something went wrong with the request! "+e.getMessage());
                e.printStackTrace();
                return -1;
            }
        }else {
            return -1;
        }
    }

    public static double getWCAAverage(String wcaId, Event event) {
        if (!wcaId.isEmpty() && wcaId!=null) {
            try {
                HttpResponse<String> response = WebRequests.sendGetRequest("https://raw.githubusercontent.com/robiningelbrecht/wca-rest-api/master/api/persons/"+wcaId+".json");
                if (response.statusCode()!=HttpStatus.OK.value()) {
                    throw new Exception("Got bad status code (not 200). Code: "+response.statusCode());
                }else {
                    int average = -1;

                    JSONObject responseJson = new JSONObject(response.body());
                    JSONArray averages = responseJson.getJSONObject("rank").getJSONArray("averages");
                    for (int i=0;i<averages.length();i++) {
                        String currEvent = averages.getJSONObject(i).getString("eventId");
                        if (currEvent.equals(event.getEventId())) {
                            average = averages.getJSONObject(i).getInt("best");
                        }
                    }
                    if (average!=-1) {
                        return average/100.0;
                    }else {
                        return -1;
                    }
                }
            }catch (Exception e) {
                System.out.println("Something went wrong with the request! "+e.getMessage());
                e.printStackTrace();
                return -1;
            }
        }else {
            return -1;
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
            System.out.println("Solve: "+solve.getPenalizedTime()+" | Flagged: "+solve.isFlagged()+" | Validity: "+solve.isValid());
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

            System.out.println("Average: "+average+" | Flagged: "+isFlagged+" | Validity: "+isValid);
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
    public boolean okSingle(@RequestBody PostRequestClass.DNFTime dnfTime) {
        int userId = dnfTime.getUserId();
        String event = dnfTime.getEvent();
        double time = dnfTime.getTime();
        String scramble = dnfTime.getScramble();
        try {
            DBController.removeSingle(userId, Event.valueOf(event), time, scramble);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/api/ok-average")
    public boolean okAverage(@RequestBody PostRequestClass.DNFTime dnfTime) {
        int userId = dnfTime.getUserId();
        String event = dnfTime.getEvent();
        double time = dnfTime.getTime();
        try {
            DBController.removeAverage(userId, Event.valueOf(event), time);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/api/dnf-single")
    public boolean dnfSingle(@RequestBody PostRequestClass.DNFTime dnfTime) {
        int userId = dnfTime.getUserId();
        String event = dnfTime.getEvent();
        double time = dnfTime.getTime();
        String scramble = dnfTime.getScramble();
        try {
            DBController.dnfSingle(userId, Event.valueOf(event), time, scramble);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/api/dnf-average")
    public boolean dnfAverage(@RequestBody PostRequestClass.DNFTime dnfTime) {
        int userId = dnfTime.getUserId();
        String event = dnfTime.getEvent();
        double time = dnfTime.getTime();
        try {
            DBController.dnfAverage(userId, Event.valueOf(event), time);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/api/report-solve")
    public void reportUser(@RequestParam("userId") int userId) {
        try {
            Match userMatch = DBController.getUserByIDList(userId).getCurrentMatch();
            ArrayList<SolveData> userSolves = userMatch.getUserSolves().get(userId);
            if (userSolves.size()>0) {
                userSolves.get(userSolves.size()-1).setValidity(false);
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
    public boolean reportUser(@RequestBody PostRequestClass.UserReport userReport) {
        try {
            DBController.addUserReport(userReport.getUserId(), userReport.getReason(), userReport.getInfo());
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/api/remove-user-report")
    public boolean removeUserReport(@RequestBody ReportedUser reportedUser) {
        try {
            DBController.removeUserReport(reportedUser);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/api/warn-user")
    public boolean warnUser(@RequestBody PostRequestClass.UserWarningReq userWarning) {
        int userId = userWarning.getUserId();
        long duration = userWarning.getDuration();
        String reason = userWarning.getReason();

        return warnUser(userId, reason, duration);
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

    public static boolean warnUser(int userId, String reason, long duration) {
        long expirationDate = System.currentTimeMillis()+duration;

        try {
            DBController.addUserWarning(userId, expirationDate, reason);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    @PostMapping("/api/set-user-warnings")
    public boolean setUserWarnings(@RequestParam("id") int userId, @RequestParam("warnings") int warnings) {
        try {
            DBController.setUserWarnings(userId, warnings);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @PostMapping("/api/ban-user")
    public boolean banUser(@RequestBody PostRequestClass.UserBan userBan) {
        int userId = userBan.getUserId();
        long duration = userBan.getDuration();
        String reason = userBan.getReason();

        return banUser(userId, duration, reason);
    }

    public static boolean banUser(int userId, long duration, String reason) {
        long expirationDate = System.currentTimeMillis()+duration;

        try {
            if (duration<0) {
                DBController.addBannedUser(userId, -1, reason);
            }else {
                DBController.addBannedUser(userId, expirationDate, reason);
            }
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @PostMapping("/api/unban-user")
    public boolean unbanUser(@RequestParam("id") int userId) {
        try {
            DBController.removeBannedUser(userId);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @PostMapping("/api/rename-user-random")
    public boolean renameUserRandom(@RequestParam("id") int userId) {
        String[] adjectives = {"Fast","Speedy","Average","Smooth","Dry","Sandy","Tough","Smart","Nimble","Goofy","Focused","Ready","Broken","Bent","Chipped"};
        String[] nouns = {"Cube","Plastic","DNF","Corner","Edge","Center","Screw","Timer","Mat","Cover","Puzzle","Judge","Winner","Podium"};
        Random rand = new Random();
        String generatedUsername = adjectives[rand.nextInt(adjectives.length)] + nouns[rand.nextInt(nouns.length)] + String.format("%02d",rand.nextInt(99)+1);
        System.out.println(generatedUsername);
        return DBController.changeUsername(userId, generatedUsername);
    }









    //Checks durations for warnings and bans
    //This method is primarily for redundancy, as if a user never searches for a match, their ban/warning will never be removed from the database. this method will ensure they are removed from the database
    //This method also reloads the user bans directly from the database in case I added from there
    @Scheduled(fixedRate = 60000) //1 hour
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
            for (UserWarning warning:DBController.getUserByIDList(userId).getUserWarnings()) {
                if (warning.getExpirationDate()>=0 && currTimeMillis>warning.getExpirationDate()) {
                    //will only remove them from database!
                    DBController.removeUserWarning(userId, warning);
                }
            }
        }
    }
}
