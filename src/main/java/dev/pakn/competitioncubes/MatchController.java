package dev.pakn.competitioncubes;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class MatchController {

    private ArrayList<Match> matches = new ArrayList<>();

    @MessageMapping("/find-match")
    @SendTo("/room/matches")
    public Match findMatch(int userId) {
        //cloning waitlist because we want to ignore userId and if we don't clone it we will accidentally remove userId from actual waitlist
        ArrayList<Integer> waitList = (ArrayList<Integer>) CompController.getWaitingList().clone();
        waitList.remove((Integer) userId);
        if (!waitList.isEmpty()) {
            //find match and actually remove user from waitlist
            int opponentId = waitList.get(0);
            CompController.getWaitingList().remove((Integer) userId);
            CompController.getWaitingList().remove((Integer) opponentId);
            Match match = new Match(new int[]{userId,opponentId},(int)(Math.random()*9999999));
            matches.add(match);
            return match;
        }
        return new Match(null,-1);
    }

    @MessageMapping("/update-match")
    @SendTo("/room/matches")
    public Match updateMatch(MatchCommand command) {
        Match match = null;
        for (Match currMatch:matches) {
            if (currMatch.getRoomId()==command.getRoomId()) {
                match=currMatch;
            }
        }

        if (match==null) return null;
        
        if (command.getCommand().equals("solveFinished")) {
            match.nextSolver();
        }

        return match;
    }

    @GetMapping("/get-match-info/{roomId}")
    public Match getMatchInfo(@PathVariable int roomId) {
        for (Match currMatch:matches) {
            if (currMatch.getRoomId()==roomId) {
                return currMatch;
            }
        }
        return null;
    }
}
