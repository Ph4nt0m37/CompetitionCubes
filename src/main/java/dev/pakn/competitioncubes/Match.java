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
    private HashMap<Integer,String> userAo5s = new HashMap<>();
    private HashMap<Integer,Integer> userScores = new HashMap<>();

    private int winner = -1;

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

    public int getSolverIndex() {
        return solverIndex;
    }

    public int getWinner() {
        return winner;
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

    public HashMap<Integer,String> getUserAo5s() {
        return userAo5s;
    }

    public String generateNewScramble(PuzzleRegistry puzzle) {
        PuzzleRegistry puzzleRegistry = puzzle;
        Puzzle scrambler = puzzleRegistry.getScrambler();
        currentScramble=scrambler.generateScramble();
        return currentScramble;
    }

    public boolean nextSolver() {
        try {
            solverIndex++;
            solverIndex=solverIndex%users.length;
            currentSolver = users[solverIndex];
            for (int user:users) {
                if (userTimes.get(user).size()>4) {
                    calculateAo5(user);
                }
            }
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
                        winner = userId;
                        return true;
                    }
                }
                generateNewScramble(PuzzleRegistry.THREE);
                currentSolve++;
            }
            return false;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void calculateAo5(int user) {
        int timeTotal = 0;
        ArrayList<String> solveTimes = userTimes.get(user);
        for (int i = solveTimes.size()-5; i<solveTimes.size(); i++) {
            timeTotal+=TimeConversions.timeToDouble(solveTimes.get(i));
        }
        userAo5s.put(user, TimeConversions.doubleToTime(timeTotal/5.0));
    }
}
