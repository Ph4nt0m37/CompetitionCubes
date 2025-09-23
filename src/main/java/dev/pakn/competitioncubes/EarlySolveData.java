package dev.pakn.competitioncubes;

public class EarlySolveData {
    private String time;
    private int roomId;
    private int userId;

    EarlySolveData(int roomId, String time, int userId) {
        this.roomId=roomId;
        this.time=time;
        this.userId=userId;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getTime() {
        return time;
    }

    public int getUserId() {
        return userId;
    }
}
