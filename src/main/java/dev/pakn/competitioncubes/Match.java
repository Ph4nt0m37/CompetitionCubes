package dev.pakn.competitioncubes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.worldcubeassociation.tnoodle.scrambles.Puzzle;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleRegistry;

public class Match {
    private int[] users;
    private int roomId;
    private int currentSolver;
    private int solverIndex = 0;
    private int currentSolve = 0;
    private String currentScramble;
    private HashMap<Integer,ArrayList<String>> userTimes = new HashMap<>();
    private HashMap<Integer,Integer> userScores = new HashMap<>();

    public Match() {
        currentSolver = users[0];
        for (int user:users) userScores.put(user, 0);
        for (int user:users) userTimes.put(user, new ArrayList<String>());
        generateNewScramble(PuzzleRegistry.THREE);
    }

    public Match(int[] users, int roomId) {
        this.users=users;
        this.roomId=roomId;
        currentSolver = users[0];
        for (int user:users) userScores.put(user, 0);
        for (int user:users) userTimes.put(user, new ArrayList<String>());
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

    public int getCurrentSolve() {
        return currentSolve;
    }

    public void setCurrentSolver(int userId) {
        currentSolver = userId;
    }

    public HashMap<Integer,ArrayList<String>> getUserTimes() {
        return userTimes;
    }

    public HashMap<Integer,Integer> getUserScores() {
        return userScores;
    }

    public String generateNewScramble(PuzzleRegistry puzzle) {
        PuzzleRegistry puzzleRegistry = puzzle;
        Puzzle scrambler = puzzleRegistry.getScrambler();
        currentScramble=scrambler.generateScramble();
        return currentScramble;
    }

    public void nextSolver() {
        try {
            solverIndex++;
            solverIndex=solverIndex%users.length;
            currentSolver = users[solverIndex];
            if (solverIndex==0) {
                double fastestTime = 10000;
                for (int userId:users) {
                    double userTime = TimeConversions.timeToDouble(userTimes.get(userId).get(currentSolve));
                    if (userTime<fastestTime) {
                        fastestTime=userTime;
                    }
                }
                for (int userId:users) {
                    double userTime = TimeConversions.timeToDouble(userTimes.get(userId).get(currentSolve));
                    if (userTime==fastestTime) {
                        userScores.put(userId, userScores.get(userId)+1);
                    }
                    if (userScores.get(userId)>=5) {
                        System.out.println(userId+" won!");
                    }
                }
                generateNewScramble(PuzzleRegistry.THREE);
                currentSolve++;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
