package dev.pakn.competitioncubes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private HashMap<Integer,ArrayList<Penalty>> userPenalties = new HashMap<>();

    private User winner = null;

    public Match() {
        currentSolver = users[0];
        for (int user:users) userScores.put(user, 0);
        for (int user:users) userTimes.put(user, new ArrayList<String>());
        for (int user:users) userPenalties.put(user, new ArrayList<Penalty>());
        generateNewScramble(PuzzleRegistry.THREE);
    }

    public Match(int[] users, int roomId) {
        this.users=users;
        this.roomId=roomId;
        currentSolver = users[0];
        for (int user:users) userScores.put(user, 0);
        for (int user:users) userTimes.put(user, new ArrayList<String>());
        for (int user:users) userPenalties.put(user, new ArrayList<Penalty>());
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

    public User getWinner() {
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

    public HashMap<Integer,ArrayList<Penalty>> getUserPenalties() {
        return userPenalties;
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
                double fastestTime = Integer.MAX_VALUE;
                for (int userId:users) {
                    double userTime = TimeConversions.timeToDouble(userTimes.get(userId).get(currentSolve));
                    if (userPenalties.get(userId).get(currentSolve)==Penalty.DNF) userTime=Integer.MAX_VALUE;
                    if (userTime<fastestTime) {
                        fastestTime=userTime;
                    }
                }
                for (int userId:users) {
                    double userTime = TimeConversions.timeToDouble(userTimes.get(userId).get(currentSolve));
                    if (userPenalties.get(userId).get(currentSolve)==Penalty.DNF) userTime=Integer.MAX_VALUE;
                    if (userTime==fastestTime) {
                        userScores.put(userId, userScores.get(userId)+1);
                    }
                    if (userScores.get(userId)>=5) {
                        winner = DBController.getUserByID(userId);
                        winner.setElo(winner.getElo()+10);
                        winner.saveUserData();
                        for (int loserUserId:users) {
                            if (loserUserId!=userId) {
                                User loser = DBController.getUserByID(loserUserId);
                                loser.setElo(loser.getElo()-10);
                                loser.saveUserData();
                            }
                        }
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
        double timeTotal = 0;
        ArrayList<String> solveTimes = userTimes.get(user);
        ArrayList<Double> solveTimesDouble = new ArrayList<>();
        ArrayList<Penalty> penalties = userPenalties.get(user);
        //getting the last 5 solves ONLY.
        for (int i = solveTimes.size()-5; i<solveTimes.size(); i++) {
            if (penalties.get(i)==Penalty.DNF) {
                solveTimesDouble.add((double) Integer.MAX_VALUE);
            }else {
                solveTimesDouble.add(TimeConversions.timeToDouble(solveTimes.get(i)));
            }
        }
        //removing biggest and smallest
        solveTimesDouble.remove(Collections.max(solveTimesDouble));
        solveTimesDouble.remove(Collections.min(solveTimesDouble));
        for (double time:solveTimesDouble) {
            if ((int) time==Integer.MAX_VALUE) {
                userAo5s.put(user, "DNF");
                return;
            }else {
                timeTotal+=time;
            }
        }
        userAo5s.put(user, TimeConversions.doubleToTime((Math.round((timeTotal/3.0)*100))/100.0));
    }
}
