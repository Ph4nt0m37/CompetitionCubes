package dev.pakn.competitioncubes;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SolveController {


    @MessageMapping("/solveData")
    @SendTo("/room/solves")
    public SolveData sendSolveData(SolveData data) {
        ArrayList<Match> matches = MatchController.getMatches();
        for (Match currMatch:matches) {
            if (currMatch.getRoomId()==data.getRoomId()) {
                ArrayList<String> times = currMatch.getUserTimes().get(data.getUserId());
                times.add(data.getTime());
            }
        }
        return data;
    }

    @MessageMapping("/switchTimer")
    @SendTo("/room/switchTimer")
    public TimerState sendStart(TimerState state) {
        return state;
    }
}
