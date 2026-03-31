package dev.pakn.competitioncubes;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class MatchFinder {

    private final ServerInfo serverInfo;

    private static Logger logger = LoggerFactory.getLogger(MatchFinder.class);

    private static ArrayList<WaitlistRequest> waitingList = new ArrayList<>();

    MatchFinder(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @PostMapping("/api/waiting-list")
    private WaitlistResult addToWaitingList(@AuthenticationPrincipal User user, @RequestBody String userIdJSON) {
        if (ServerInfo.isUnderMaintenance()) return new WaitlistResult(WaitlistCode.MAINTENANCE);
        try {
            JSONObject requestJson = new JSONObject(userIdJSON);
            int userId = user.getUserId();
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
            removeFromWaitingList(userId);
            WaitlistRequest request = new WaitlistRequest(userId, event);
            waitingList.add(request);
            logger.debug("added "+userId+" to waiting list");
            return new WaitlistResult(WaitlistCode.SUCCESS);
        }catch (Exception e) {
            e.printStackTrace();
            return new WaitlistResult(WaitlistCode.ERROR);
        }
    }

    @DeleteMapping("/api/waiting-list")
    private void removeFromWaitingListReq(@AuthenticationPrincipal User user) {
        int userId = user.getUserId();
        logger.debug("removed "+userId+" from waiting list");
        removeFromWaitingList(userId);
    }

    @GetMapping("/api/waiting-list/{event}")
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
        return waitingList.removeAll(Collections.singleton(new WaitlistRequest(userId, null)));
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
