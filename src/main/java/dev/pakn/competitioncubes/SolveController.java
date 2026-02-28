package dev.pakn.competitioncubes;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class SolveController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    @MessageMapping("/solveData")
    public void sendSolveData(@AuthenticationPrincipal User user, SentSolveData data) {
        try {
            ArrayList<Match> matches = MatchController.getMatches();
            for (Match currMatch:matches) {
                if (currMatch.getRoomId()==data.getRoomId()) {
                    SolveData solveData = new SolveData(currMatch.getEvent(),data.getTime(),data.getScramble(),Penalty.stringToPenalty(data.getPenalty()),user.getUserId());
                    currMatch.addSolve(user.getUserId(),solveData);
                }
            }
            simpMessagingTemplate.convertAndSend("/room/solves/"+data.getRoomId(),data);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/solveCompleted")
    public void sendSolveCompleted(EarlySolveData data) {
        simpMessagingTemplate.convertAndSend("/room/solveCompleted/"+data.getRoomId(),data);
    }

    @MessageMapping("/switchTimer")
    public void sendStart(TimerState state) {
        simpMessagingTemplate.convertAndSend("/room/switchTimer/"+state.getRoomId(),state);
    }
}
