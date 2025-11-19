package dev.pakn.competitioncubes;

import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

public class AntiCheat {
    public static void getWCASingle(String wcaId, Event event) {
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
                System.out.println(single/100.0);
            }
        }catch (Exception e) {
            System.out.println("Something went wrong with the request! "+e.getMessage());
            e.printStackTrace();
        }
    }

    public static void getWCAAverage(String wcaId, Event event) {
        try {
            HttpResponse<String> response = WebRequests.sendGetRequest("https://raw.githubusercontent.com/robiningelbrecht/wca-rest-api/master/api/persons/"+wcaId+".json");
            if (response.statusCode()!=HttpStatus.OK.value()) {
                throw new Exception("Got bad status code (not 200). Code: "+response.statusCode());
            }else {
                int single = -1;

                JSONObject responseJson = new JSONObject(response.body());
                JSONArray singles = responseJson.getJSONObject("rank").getJSONArray("averages");
                for (int i=0;i<singles.length();i++) {
                    String currEvent = singles.getJSONObject(i).getString("eventId");
                    if (currEvent.equals(event.getEventId())) {
                        single = singles.getJSONObject(i).getInt("best");
                        break;
                    }
                }
                if (single!=-1)
                    System.out.println(single/100.0);
            }
        }catch (Exception e) {
            System.out.println("Something went wrong with the request! "+e.getMessage());
            e.printStackTrace();
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
}
