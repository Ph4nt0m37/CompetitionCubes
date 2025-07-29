package dev.pakn.competitioncubes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class MatchController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private static ArrayList<Match> matches = new ArrayList<>();

    @MessageMapping("/find-match")
    @SendTo("/room/found-match")
    public Match findMatch(WaitlistRequest waitlistRequest) {
        //cloning waitlist because we want to ignore userId and if we don't clone it we will accidentally remove userId from actual waitlist
        ArrayList<WaitlistRequest> waitList = (ArrayList<WaitlistRequest>) CompController.getWaitingList().clone();
        for (int i=0;i<waitList.size();i++) {
            if (waitList.get(i).getUserId()==waitlistRequest.getUserId()) waitList.remove(i);
        }
        User user = DBController.getUsers().get(waitlistRequest.getUserId());
        for (WaitlistRequest oppReq:waitList) {
            User oppUser = DBController.getUsers().get(oppReq.getUserId());
            int oppId = oppReq.getUserId();
            Event event = DBController.stringToEventMap.get(waitlistRequest.getEvent());
            if (oppReq.getEvent()==waitlistRequest.getEvent() && Math.abs(user.getElo(event)-oppUser.getElo(event))<100) {
                //fix
                CompController.removeFromWaitingList(waitlistRequest.getUserId());
                CompController.removeFromWaitingList(oppId);
                System.out.println("match found between "+waitlistRequest.getUserId()+" and "+oppId);
                Match match = new Match(new int[]{waitlistRequest.getUserId(),oppId},(int)(Math.random()*9999999));
                matches.add(match);
                return match;
            }
        }
        return new Match(null,-1);
    }

    @MessageMapping("/update-match")
    public void updateMatch(MatchCommand command) {
        Match match = null;
        for (Match currMatch:matches) {
            if (currMatch.getRoomId()==command.getRoomId()) {
                match=currMatch;
            }
        }

        if (match==null) return;
        
        if (command.getCommand().equals("solveFinished")) {
            match.nextSolver();
        }

        simpMessagingTemplate.convertAndSend("/room/matches/"+command.getRoomId(),match);
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

    public static ArrayList<Match> getMatches() {
        return matches;
    }
}
