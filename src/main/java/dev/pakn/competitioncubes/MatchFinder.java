package dev.pakn.competitioncubes;

import org.json.JSONObject;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class MatchFinder {

    private static ArrayList<WaitlistRequest> waitingList = new ArrayList<>();

    @PostMapping("/waiting-list")
    private WaitlistResult addToWaitingList(@RequestBody String userIdJSON) {
        try {
            JSONObject requestJson = new JSONObject(userIdJSON);
            int userId = requestJson.getInt("userId");
            String event = requestJson.getString("event");

            //check if the user is banned
            UserBan userBan = DBController.getBannedUser(userId);
            if (userBan != null) {
                if (userBan.getExpirationDate()<0) return new WaitlistResult(WaitlistCode.BANNED_PERMANENTLY, userBan.getExpirationDate());
                if (System.currentTimeMillis()>userBan.getExpirationDate()) {
                    //remove user from database and continue
                    DBController.removeBannedUser(userBan.getUserId());
                }else {
                    return new WaitlistResult(WaitlistCode.BANNED, userBan.getExpirationDate());
                }
            }

            if (AntiCheat.getWCAAverage(DBController.getUserByIDList(userId).getWcaId(), Event.eventIdToEvent(event))<0) {
                return new WaitlistResult(WaitlistCode.NOT_COMPETED);
            }
            
            Match userMatch = DBController.getUsers().get(userId).getCurrentMatch();
            if (userMatch!=null) {
                return new WaitlistResult(WaitlistCode.IN_MATCH);
            }
            for (WaitlistRequest req:waitingList) {
                if (req.getUserId()==userId) {
                    return new WaitlistResult(WaitlistCode.IN_MATCH);
                }
            }
            waitingList.add(new WaitlistRequest(userId, event));
            System.out.println("added "+userId+" to waiting list");
            return new WaitlistResult(WaitlistCode.SUCCESS);
        }catch (Exception e) {
            e.printStackTrace();
            return new WaitlistResult(WaitlistCode.ERROR);
        }
    }

    //Idk what i was doing when i wrote this lol
    /*@GetMapping("/waiting-list/{userId}")
    private int getFromWaitingList(@PathVariable int userId) {
        return userId;
    }*/

    @DeleteMapping("/waiting-list")
    private void removeFromWaitingListReq(@RequestBody String userIdJSON) {
        int userId = new JSONObject(userIdJSON).getInt("userId");
        System.out.println("removed "+userId+" from waiting list");
        removeFromWaitingList(userId);
    }

    @GetMapping("/waiting-list/{event}")
    public int getFromWaitingList(@PathVariable String event) {
        int usersInWaitingList = 0;
        for (WaitlistRequest req:waitingList) {
            if (req.getEvent().equals(event)) {
                usersInWaitingList++;
            }
        }
        return usersInWaitingList;
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

    @PostMapping("/api/forfeit-match")
    public void forfeitMatch(@RequestBody int userId) {
        try {
            Match userMatch = DBController.getUsers().get(userId).getCurrentMatch();
            if (userMatch!=null) {
                for (int matchUserId:userMatch.getUsers()) {
                    if (matchUserId!=(int) userId) {
                        User quitUser = DBController.getUsers().get(userId);
                        userMatch.setQuitUser(quitUser);
                        quitUser.setCurrentMatch(null);
                        userMatch.setWinner(matchUserId);
                        MatchController.sendMatchData(userMatch);
                    }
                }
                
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
