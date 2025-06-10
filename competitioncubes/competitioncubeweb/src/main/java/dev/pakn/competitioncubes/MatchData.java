package dev.pakn.competitioncubes;

import java.util.ArrayList;

public class MatchData {
    private int[] users;
    private int roomId;

    public MatchData() {

    }

    public MatchData(int[] users, int roomId) {
        this.users=users;
        this.roomId=roomId;
    }

    public int[] getUsers() {
        return users;
    }

    public int getRoomId() {
        return roomId;
    }
}
