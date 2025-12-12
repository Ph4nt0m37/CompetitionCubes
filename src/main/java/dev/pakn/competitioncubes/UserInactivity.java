package dev.pakn.competitioncubes;

public class UserInactivity {
    private int userId;
    private int timer = 0;
    private int maxTime;

    UserInactivity(int userId, int maxTime) {
        this.userId = userId;
        this.maxTime = maxTime;
    }

    public int getUserId() {
        return userId;
    }

    public int getTime() {
        return timer;
    }

    public void incrementTime() {
        timer++;
    }

    public void decrementTime() {
        timer--;
    }

    public int getMaxTime() {
        return maxTime;
    }
}
