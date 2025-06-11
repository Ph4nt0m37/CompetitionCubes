package dev.pakn.competitioncubes;

import java.util.ArrayList;
import java.util.HashMap;

public class Match {
    private int[] users;
    private int roomId;
    private int currentSolver;
    private int solverIndex = 0;
    private HashMap<Integer,Integer> userScores = new HashMap<>();

    public Match() {
        currentSolver = users[0];
        for (int user:users) userScores.put(user, 0);
    }

    public Match(int[] users, int roomId) {
        this.users=users;
        this.roomId=roomId;
        currentSolver = users[0];
        for (int user:users) userScores.put(user, 0);
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

    public void setCurrentSolver(int userId) {
        currentSolver = userId;
    }

    public void nextSolver() {
        solverIndex++;
        solverIndex=solverIndex%users.length;
        currentSolver = users[solverIndex];
        System.out.println("solver switched to "+currentSolver);
    }
}
