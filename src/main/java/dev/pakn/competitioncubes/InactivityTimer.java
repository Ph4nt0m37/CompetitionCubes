package dev.pakn.competitioncubes;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InactivityTimer {
    ArrayList<UserInactivity> userInactivityTimers = new ArrayList<>();

    @PostMapping("/api/reset-inactivity-timer")
    public void resetInactivityTimer(@RequestBody UserInactivity userInactivity) {
        for (int i=0;i<userInactivityTimers.size();i++) {
            if (userInactivityTimers.get(i).getUserId()==userInactivity.getUserId()) {
                userInactivityTimers.remove(i);
                userInactivityTimers.add(userInactivity);
                break;
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    public void incrementInactivityTimer() {
        for (UserInactivity userInactivity:userInactivityTimers) {
            try {
                userInactivity.incrementTime();
                int userId = userInactivity.getUserId();
                Match userMatch = DBController.getUsers().get(userId).getCurrentMatch();
                if (userMatch!=null && userInactivity.getTime()>userInactivity.getMaxTime()) {
                    userInactivityTimers.remove(userInactivity);
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
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
