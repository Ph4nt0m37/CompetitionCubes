package dev.pakn.competitioncubes;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class SolveController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    @MessageMapping("/solveData")
    public void sendSolveData(SolveData data) {
        try {
            ArrayList<Match> matches = MatchController.getMatches();
            for (Match currMatch:matches) {
                if (currMatch.getRoomId()==data.getRoomId()) {
                    ArrayList<Double> timesDoubles = currMatch.getUserTimesDoubles().get(data.getUserId());
                    ArrayList<Penalty> currentPenalties = currMatch.getUserPenalties().get(data.getUserId());
                    Penalty penalty = Penalty.OK;
                    if (data.getPenalty().equals("+2")) {
                        penalty=Penalty.PLUS_2;
                        timesDoubles.add(TimeConversions.timeToDouble(data.getTime())+2.0);
                    }
                    if (data.getPenalty().equals("+4")) {
                        penalty=Penalty.PLUS_4;
                        timesDoubles.add(TimeConversions.timeToDouble(data.getTime())+4.0);
                    }
                    if (data.getPenalty().equalsIgnoreCase("DNF")) {
                        penalty=Penalty.DNF;
                        timesDoubles.add((double) Integer.MAX_VALUE);
                    }
                    currentPenalties.add(penalty);
                    ArrayList<String> times = currMatch.getUserTimes().get(data.getUserId());
                    times.add(data.getTime());
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
