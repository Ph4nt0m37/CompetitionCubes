package dev.pakn.competitioncubes;

import lombok.Getter;

public class SolveData {
    private int roomId;
    private String time;
    private String scramble;
    private int userId;

    public SolveData() {}

    public SolveData(int id, String time, int userId) {
        roomId = id;
        this.time=time;
        this.scramble="";
        this.userId=userId;
    }


    public SolveData(int id, String time, String scramble, int userId) {
        roomId = id;
        this.time=time;
        this.scramble=scramble;
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
}
