package dev.pakn.competitioncubes;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Map;

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
            //percent error calculation. uses average because it is a better tell of what single is possible
            //double yVal = (-0.425*wcaAveragePb)+wcaAveragePb;
            double maxPercentDiff = 0.425; //0.425 seems like a good max single
            double solveDiffPercent;
            if (wcaAveragePb < 56.52744) { //56.52744 is the intersect between both equations
                solveDiffPercent = -(((time) - (Math.pow(wcaAveragePb,2.0) / (maxPercentDiff*100)) - wcaAveragePb) / wcaAveragePb);
            }else {
                solveDiffPercent = -(((time) - Math.sqrt(wcaAveragePb) - wcaAveragePb) / wcaAveragePb);
            }
            return solveDiffPercent < maxPercentDiff;
        }else {
            return true;
        }
    }

    //3x3 ONLY
    public static boolean validateAverage(double time, double wcaAveragePb) {
        if (time>0) {
            double maxPercentDiff = 0.25;
            double solveDiffPercent = -((time-wcaAveragePb)/wcaAveragePb);
            return solveDiffPercent < maxPercentDiff;
        }else {
            return true;
        }
    }

    public static void addInvalidSingle(SolveData solve, String wcaSingle, String wcaAverage) {
        //TODO: calculate auto bans
        DBController.addInvalidSingle(solve, wcaSingle, wcaAverage);
    }

    public static void addInvalidAverage(User user, Event event, double avg, String wcaSingle, String wcaAverage) {
        //TODO: calculate auto bans
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

    @PostMapping("/api/report-user")
    public boolean reportUser(@RequestBody PostRequestClass.UserReport userReport) {
        try {
            DBController.addUserReport(userReport.getUserId(), userReport.getReason());
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
    public boolean warnUser(@RequestParam("id") int userId) {
        try {
            DBController.addUserWarning(userId);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @PostMapping("/api/set-user-warnings")
    public boolean warnUser(@RequestParam("id") int userId, @RequestParam("warnings") int warnings) {
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
        long expirationDate = System.currentTimeMillis()+userBan.getDuration();

        try {
            DBController.addBannedUser(userId, expirationDate);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @PostMapping("/api/unban-user")
    public boolean banUser(@RequestParam("id") int userId) {
        try {
            DBController.removeBannedUser(userId);
            return true;
        } catch (Exception e){
            return false;
        }
    }









    //Ban Timer. This is only removes the user from the database! If a user searched for a match, their ban timer will be calculated then as well
    //This method is primarily for redundancy, as if a user never searches for a match, their ban will never be removed from the database. this method will ensure they are removed from the database
    //This method also reloads the user bans directly from the database in case I added from there
    @Scheduled(fixedRate = 3600000) //1 hour
    private void unbanUsersCheck() {
        //refresh from db
        DBController.loadUserBans();

        ArrayList<UserBan> userBans = DBController.getBannedUsers();
        long currTimeMillis = System.currentTimeMillis();

        //go through all users and check if the current time is past their expiration date
        for (UserBan userBan:userBans) {
            if (currTimeMillis>userBan.getExpirationDate()) {
                //will only remove them from database!
                DBController.removeBannedUser(userBan.getUserId());
            }
        }
    }
}
