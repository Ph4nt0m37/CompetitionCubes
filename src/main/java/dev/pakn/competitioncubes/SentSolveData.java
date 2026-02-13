package dev.pakn.competitioncubes;

import lombok.Getter;

public class SentSolveData {
    private int roomId;
    private String time;
    private String scramble;
    private String penalty;
    private int userId;

    public SentSolveData() {}

    public SentSolveData(int id, String time, String penalty, int userId) {
        roomId = id;
        this.time=time;
        this.scramble="";
        this.penalty = penalty;
        this.userId=userId;
    }


    public SentSolveData(int id, String time, String penalty, String scramble, int userId) {
        roomId = id;
        this.time=time;
        this.scramble=scramble;
        this.penalty = penalty;
        this.userId=userId;
    }

    public String getTime() {
        return time;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getScramble() {
        return scramble;
    }

    public int getUserId() {
        return userId;
    }

    public String getPenalty() {
        return penalty;
    }
}
