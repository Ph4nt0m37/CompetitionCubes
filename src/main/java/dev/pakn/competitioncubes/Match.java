package dev.pakn.competitioncubes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    private HashMap<Integer,ArrayList<SolveData>> userSolves = new HashMap<>();
    private HashMap<Integer,String> userAo5s = new HashMap<>();
    private HashMap<Integer,Integer> userScores = new HashMap<>();
    private HashMap<Integer,Double> userWcaSinglePbs = new HashMap<>();
    private HashMap<Integer,Double> userWcaAveragePbs = new HashMap<>();

    private User winner = null;
    private int eloChange = 0;

    private User quitUser = null;

    private boolean isPrivate = false;

    public Match() {
        
    }

    public Match(Event event, int[] users, int roomId) {
        this.users=users;
        this.roomId=roomId;
        this.event=event;
        currentSolver = users[0];
        for (int user:users) {
            userScores.put(user, 0);
            userSolves.put(user, new ArrayList<SolveData>());
            double[] wcaPbs = AntiCheat.getWCAPbs(DBController.getUserByIDList(user).getWcaId(), event);
            userWcaSinglePbs.put(user, wcaPbs[0]);
            userWcaAveragePbs.put(user, wcaPbs[1]);
        }
        generateNewScramble(EventToPuzzle.eventToPuzzle(event));
    }

    protected Match(Event event, int[] users, int roomId, boolean isPrivate) {
        this.users=users;
        this.roomId=roomId;
        this.event=event;
        this.isPrivate = isPrivate;
        currentSolver = users[0];
        for (int user:users) {
            userScores.put(user, 0);
            userSolves.put(user, new ArrayList<SolveData>());
            userWcaSinglePbs.put(user, AntiCheat.getWCASingle(DBController.getUserByIDList(user).getWcaId(), event));
            userWcaAveragePbs.put(user, AntiCheat.getWCAAverage(DBController.getUserByIDList(user).getWcaId(), event));
        }
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

    public User getQuitUser() {
        return quitUser;
    }

    public void setQuitUser(User user) {
        quitUser = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setCurrentSolver(int userId) {
        currentSolver = userId;
    }

    public HashMap<Integer,ArrayList<SolveData>> getUserSolves() {
        return userSolves;
    }

    public HashMap<Integer,Integer> getUserScores() {
        return userScores;
    }

    public HashMap<Integer,String> getUserAo5s() {
        return userAo5s;
    }

    public double getUserWcaPbSingle(int userId) {
        return userWcaSinglePbs.get(userId);
    }

    public double getUserWcaPbAvg(int userId) {
        return userWcaAveragePbs.get(userId);
    }

    public int getEloChange() {
        return eloChange;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean addSolve(int userId, SolveData solve) {
        if (!isPrivate) {
            boolean isValidSolve = AntiCheat.validateSolve(solve, getUserWcaPbAvg(solve.getUserId()), getUserWcaPbSingle(solve.getUserId()));

            User user = DBController.getUserByIDList(userId);
            if (isValidSolve) {
                if (user.getAllSinglesArray(event).length<5 || solve.getPenalizedTime()<=user.getLastStoredPbSingle(event)) {
                    user.addSingle(event, solve.getPenalizedTime());
                }
            }
        }
        return userSolves.get(userId).add(solve);
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
            for (int userId:users) {
                if (userSolves.get(userId).size()>4) {
                    double ao5 = calculateAo5(userId);
                }
            }
            if (solverIndex==0) {
                double fastestTime = Integer.MAX_VALUE;
                for (int userId:users) {
                    SolveData solve = userSolves.get(userId).get(userSolves.get(userId).size()-1);
                    double userTime = solve.getPenalizedTime();
                    if (userTime<fastestTime) {
                        fastestTime=userTime;
                    }
                }
                for (int userId:users) {
                    SolveData solve = userSolves.get(userId).get(userSolves.get(userId).size()-1);
                    double userTime = solve.getPenalizedTime();
                    if (userTime==fastestTime) {
                        userScores.put(userId, userScores.get(userId)+1);
                    }
                    if (userScores.get(userId)>=5) {
                        setWinner(userId);
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

    public double calculateAo5(int userId) {
        double timeTotal = 0;
        ArrayList<SolveData> solves = userSolves.get(userId);
        ArrayList<Double> solveTimesDouble = new ArrayList<>();
        //getting the last 5 solves ONLY.
        for (int i = solves.size()-5; i<solves.size(); i++) {
            solveTimesDouble.add(solves.get(i).getPenalizedTime());
        }
        //removing biggest and smallest
        solveTimesDouble.remove(Collections.max(solveTimesDouble));
        solveTimesDouble.remove(Collections.min(solveTimesDouble));
        for (double time:solveTimesDouble) {
            if ((int) time==Integer.MAX_VALUE) {
                userAo5s.put(userId, "DNF");
                return -1;
            }else {
                timeTotal+=time;
            }
        }
        double averageDouble = (Math.round((timeTotal/3.0)*100))/100.0;
        userAo5s.put(userId, TimeConversions.doubleToTime(averageDouble));
        if (!isPrivate) {
            User user = DBController.getUsers().get(userId);
            double userPbAverage = user.getAverage(event) < 0 ? Integer.MAX_VALUE : user.getAverage(event);
            boolean isValidAverage = AntiCheat.validateAverage(user,event, averageDouble, userWcaAveragePbs.get(userId), userWcaSinglePbs.get(userId));
            if (isValidAverage && averageDouble>0 && averageDouble<userPbAverage) {
                user.addAverage(event, averageDouble);
            }
        }
        return averageDouble;
    }

    public void setWinner(int userId) {
        eloChange = 15;
        winner = DBController.getUsers().get(userId);
        if (!isPrivate) {
            int winnerElo = winner.getElo(event);
            for (int loserUserId:users) {
                if (loserUserId!=winner.getUserId()) {
                    User loser = DBController.getUsers().get(loserUserId);
                    int loserElo = loser.getElo(event);
                    if (loserElo>0 && Math.abs(winnerElo-loserElo)>75) {
                        if (winnerElo<loserElo) {
                            eloChange=(Math.abs(winnerElo-loserElo)/4)*(loserElo/winnerElo);
                        }else {
                            eloChange=(int)((Math.abs(winnerElo-loserElo)/4)*(loserElo/Math.pow(winnerElo,1.325)));
                        }
                        eloChange=Math.abs(Math.max(5,Math.min(100, eloChange)));
                    }

                    int loserNewElo = loserElo-eloChange;
                    //whoop whoop ternary operator :D
                    loserNewElo = loserNewElo>=0 ? loserNewElo : 0;
                    loser.setElo(event, loserNewElo);
                    loser.addLoss();
                    BadgeController.calculateAndGrantBadges(this);
                    loser.calculateRanks();
                    loser.saveUserData();
                    loser.setCurrentMatch(null);
                    DBController.saveDataForEvent(loserUserId, event, loserNewElo, loser.getSingle(event), loser.getAverage(event));
                }
            }

            int winnerNewElo = winnerElo+eloChange;
            winner.setElo(event, winnerNewElo);
            winner.addWin();
            winner.setCurrentMatch(null);
            BadgeController.calculateAndGrantBadges(this);
            winner.calculateRanks();
            DBController.saveDataForEvent(userId, event, winnerNewElo, winner.getSingle(event), winner.getAverage(event));
            winner.saveUserData();
        }else {
            for (int loserUserId:users) {
                User loser = DBController.getUsers().get(loserUserId);
                loser.setCurrentMatch(null);
            }
            winner.setCurrentMatch(null);
        }
        MatchController.getMatches().remove(this);
    }
}
