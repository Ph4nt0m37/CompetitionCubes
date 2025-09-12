package dev.pakn.competitioncubes;

import org.json.JSONObject;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class CompController {

    private static ArrayList<WaitlistRequest> waitingList = new ArrayList<>();

    @PostMapping("/waiting-list")
    private boolean addToWaitingList(@RequestBody String userIdJSON) {
        JSONObject requestJson = new JSONObject(userIdJSON);
        int userId = requestJson.getInt("userId");
        String event = requestJson.getString("event");
        System.out.println("added "+userId+" to waiting list");
        Match userMatch = MatchController.getCurrentUserMatch(userId);
        if (userMatch!=null) {
            return false;
        }
        for (WaitlistRequest req:waitingList) {
            if (req.getUserId()==userId) {
                return false;
            }
        }
        waitingList.add(new WaitlistRequest(userId, event));
        return true;
    }

    @GetMapping("/waiting-list/{userId}")
    private int getFromWaitingList(@PathVariable int userId) {
        return userId;
    }

    @DeleteMapping("/waiting-list")
    private void removeFromWaitingListReq(@RequestBody String userIdJSON) {
        int userId = new JSONObject(userIdJSON).getInt("userId");
        System.out.println("removed "+userId+" from waiting list");
        removeFromWaitingList(userId);
    }

    public static boolean removeFromWaitingList(int userId) {
        boolean removed = false;
        for (int i=0;i<waitingList.size();i++) {
            if (waitingList.get(i).getUserId()==userId) {
                waitingList.remove(i);
                removed = true;
            }
        }
        return removed;
    }

    public static ArrayList<WaitlistRequest> getWaitingList() {
        return waitingList;
    }
}
