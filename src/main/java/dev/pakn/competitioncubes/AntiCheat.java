package dev.pakn.competitioncubes;

import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

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
    }

    //3x3 ONLY
    public static boolean validateAverage(double time, double wcaAveragePb) {
        double maxPercentDiff = 0.25;
        double solveDiffPercent = -((time-wcaAveragePb)/wcaAveragePb);
        return solveDiffPercent < maxPercentDiff;
    }

    public static void addInvalidSingle(SolveData solve, String wcaSingle, String wcaAverage) {
        //TODO: calculate auto bans
        DBController.addInvalidSingle(solve, wcaSingle, wcaAverage);
    }

    public static void addInvalidAverage(User user, Event event, double avg, String wcaSingle, String wcaAverage) {
        //TODO: calculate auto bans
        DBController.addInvalidAverage(user, event, avg, wcaSingle, wcaAverage);
    }
}
