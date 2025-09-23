package dev.pakn.competitioncubes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.worldcubeassociation.tnoodle.scrambles.*;

@Controller
public class ScrambleController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    
    @MessageMapping("/scramble/3x3")
    private void get3x3Scramble(int roomId) {
        PuzzleRegistry puzzleRegistry = PuzzleRegistry.THREE;
        Puzzle scrambler = puzzleRegistry.getScrambler();
        simpMessagingTemplate.convertAndSend("/room/scrambles/"+roomId, new Scramble("three",scrambler.generateScramble(),roomId));
    }
}
