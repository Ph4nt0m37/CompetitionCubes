package dev.pakn.competitioncubes;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SolveController {


    @MessageMapping("/solveData")
    @SendTo("/room/solves")
    public SolveData sendSolveData(SolveData data) {
        return data;
    }

    @MessageMapping("/switchTimer")
    @SendTo("/room/switchTimer")
    public TimerState sendStart(TimerState state) {
        return state;
    }
}
