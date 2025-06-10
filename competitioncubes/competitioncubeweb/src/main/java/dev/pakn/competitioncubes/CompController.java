package dev.pakn.competitioncubes;

import org.json.JSONObject;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class CompController {

    private static ArrayList<Integer> waitingList = new ArrayList<>();

    @PostMapping("/waiting-list")
    private void addToWaitingList(@RequestBody String userIdJSON) {
        int userId = new JSONObject(userIdJSON).getInt("userId");
        System.out.println("added "+userId+" to waiting list");
        waitingList.add(userId);
    }

    @GetMapping("/waiting-list/{userId}")
    private int getFromWaitingList(@PathVariable int userId) {
        return userId;
    }

    @DeleteMapping("/waiting-list")
    private void removeFromWaitingList(@RequestBody String userIdJSON) {
        int userId = new JSONObject(userIdJSON).getInt("userId");
        System.out.println("removed "+userId+" from waiting list");
        waitingList.remove(userId);
    }

    public static ArrayList<Integer> getWaitingList() {
        return waitingList;
    }
}
