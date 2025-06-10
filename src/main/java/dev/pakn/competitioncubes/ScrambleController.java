package dev.pakn.competitioncubes;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.worldcubeassociation.tnoodle.*;
import org.worldcubeassociation.tnoodle.scrambles.*;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleRegistry;

@Controller
public class ScrambleController {
    
    @MessageMapping("/scramble/3x3")
    @SendTo("/room/scrambles")
    private Scramble get3x3Scramble(int roomId) {
        PuzzleRegistry puzzleRegistry = PuzzleRegistry.THREE;
        Puzzle scrambler = puzzleRegistry.getScrambler();
        return new Scramble("three",scrambler.generateScramble(),roomId);
    }
}
