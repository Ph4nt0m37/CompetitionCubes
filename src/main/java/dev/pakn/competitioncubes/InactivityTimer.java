package dev.pakn.competitioncubes;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InactivityTimer {
    ArrayList<UserInactivity> userInactivityTimers = new ArrayList<>();

    @PostMapping("/api/reset-inactivity-timer")
    public ResponseEntity<?> resetInactivityTimer(@AuthenticationPrincipal User user, @RequestBody UserInactivity userInactivity) {
        if (user.getUserId()!=userInactivity.getUserId()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        for (int i=0;i<userInactivityTimers.size();i++) {
            if (userInactivityTimers.get(i).getUserId()==userInactivity.getUserId()) {
                userInactivityTimers.remove(i);
            }
        }
        userInactivityTimers.add(userInactivity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/api/remove-inactivity-timer")
    public ResponseEntity<?> deleteInactivityTimer(@AuthenticationPrincipal User user) {
        for (int i=0;i<userInactivityTimers.size();i++) {
            //should probably just write an equals method but im lazy
            if (userInactivityTimers.get(i).getUserId()==user.getUserId()) {
                userInactivityTimers.remove(i);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/get-inactivity-time/{userId}")
    public UserInactivity getInactivityTimer(@PathVariable int userId) {
        for (int i=0;i<userInactivityTimers.size();i++) {
            if (userInactivityTimers.get(i).getUserId()==userId) {
                return userInactivityTimers.get(i);
            }
        }
        return null;
    }

    @Scheduled(fixedRate = 1000)
    public void incrementInactivityTimer() {
        for (int i=0;i<userInactivityTimers.size();i++) {
            UserInactivity userInactivity = userInactivityTimers.get(i);
            //System.out.println(userInactivity.getUserId()+" | "+userInactivity.getTime()+" | "+userInactivity.getMaxTime());
            try {
                userInactivity.incrementTime();
                int userId = userInactivity.getUserId();
                Match userMatch = DBController.getUsers().get(userId).getCurrentMatch();
                if (userMatch!=null && userInactivity.getTime()>userInactivity.getMaxTime()) {
                    userInactivityTimers.remove(userInactivity);
                    //gotta subtract i by one to account for the fact that we just removed a user inactivity timer
                    i--;
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
