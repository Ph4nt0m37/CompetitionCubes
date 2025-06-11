package dev.pakn.competitioncubes;

import java.util.ArrayList;
import java.util.HashMap;

import org.worldcubeassociation.tnoodle.scrambles.Puzzle;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleRegistry;

public class Match {
    private int[] users;
    private int roomId;
    private int currentSolver;
    private int solverIndex = 0;
    private String currentScramble;
    private HashMap<Integer,Integer> userScores = new HashMap<>();

    public Match() {
        currentSolver = users[0];
        for (int user:users) userScores.put(user, 0);
        generateNewScramble(PuzzleRegistry.THREE);
    }

    public Match(int[] users, int roomId) {
        this.users=users;
        this.roomId=roomId;
        currentSolver = users[0];
        for (int user:users) userScores.put(user, 0);
        generateNewScramble(PuzzleRegistry.THREE);
    }

    public int[] getUsers() {
        return users;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getCurrentSolver() {
        return currentSolver;
    }

    public String getCurrentScramble() {
        return currentScramble;
    }

    public void setCurrentSolver(int userId) {
        currentSolver = userId;
    }

    public String generateNewScramble(PuzzleRegistry puzzle) {
        PuzzleRegistry puzzleRegistry = puzzle;
        Puzzle scrambler = puzzleRegistry.getScrambler();
        currentScramble=scrambler.generateScramble();
        return currentScramble;
    }

    public void nextSolver() {
        solverIndex++;
        solverIndex=solverIndex%users.length;
        currentSolver = users[solverIndex];
        if (solverIndex==0) {
            generateNewScramble(PuzzleRegistry.THREE);
        }
    }
}
