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
    private Event event;
    private int currentSolver;
    private int solverIndex = 0;
    private int currentSolve = 0;
    private String currentScramble;
    private HashMap<Integer,ArrayList<String>> userTimes = new HashMap<>();
    private HashMap<Integer,String> userAo5s = new HashMap<>();
    private HashMap<Integer,Integer> userScores = new HashMap<>();
    private HashMap<Integer,ArrayList<Penalty>> userPenalties = new HashMap<>();

    private User winner = null;
    private int eloChange = 0;

    public Match() {
        currentSolver = users[0];
        for (int user:users) userScores.put(user, 0);
        for (int user:users) userTimes.put(user, new ArrayList<String>());
        for (int user:users) userPenalties.put(user, new ArrayList<Penalty>());
        event=Event.THREE_BY_THREE;
        generateNewScramble(EventToPuzzle.eventToPuzzle(event));
    }

    public Match(Event event, int[] users, int roomId) {
        this.users=users;
        this.roomId=roomId;
        this.event=event;
        currentSolver = users[0];
        for (int user:users) userScores.put(user, 0);
        for (int user:users) userTimes.put(user, new ArrayList<String>());
        for (int user:users) userPenalties.put(user, new ArrayList<Penalty>());
        generateNewScramble(EventToPuzzle.eventToPuzzle(event));
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

    public Event getEvent() {
        return event;
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

    public int getEloChange() {
        return eloChange;
    }

    public String generateNewScramble(PuzzleRegistry puzzle) {
        PuzzleRegistry puzzleRegistry = puzzle;
        Puzzle scrambler = puzzleRegistry.getScrambler();
        currentScramble=scrambler.generateScramble();
        return currentScramble;
    }

    public boolean nextSolver() {
        try {
            User currentSolverUser = DBController.getUsers().get(currentSolver);
            double currentSolveTime = TimeConversions.timeToDouble(userTimes.get(currentSolver).get(currentSolve));
            if (currentSolveTime<currentSolverUser.getSingle(event)) {
                currentSolverUser.setSingle(event, currentSolveTime);
            }
            solverIndex++;
            solverIndex=solverIndex%users.length;
            currentSolver = users[solverIndex];
            for (int userId:users) {
                if (userTimes.get(userId).size()>4) {
                    double ao5 = calculateAo5(userId);
                    User user = DBController.getUsers().get(userId);
                    if (ao5>0 && ao5<user.getAverage(event)) {
                        user.setAverage(event, ao5);
                    }
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
                        eloChange = 15;
                        winner = DBController.getUsers().get(userId);
                        for (int loserUserId:users) {
                            if (loserUserId!=winner.getUserId()) {
                                User loser = DBController.getUsers().get(loserUserId);
                                if (Math.abs(winner.getElo(event)-loser.getElo(event))>75) {
                                    if (winner.getElo(event)<loser.getElo(event)) {
                                        eloChange=(Math.abs(winner.getElo(event)-loser.getElo(event))/4)*(loser.getElo(event)/winner.getElo(event));
                                    }else {
                                        eloChange=(int)((Math.abs(winner.getElo(event)-loser.getElo(event))/4)*(loser.getElo(event)/Math.pow(winner.getElo(event),1.325)));
                                    }
                                    eloChange=Math.abs(Math.max(5,Math.min(100, eloChange)));
                                }
                                int loserNewElo = loser.getElo(event)-eloChange;
                                loser.setElo(event, loserNewElo);
                                loser.addLoss();
                                loser.saveUserData();
                                DBController.saveDataForEvent(loserUserId, event, loserNewElo, loser.getSingle(event), loser.getAverage(event));
                            }
                        }
                        int winnerNewElo = winner.getElo(event)+eloChange;
                        winner.setElo(event, winnerNewElo);
                        winner.addWin();
                        winner.saveUserData();
                        DBController.saveDataForEvent(userId, event, winnerNewElo, winner.getSingle(event), winner.getAverage(event));
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

    public double calculateAo5(int user) {
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
                return -1;
            }else {
                timeTotal+=time;
            }
        }
        double averageDouble = (Math.round((timeTotal/3.0)*100))/100.0;
        userAo5s.put(user, TimeConversions.doubleToTime(averageDouble));
        return averageDouble;
    }
}
