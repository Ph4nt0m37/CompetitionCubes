package dev.pakn.competitioncubes;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;

@Controller
public class MatchController {

    @MessageMapping("/find-match")
    @SendTo("/room/matches")
    public MatchData findMatch(int userId) {
        //cloning waitlist because we want to ignore userId and if we don't clone it we will accidentally remove userId from actual waitlist
        ArrayList<Integer> waitList = (ArrayList<Integer>) CompController.getWaitingList().clone();
        waitList.remove((Integer) userId);
        if (!waitList.isEmpty()) {
            //find match and actually remove user from waitlist
            CompController.getWaitingList().remove((Integer) userId);
            return new MatchData(new int[]{userId,CompController.getWaitingList().get(0)},(int)(Math.random()*9999999));
        }
        return new MatchData(null,-1);
    }
}
