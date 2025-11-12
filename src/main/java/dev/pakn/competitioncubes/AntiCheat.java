package dev.pakn.competitioncubes;

import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;

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
                    }
                }
                System.out.println(single/100.0);
            }
        }catch (Exception e) {
            System.out.println("Something went wrong with the request! "+e.getMessage());
            e.printStackTrace();
        }
    }
}
