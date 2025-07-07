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
                ArrayList<Penalty> currentPenalties = currMatch.getUserPenalties().get(data.getUserId());
                Penalty penalty = Penalty.OK;
                if (data.getPenalty().equals("+2")) penalty=Penalty.PLUS_2;
                if (data.getPenalty().equals("+4")) penalty=Penalty.PLUS_4;
                if (data.getPenalty().equalsIgnoreCase("DNF")) penalty=Penalty.DNF;
                currentPenalties.add(penalty);
            }
        }
        return data;
    }

    @MessageMapping("/solveCompleted")
    @SendTo("/room/solveCompleted")
    public EarlySolveData sendSolveCompleted(EarlySolveData data) {
        return data;
    }

    @MessageMapping("/switchTimer")
    @SendTo("/room/switchTimer")
    public TimerState sendStart(TimerState state) {
        return state;
    }
}
