package dev.pakn.competitioncubes;

public class TimerState {
    private int roomId;
    private int state;
    private int userId;

    public TimerState() {}

    public TimerState(int roomId, int state, int userId) {
        this.roomId = roomId;
        this.state = state;
        this.userId = userId;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getState() {
        return state;
    }

    public int getUserId() {
        return userId;
    }
}
